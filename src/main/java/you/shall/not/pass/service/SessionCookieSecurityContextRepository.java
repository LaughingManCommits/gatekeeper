package you.shall.not.pass.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import you.shall.not.pass.domain.Session;
import you.shall.not.pass.domain.UserAccount;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCookieSecurityContextRepository implements SecurityContextRepository {
    public static final String BLANK_PASSWORD = "";
    private final CookieService cookieService;
    private final UserService userService;
    private final SessionService sessionService;

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        log.info("loadContext: checking for session cookie");
        final HttpServletRequest request = requestResponseHolder.getRequest();
        final SecurityContext context = SecurityContextHolder.createEmptyContext();
        final String sessionCookie = cookieService.getCookieValue(request, SecurityFilter.SESSION_COOKIE);
        if (request.getRequestURI().equals("/home")
                || request.getRequestURI().equals("/authenticate")
                || sessionCookie == null) {
            return context;
        }

        log.info("loadContext: session cookie is provided: {}", sessionCookie);
        Optional<Session> sessionByToken = sessionService.findSessionByToken(sessionCookie);
        if (sessionByToken.isEmpty()) {
            return context;
        }

        final Session session = sessionByToken.get();
        final SimpleGrantedAuthority grant = new SimpleGrantedAuthority(session.getLevel().name());
        final Optional<UserAccount> userById = userService.findUserById(session.getUserId());
        final Set<SimpleGrantedAuthority> simpleGrantedAuthorities = Collections.singleton(grant);

        final SessionCookieAuthenticationToken authenticationToken = userById.map(userDetail ->
                new SessionCookieAuthenticationToken(sessionCookie, getPrincipal(simpleGrantedAuthorities, userDetail))).
                orElse(new SessionCookieAuthenticationToken(sessionCookie, simpleGrantedAuthorities));

        context.setAuthentication(authenticationToken);
        return context;
    }

    private User getPrincipal(Set<SimpleGrantedAuthority> simpleGrantedAuthorities, UserAccount userAccount) {
        return new User(userAccount.getUserName(), BLANK_PASSWORD, simpleGrantedAuthorities);
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        //do nothing
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        if (request.getRequestURI().equals("/home")
                || request.getRequestURI().equals("/authenticate")) {
            return false;
        }

        String cookieValue = cookieService.getCookieValue(request, SecurityFilter.SESSION_COOKIE);
        Optional<Session> sessionByToken = sessionService.findSessionByToken(cookieValue);
        boolean isValidCookie = sessionByToken.isPresent() && !sessionService.isExpiredSession(sessionByToken);
        log.info("request contains valid session cookie: {}", isValidCookie);
        return isValidCookie;
    }

}
