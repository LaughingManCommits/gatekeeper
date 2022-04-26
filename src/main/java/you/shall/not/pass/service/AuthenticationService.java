package you.shall.not.pass.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import you.shall.not.pass.domain.Access;
import you.shall.not.pass.domain.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static you.shall.not.pass.filter.SecurityFilter.SESSION_COOKIE;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationService {
    public static final String AUTHENTICATED = "authenticated";
    private final CookieService cookieService;
    private final CsrfCookieService csrfCookieService;
    private final SessionService sessionService;

    public void createAuthenticatedUserSession(HttpServletRequest request, HttpServletResponse response) {
        final Object attribute = request.getAttribute(AUTHENTICATED);
        if (attribute != null) {
            return;
        }

        final String sessionCookie = cookieService.getCookieValue(request, SESSION_COOKIE);
        log.info("access logic here");
        csrfCookieService.validateCsrfCookie(request);
        String newSession = sessionService.createNewSessionForUser(sessionCookie).orElseThrow();
        String newCsrf = csrfCookieService.getCsrfCookie();
        cookieService.addCookie(newCsrf, response);
        cookieService.addCookie(newSession, response);
        request.setAttribute(AUTHENTICATED, true);
    }

    public void createAnonymousSession(HttpServletRequest request, HttpServletResponse response) {
        final String sessionCookie = cookieService.getCookieValue(request, SESSION_COOKIE);
        final Optional<Session> sessionByToken = sessionService.findSessionByToken(sessionCookie);
        if (isNullOrExpiredSession(sessionCookie, sessionByToken)) {
            log.info("Should add anonymous session");
            String anonymousSession = sessionService.createAnonymousSession(Access.Level0);
            String csrfCookie = csrfCookieService.getCsrfCookie();
            cookieService.addCookie(csrfCookie, response);
            cookieService.addCookie(anonymousSession, response);
        }
    }

    private boolean isNullOrExpiredSession(String sessionCookie, Optional<Session> sessionByToken) {
        return sessionCookie == null || sessionService.isExpiredSession(sessionByToken);
    }

}
