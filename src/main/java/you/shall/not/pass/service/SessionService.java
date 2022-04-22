package you.shall.not.pass.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import you.shall.not.pass.domain.Access;
import you.shall.not.pass.domain.Session;
import you.shall.not.pass.domain.UserDetail;
import you.shall.not.pass.repositories.SessionRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static you.shall.not.pass.filter.SecurityFilter.SESSION_COOKIE;

@Service
public class SessionService {

    private static final Logger LOG = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final CsrfProtectionService csrfProtectionService;
    private final CookieService cookieService;
    private final LogonUserService logonUserService;
    @Value("${session.expiry.seconds}")
    private int sessionExpirySeconds;

    public SessionService(SessionRepository sessionRepository, UserService userService,
                          CsrfProtectionService csrfProtectionService, CookieService cookieService,
                          LogonUserService logonUserService) {
        this.sessionRepository = sessionRepository;
        this.userService = userService;
        this.csrfProtectionService = csrfProtectionService;
        this.cookieService = cookieService;
        this.logonUserService = logonUserService;
    }

    public Optional<Session> findSessionByToken(String token) {
        if (StringUtils.isBlank(token)) {
            return Optional.empty();
        }
        return sessionRepository.findByToken(token);
    }

    public Optional<String> stepUpOrCreateSession(String sessionId) {
        final String username = logonUserService.getCurrentUser().orElseThrow(()
                -> new RuntimeException("unknown user requesting session!"));
        final Access level = logonUserService.getCurrentAccessLevel().orElseThrow(()
                -> new RuntimeException("Invalid user access level!"));
        return getSessionForAuthenticatedUser(sessionId, username, level);
    }

    public String createAnonymousSession(Access level) {
        LOG.info("returning new session cookie");
        return createNewSessionCookie(level, null);
    }

    private Optional<String> getSessionForAuthenticatedUser(String currentSessionId, String username, Access level) {
        final UserDetail user = userService.findUserByName(username);
        Optional<Session> currentSession = findSessionByToken(currentSessionId);
        boolean expired = isExpiredSession(currentSession);

        if (!expired) {
            LOG.info("extending current session and attaching user");
            Session session = currentSession.orElseThrow(() -> new RuntimeException("unknown user requesting session!"));
            return Optional.of(extendCurrentSession(session, level, user));
        }

        LOG.info("returning new session cookie");
        return Optional.of(createNewSessionCookie(level, user));
    }

    public boolean isExpiredSession(Optional<Session> optionalSession) {
        return optionalSession.isEmpty() || optionalSession.filter(session -> LocalDateTime.now()
                .isAfter(DateService.asLocalDateTime(session.getDate()))).isPresent();
    }

    private String extendCurrentSession(Session session, Access grant, UserDetail user) {
        session.setUserId(user.getId());
        session.setDate(DateService.asDate(LocalDateTime.now().plusSeconds(sessionExpirySeconds)));
        session.setGrant(grant);
        sessionRepository.save(session);
        return createCookie(session.getToken());
    }

    private String createNewSessionCookie(Access grant, UserDetail user) {
        final String token = csrfProtectionService.generateToken();

        Session session = new Session();

        if (user != null) {
            session.setUserId(user.getId());
        }

        session.setDate(DateService.asDate(LocalDateTime.now().plusSeconds(sessionExpirySeconds)));
        session.setGrant(grant);
        session.setToken(token);

        sessionRepository.save(session);
        return createCookie(token);
    }

    private String createCookie(String token) {
        return cookieService.createCookie(SESSION_COOKIE, token,true);
    }

}
