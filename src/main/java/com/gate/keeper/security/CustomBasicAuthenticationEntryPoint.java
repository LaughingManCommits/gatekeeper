package com.gate.keeper.security;

import com.google.gson.Gson;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Gson gson;

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    private static class UsernameNotFoundException extends AuthenticationException {
        UsernameNotFoundException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    private static class UserAccountLockedException extends AuthenticationException {
        UserAccountLockedException(String message) {
            super(message);
        }
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        processError(response, authException);
    }

    private void processError(HttpServletResponse response, AuthenticationException authException) {
        int httpStatus;
        ViolationResponseDto violationResponseDto;

        if (authException instanceof UsernameNotFoundException) {
            httpStatus = HttpStatus.UNAUTHORIZED.value();
            violationResponseDto = new ViolationResponseDto(httpStatus, "Username not found", authException.getMessage());
            log.info("Setting HTTP status to {} - Username not found", httpStatus);
        } else if (authException instanceof UserAccountLockedException) {
            httpStatus = HttpStatus.FORBIDDEN.value();
            violationResponseDto = new ViolationResponseDto(httpStatus, "User account locked", authException.getMessage());
            log.info("Setting HTTP status to {} - User account locked", httpStatus);
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR.value();
            violationResponseDto = new ViolationResponseDto(httpStatus, "Authentication error", authException.getMessage());
            log.error("Unexpected authentication error occurred", authException);
        }

        response.setStatus(httpStatus);
        writeResponse(response, gson.toJson(violationResponseDto));
    }

    private void writeResponse(HttpServletResponse response, String message) {
        try {
            PrintWriter out = response.getWriter();
            log.info("Response message: {}", message);
            out.print(message);
            out.flush();
        } catch (Exception e) {
            log.error("Error writing response", e);
        }
    }

    //TODO improve error http status response
    // UsernameNotFoundException http status 401
    // UserAccountLocked http status 403
    // FIXED

    private static class ViolationResponseDto {
        private final int statusCode;
        private final String error;
        private final String message;

        public ViolationResponseDto(int statusCode, String error, String message) {
            this.statusCode = statusCode;
            this.error = error;
            this.message = message;
        }
    }
}
