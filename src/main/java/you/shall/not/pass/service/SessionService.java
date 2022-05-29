package you.shall.not.pass.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import you.shall.not.pass.domain.AccessLevel;
import you.shall.not.pass.domain.Session;
import you.shall.not.pass.domain.UserAccount;
import you.shall.not.pass.repositories.SessionRepository;
import you.shall.not.pass.security.SecurityContextService;
import you.shall.not.pass.security.SecurityCsrfService;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {
    private static final Logger LOG = LoggerFactory.getLogger(SessionService.class);
    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final SecurityCsrfService securityCsrfService;
    private final SecurityContextService userContextService;

    @Value("${session.expiry.seconds}")
    private int sessionExpirySeconds;

    public Optional<Session> findSessionByToken(String token) {
        if (StringUtils.isBlank(token)) {
            return Optional.empty();
        }
        return sessionRepository.findByToken(token);
    }

    public Optional<String> createNewSessionForUser(String sessionId) {
        //TODO if a user is already known because has a active
        // session then his current user should be matched against
        // the stepped up authenticated user session
        //TODO replace with proper exception
        final String username = userContextService.getCurrentUser().orElseThrow(()
                -> new RuntimeException("unknown user requesting session!"));
        //TODO replace with proper exception
        final AccessLevel level = userContextService.getCurrentAccessLevel().orElseThrow(()
                -> new RuntimeException("Invalid user access level!"));
        return getSessionForAuthenticatedUser(sessionId, username, level);
    }

    public String createAnonymousSession(AccessLevel level) {
        LOG.info("returning new session cookie");
        return createNewSessionCookie(level, null);
    }

    private Optional<String> getSessionForAuthenticatedUser(String currentSessionId, String username, AccessLevel level) {
        final UserAccount user = userService.findUserByName(username);
        final Optional<Session> currentSession = findSessionByToken(currentSessionId);
        final boolean expired = isExpiredSession(currentSession);

        if (!expired) {
            LOG.info("extending current session and attaching user");
            //TODO replace with proper exception
            Session session = currentSession.orElseThrow(() -> new RuntimeException("unknown user requesting session!"));
            return Optional.of(rotateSessionToken(session, level, user));
        }

        LOG.info("returning new session cookie");
        return Optional.of(createNewSessionCookie(level, user));
    }

    public boolean isExpiredSession(Optional<Session> optionalSession) {
        return optionalSession.isEmpty() || optionalSession.filter(session -> LocalDateTime.now()
                .isAfter(DateService.asLocalDateTime(session.getDate()))).isPresent();
    }

    private String rotateSessionToken(Session session, AccessLevel grant, UserAccount user) {
        final String token = securityCsrfService.generateToken();
        Date date = DateService.asDate(LocalDateTime.now()
                .plusSeconds(sessionExpirySeconds));

        session.setUserId(user.getId());
        session.setDate(date);
        session.setLevel(grant);
        session.setToken(token);

        sessionRepository.save(session);
        return session.getToken();
    }

    public String createNewSessionCookie(AccessLevel grant, UserAccount user) {
        final String token = securityCsrfService.generateToken();

        Session session = new Session();

        if (user != null) {
            session.setUserId(user.getId());
        }

        Date date = DateService.asDate(LocalDateTime.now()
                .plusSeconds(sessionExpirySeconds));

        session.setDate(date);
        session.setLevel(grant);
        session.setToken(token);

        sessionRepository.save(session);
        return token;
    }
}
