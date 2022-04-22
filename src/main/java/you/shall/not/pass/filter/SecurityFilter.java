package you.shall.not.pass.filter;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import you.shall.not.pass.domain.Access;
import you.shall.not.pass.domain.Session;
import you.shall.not.pass.domain.UserDetail;
import you.shall.not.pass.dto.ViolationDto;
import you.shall.not.pass.exception.AccessGrantException;
import you.shall.not.pass.exception.CsrfViolationException;
import you.shall.not.pass.filter.staticresource.StaticResourceValidator;
import you.shall.not.pass.service.CookieService;
import you.shall.not.pass.service.CsrfProtectionService;
import you.shall.not.pass.service.SessionService;
import you.shall.not.pass.service.UserService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@Component
@Order(1)
@RequiredArgsConstructor
public class SecurityFilter implements Filter {

    public static final String SESSION_COOKIE = "GRANT";
    public static final String EXECUTE_FILTER_ONCE = "you.shall.not.pass.filter";

    private static final Logger LOG = LoggerFactory.getLogger(SecurityFilter.class);

    private final Gson gson;
    private final CookieService cookieService;
    private final SessionService sessionService;
    private final List<StaticResourceValidator> resourcesValidators;
    private final CsrfProtectionService csrfProtectionService;
    private final UserService userService;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            if (isValidRequest(httpServletRequest)) {
                shallNotPassLogic(httpServletRequest, (HttpServletResponse) response);
            }
            request.setAttribute(EXECUTE_FILTER_ONCE, true);
            chain.doFilter(request, response);
        } catch (AccessGrantException age) {
            LOG.warn("Access violation, {}", age.getMessage());
            processAccessGrantError((HttpServletResponse) response, age);
        } catch (CsrfViolationException cve) {
            LOG.warn("CSRF violation, {}", cve.getMessage());
            processCsrfViolation((HttpServletResponse) response, cve);
        }
    }

    private boolean isValidRequest(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getAttribute(EXECUTE_FILTER_ONCE) == null;
    }

    private void processCsrfViolation(HttpServletResponse response, CsrfViolationException cve) {
        ViolationDto violationDto = ViolationDto.builder()
                .message(cve.getMessage())
                .csrfPassed(false)
                .build();

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        writeResponse(response, gson.toJson(violationDto));
    }

    private void processAccessGrantError(HttpServletResponse response, AccessGrantException age) {
        ViolationDto violationDto = ViolationDto.builder()
                .message(age.getMessage())
                .requiredAccess(age.getRequired())
                .build();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        writeResponse(response, gson.toJson(violationDto));
    }

    private void shallNotPassLogic(HttpServletRequest request, HttpServletResponse response) {
        final String sessionCookie = cookieService.getCookieValue(request, SESSION_COOKIE);
        final Optional<Session> sessionByToken = sessionService.findSessionByToken(sessionCookie);
        final String requestedUri = request.getRequestURI();
        LOG.info("incoming request {} with token {}", requestedUri, sessionCookie);
        final Access grant = sessionByToken.map(Session::getGrant).orElse(null);
        LOG.info("user grant level {}", grant);
        final Optional<StaticResourceValidator> resourceValidator = getValidator(requestedUri);

        if (requestedUri.equals("/access") || requestedUri.equals("/resources")) {
            return;
        }

        grantAnonymousAccess(response, sessionCookie, sessionByToken, resourceValidator);
        printAccessAuditLog(request, sessionByToken);
        validateRequest(request, sessionByToken, grant, resourceValidator);
    }

    private void grantAnonymousAccess(HttpServletResponse response, String sessionCookie,
                                      Optional<Session> sessionByToken,
                                      Optional<StaticResourceValidator> resourceValidator) {
        if (resourceValidator.isEmpty() && isExpiredSession(sessionCookie, sessionByToken)) {
            cookieService.addCookie(csrfProtectionService.getCsrfCookie(), response);
            cookieService.addCookie(sessionService.createAnonymousSession(Access.Level0), response);
        }
    }

    private void validateRequest(HttpServletRequest request,
                                 Optional<Session> sessionByToken, Access grant,
                                 Optional<StaticResourceValidator> resourceValidator) {
        resourceValidator.ifPresent(validator -> {
            LOG.info("resource validator enforced {}", validator.requires());
            if (sessionService.isExpiredSession(sessionByToken)
                    || validator.requires().levelIsHigher(grant)) {
                throw new AccessGrantException(validator.requires(), "invalid access level");
            }
            csrfProtectionService.validateCsrfCookie(request);
        });
    }

    private void printAccessAuditLog(HttpServletRequest request, Optional<Session> sessionByToken) {
        sessionByToken.ifPresent(session -> {
            Optional<UserDetail> userById = userService.findUserById(session.getUserId());
            userById.ifPresent(user -> LOG.info("user {} is browsing resource {}",
                    user.getUserName(), request.getRequestURI()));
            if (userById.isEmpty()) {
                LOG.info("Anonymous user is browsing resource {}", request.getRequestURI());
            }
        });
    }

    private boolean isExpiredSession(String sessionCookie, Optional<Session> sessionByToken) {
        return sessionCookie == null || sessionService.isExpiredSession(sessionByToken);
    }

    private Optional<StaticResourceValidator> getValidator(String requestedUri) {
        return resourcesValidators.stream().filter(staticResourceValidator
                -> staticResourceValidator.isApplicable(requestedUri)).findFirst();
    }

    private void writeResponse(HttpServletResponse response, String message) {
        try {
            PrintWriter out = response.getWriter();
            LOG.info("response message {}", message);
            out.print(message);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
