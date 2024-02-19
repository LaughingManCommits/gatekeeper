package you.shall.not.pass.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import you.shall.not.pass.exception.CsrfViolationException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.security.SecureRandom;


@Component
public class SecurityCsrfService {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityCsrfService.class);
    private final static SecureRandom SECURE_RANDOM = new SecureRandom();
    public final static String CSRF_COOKIE_NAME = "CSRF";
    public final static String XSRF_GUARD_NAME = "XSRF";
    private final static int STANDARD_SIZE_TOKEN = 16;
    private final static int CSRF_TOKEN_SIZE = 8;

    public String generateToken() {
        return generateToken(STANDARD_SIZE_TOKEN);
    }

    public String generateToken(int size) {
        byte[] buffer = new byte[size];
        SECURE_RANDOM.nextBytes(buffer);
        return DatatypeConverter.printBase64Binary(buffer);
    }

    public String newCsrfCookie() {
        return generateToken(CSRF_TOKEN_SIZE);
    }

    public void validateCsrfCookie(String xsrfGuard, String csrf) {
        //TODO improve all logging in all service classes, logging info everywhere seems problematic
        //TODO ensure that any logged tokens will be masked as following 03A3D7DA******* --> Fixed
        LOG.info("incoming csrf cookie: {}",  maskToken(csrf));
        LOG.info("incoming xsrf value: {}",  maskToken(xsrfGuard));
        //TODO improve error messages --> Fixed
        if (csrf == null || xsrfGuard == null) {
            throw new CsrfViolationException("Invalid CSRF token: Both XSRF guard and CSRF cookie must be provided.");
        }

        if (!csrf.equals(xsrfGuard)) {
            throw new CsrfViolationException("CSRF validation failed: XSRF guard and CSRF cookie do not match.");
        }
    }

    public String getCsrfGuardCheckValue(HttpServletRequest request) {
        String guardCheckValue = request.getHeader(XSRF_GUARD_NAME);
        if (guardCheckValue == null) {
            guardCheckValue = request.getParameter(XSRF_GUARD_NAME);
        }
        return guardCheckValue;
    }
    private String maskToken(String token) {
        return (token != null && token.length() >= 8) ? "03A3D7DA"+"********" : token;
    }


}
