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
import you.shall.not.pass.dto.ViolationDto;
import you.shall.not.pass.exception.AccessGrantException;
import you.shall.not.pass.exception.CsrfViolationException;
import you.shall.not.pass.filter.staticresource.StaticResourceValidator;
import you.shall.not.pass.security.SecurityContextService;
import you.shall.not.pass.service.*;

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
    private final SessionService sessionService;
    private final List<StaticResourceValidator> resourcesValidators;
    private final SecurityContextService userContextService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            if (isValidRequest(httpServletRequest)) {
                String requestURI = httpServletRequest.getRequestURI();
                shallNotPassLogic(requestURI);
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

    private void shallNotPassLogic(String requestURI) {
        final String sessionCookie = userContextService.getSessionToken();
        final Optional<Session> sessionByToken = sessionService.findSessionByToken(sessionCookie);
        LOG.info("incoming request on uri: '{}' with session token: {}", requestURI, sessionCookie);

        final String username = userContextService.getCurrentUser().orElse("unknown");
        final Access access = userContextService.getCurrentAccessLevel().orElse(Access.Unknown);

        LOG.info("session security context user: {}", username);
        LOG.info("session security context Access grant: {}", access);

        final Optional<StaticResourceValidator> resourceValidator = getValidator(requestURI);

        if (requestURI.equals("/access") || requestURI.equals("/resources")) {
            return;
        }

        validateRequest(sessionByToken, access, resourceValidator);
    }

    private void validateRequest(Optional<Session> sessionByToken, Access grant,
                                 Optional<StaticResourceValidator> resourceValidator) {
        resourceValidator.ifPresent(validator -> {
            LOG.info("resource validator enforced {}", validator.requires());
            if (sessionService.isExpiredSession(sessionByToken)
                    || validator.requires().levelIsHigher(grant)) {
                throw new AccessGrantException(validator.requires(), "invalid access level");
            }
        });
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
