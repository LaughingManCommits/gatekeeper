package you.shall.not.pass.security;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import you.shall.not.pass.dto.ViolationDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Gson gson;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        processError(response, authException);
    }

    private void processError(HttpServletResponse response, AuthenticationException authException) {
        ViolationDto violationDto = ViolationDto.builder()
                .message(authException.getMessage())
                .build();
        // todo improve error http status response
        // UsernameNotFoundException http status 401
        // UserAccountLocked http status 403
        if (authException instanceof UsernameNotFoundException) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
        }

        writeResponse(response, gson.toJson(violationDto));
    }

    private void writeResponse(HttpServletResponse response, String message) {
        try {
            PrintWriter out = response.getWriter();
            log.info("response message {}", message);
            out.print(message);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
