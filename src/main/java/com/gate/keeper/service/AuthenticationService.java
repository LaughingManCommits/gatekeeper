package com.gate.keeper.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.gate.keeper.domain.AccessLevel;
import com.gate.keeper.domain.Session;
import com.gate.keeper.security.SecurityCsrfService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static com.gate.keeper.security.SecurityFilter.SESSION_COOKIE;
import static com.gate.keeper.security.SecurityCsrfService.CSRF_COOKIE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationService {
    public static final String AUTHENTICATED = "authenticated";
    private final CookieService cookieService;
    private final SecurityCsrfService securityCsrfService;
    private final SessionService sessionService;

    public void createAuthenticatedUserSession(HttpServletRequest request, HttpServletResponse response) {
        if (request.getAttribute(AUTHENTICATED) != null) {
            return;
        }

        log.info("Access logic here");


        final String xsrfGuard = securityCsrfService.getCsrfGuardCheckValue(request);
        final String csrf = cookieService.getCookieValue(request, CSRF_COOKIE_NAME);

        securityCsrfService.validateCsrfCookie(xsrfGuard, csrf);

        final String sessionCookie = cookieService.getCookieValue(request, SESSION_COOKIE);

        //TODO null session should throw exception as anonymous session is the minimum requirement always --> Fixed

        //TODO should throw custom exception here --> Fixed

        final Session existingSession = sessionService.findSessionByToken(sessionCookie)
            .orElseThrow(() -> new SessionNotFoundException("Session not found"));

        final String sessionToken = sessionService.createNewSessionForUser(existingSession.getToken())
            .orElseThrow(() -> new SessionCreationException("Failed to create session"));

        rotateCsrfToken(response);

        String newSessionCookie = cookieService.createCookie(SESSION_COOKIE, sessionToken, true);
        cookieService.addCookie(newSessionCookie, response);
        request.setAttribute(AUTHENTICATED, true);
    }
    public void createAnonymousSession(HttpServletRequest request, HttpServletResponse response) {
        rotateCsrfToken(response);

        // Use orElseThrow() directly with a custom exception
        String anonymousSessionToken = sessionService.createAnonymousSession(AccessLevel.Level0);

            if(anonymousSessionToken==null) {
            throw new AnonymousSessionCreationException("Failed to create anonymous session");
            }

        String anonymousSessionCookie = cookieService.createCookie(SESSION_COOKIE, anonymousSessionToken, true);
        cookieService.addCookie(anonymousSessionCookie, response);
    }

    private void rotateCsrfToken(HttpServletResponse response) {
        final String csrfToken = securityCsrfService.newCsrfCookie();
        final String csrfCookie = cookieService.createCookie(CSRF_COOKIE_NAME, csrfToken, false);
        cookieService.addCookie(csrfCookie, response);
    }

    // Custom exception for when a session is not found
    private static class SessionNotFoundException extends RuntimeException {
        public SessionNotFoundException(String message) {
            super(message);
        }
    }

    // Custom exception for when session creation fails
    private static class SessionCreationException extends RuntimeException {
        public SessionCreationException(String message) {
            super(message);
        }
    }

    // Custom exception for when anonymous session creation fails
    private static class AnonymousSessionCreationException extends RuntimeException {
        public AnonymousSessionCreationException(String message) {
            super(message);
        }
    }


}
