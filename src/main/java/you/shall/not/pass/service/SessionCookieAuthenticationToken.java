package you.shall.not.pass.service;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class SessionCookieAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private final Object principal;
    private final String sessionId;

    public SessionCookieAuthenticationToken(String sessionId, User principal) {
        super(principal.getAuthorities());
        if (sessionId == null || "".equals(sessionId)) {
            throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
        }
        this.principal = principal;
        this.sessionId = sessionId;
        setAuthenticated(true);
    }

    public SessionCookieAuthenticationToken(String sessionId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        if ((sessionId == null) || ("".equals(sessionId))) {
            throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
        }
        this.principal = null;
        this.sessionId = sessionId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof SessionCookieAuthenticationToken) {
            SessionCookieAuthenticationToken other = (SessionCookieAuthenticationToken) obj;
            return this.getSessionId().equals(other.getSessionId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.sessionId.hashCode();
        return result;
    }
}
