package you.shall.not.pass.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import you.shall.not.pass.domain.AccessLevel;
import you.shall.not.pass.domain.Session;
import you.shall.not.pass.security.SecurityCsrfService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static you.shall.not.pass.security.SecurityFilter.SESSION_COOKIE;
import static you.shall.not.pass.security.SecurityCsrfService.CSRF_COOKIE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationService {
    public static final String AUTHENTICATED = "authenticated";
    private final CookieService cookieService;
    private final SecurityCsrfService securityCsrfService;
    private final SessionService sessionService;

    public void createAuthenticatedUserSession(HttpServletRequest request, HttpServletResponse response) {
        final Object attribute = request.getAttribute(AUTHENTICATED);
        if (attribute != null) {
            return;
        }

        log.info("access logic here");

        final String xsrfGuard = securityCsrfService.getCsrfGuardCheckValue(request);
        final String csrf = cookieService.getCookieValue(request, CSRF_COOKIE_NAME);

        securityCsrfService.validateCsrfCookie(xsrfGuard, csrf);

        //TODO null session should throw exception as anonymous session is the minimum requirement always
        final String sessionCookie = cookieService.getCookieValue(request, SESSION_COOKIE);

        //TODO should throw custom exception here
        final String sessionToken = sessionService.createNewSessionForUser(sessionCookie).orElseThrow();

        rotateCsrfToken(response);

        String newSessionCookie = cookieService.createCookie(SESSION_COOKIE, sessionToken, true);
        cookieService.addCookie(newSessionCookie, response);
        request.setAttribute(AUTHENTICATED, true);
    }

    public void createAnonymousSession(HttpServletRequest request, HttpServletResponse response) {
        rotateCsrfToken(response);

        final String sessionCookie = cookieService.getCookieValue(request, SESSION_COOKIE);
        if (isNullOrExpiredSession(sessionCookie)) {
            log.info("Should add anonymous session");
            String anonymousSessionToken = sessionService.createAnonymousSession(AccessLevel.Level0);
            String anonymousSessionCookie = cookieService.createCookie(SESSION_COOKIE, anonymousSessionToken, true);
            cookieService.addCookie(anonymousSessionCookie, response);
        }
    }

    private void rotateCsrfToken(HttpServletResponse response) {
        final String csrfToken = securityCsrfService.newCsrfCookie();
        final String csrfCookie = cookieService.createCookie(CSRF_COOKIE_NAME, csrfToken, false);
        cookieService.addCookie(csrfCookie, response);
    }

    private boolean isNullOrExpiredSession(String sessionCookie) {
        final Optional<Session> sessionByToken = sessionService.findSessionByToken(sessionCookie);
        return sessionCookie == null || sessionService.isExpiredSession(sessionByToken);
    }

}
