package com.gate.keeper.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.gate.keeper.domain.AccessLevel;
import com.gate.keeper.domain.Session;
import com.gate.keeper.domain.UserAccount;
import com.gate.keeper.repositories.SessionRepository;
import com.gate.keeper.security.SecurityContextService;
import com.gate.keeper.security.SecurityCsrfService;

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
        //TODO replace with proper exception --> Fixed
        //TODO replace with proper exception --> Fixed

        final String username = userContextService.getCurrentUser().orElseThrow(
            () -> new UnknownUserException("Unknown user requesting session!"));

        final AccessLevel level = userContextService.getCurrentAccessLevel().orElseThrow(
            () -> new InvalidUserLevelException("Invalid user access level!"));

        return getSessionForAuthenticatedUser(sessionId, username, level);
    }

    public String createAnonymousSession(AccessLevel level) {
        LOG.info("Returning new session cookie");
        return createNewSessionCookie(level, null);
    }

    private Optional<String> getSessionForAuthenticatedUser(String currentSessionId, String username, AccessLevel level) {
        final UserAccount user = userService.findUserByName(username);
        final Optional<Session> currentSession = findSessionByToken(currentSessionId);
        final boolean expired = isExpiredSession(currentSession);

        if (!expired) {
            LOG.info("Extending current session and attaching user");
            //TODO replace with proper exception --> Fixed
            Session session = currentSession.orElseThrow(
                () -> new UnknownUserException("Unknown user requesting session!"));

            if (user.getId() == (session.getUserId())) {
                return Optional.of(session.getToken());
            }
            return Optional.of(rotateSessionToken(session, level, user));
        }

        LOG.info("returning new session cookie");
        return Optional.of(createNewSessionCookie(level, user));
    }

    public boolean isExpiredSession(Optional<Session> optionalSession) {
        return optionalSession.isEmpty() || optionalSession.filter(session -> LocalDateTime.now()
            .isAfter(DateService.asLocalDateTime(Date.from(session.getDate())))).isPresent();
    }

        private String rotateSessionToken(Session session, AccessLevel grant, UserAccount user) {
        final String token = securityCsrfService.generateToken();
        Instant date = DateService.asDate(LocalDateTime.now()
                .plusSeconds(sessionExpirySeconds)).toInstant();

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

        Instant date = DateService.asDate(LocalDateTime.now()
                .plusSeconds(sessionExpirySeconds)).toInstant();

        session.setDate(date);
        session.setLevel(grant);
        session.setToken(token);

        sessionRepository.save(session);
        return token;
    }

    private static class UnknownUserException extends RuntimeException {
        public UnknownUserException(String message) {
            super(message);
        }
    }

    private static class InvalidUserLevelException extends RuntimeException {
        public InvalidUserLevelException(String message) {
            super(message);
        }
    }
}
