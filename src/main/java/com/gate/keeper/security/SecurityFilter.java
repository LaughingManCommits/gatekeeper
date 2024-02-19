package com.gate.keeper.security;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.gate.keeper.domain.AccessLevel;
import com.gate.keeper.domain.Session;
import com.gate.keeper.dto.ViolationResponseDto;
import com.gate.keeper.exception.AccessGrantException;
import com.gate.keeper.exception.CsrfViolationException;
import com.gate.keeper.security.staticresource.StaticResourceAccessValidator;
import com.gate.keeper.security.staticresource.StaticResourceAccessValidatorResult;
import com.gate.keeper.service.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Set;

@Component
@Order(1)
@RequiredArgsConstructor
public class SecurityFilter implements Filter {

    public static final Set<String> IGNORE_REQUEST_URI_SET = Set.of("/authenticate", "/resources", "/home");

    public static final String SESSION_COOKIE = "GRANT";
    public static final String EXECUTE_FILTER_ONCE = "com.gate.keeper.filter";
    private static final Logger LOG = LoggerFactory.getLogger(SecurityFilter.class);
    private final Gson gson;
    private final SessionService sessionService;
    private final SecurityContextService userContextService;
    private final StaticResourceAccessValidator staticResourceAccessValidator;

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
        ViolationResponseDto violationResponseDto = ViolationResponseDto.builder()
                .message(cve.getMessage())
                .csrfPassed(false)
                .build();

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        writeResponse(response, gson.toJson(violationResponseDto));
    }

    private void processAccessGrantError(HttpServletResponse response, AccessGrantException age) {
        ViolationResponseDto violationResponseDto = ViolationResponseDto.builder()
                .message(age.getMessage())
                .requiredAccessLevel(age.getRequired())
                .build();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        writeResponse(response, gson.toJson(violationResponseDto));
    }

    private void shallNotPassLogic(String requestURI) {
        if (ignoreShallNoPassLogic(requestURI)) {
            return;
        }

        final Optional<String> sessionCookie = userContextService.getSessionToken();
        final Optional<Session> sessionByToken = getSessionByToken(sessionCookie);
        LOG.info("incoming request on uri: '{}' with session token: {}", requestURI, sessionCookie);

        final String username = userContextService.getCurrentUser().orElse("unknown");
        final AccessLevel accessLevel = userContextService.getCurrentAccessLevel().orElse(AccessLevel.Unknown);

        LOG.info("session security context user: {}", username);
        LOG.info("session security context Access grant: {}", accessLevel);

        validateRequest(sessionByToken, accessLevel, staticResourceAccessValidator.findAccessForPath(requestURI));
    }

    private boolean ignoreShallNoPassLogic(String requestURI) {
        return IGNORE_REQUEST_URI_SET.contains(requestURI);
    }

    private Optional<Session> getSessionByToken(Optional<String> sessionCookie) {
        return sessionCookie.map(sessionService::findSessionByToken).filter(Optional::isPresent).map(Optional::get);
    }

    private void validateRequest(Optional<Session> sessionByToken, AccessLevel currentAccessLevel,
                                 StaticResourceAccessValidatorResult result) {

        AccessLevel accessLevel = result.getRequiredAccessLevel();
        if (accessLevel == null) {
            //TODO throw proper error
            throw new RuntimeException("no static resource access mapping for path: " + result.getRequestedUri());
        }

        LOG.info("required static resource accessLevel: {}", accessLevel);
        if (sessionService.isExpiredSession(sessionByToken)
                || accessLevel.levelIsHigher(currentAccessLevel)) {
            throw new AccessGrantException(accessLevel, "invalid access level");
        }

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
