package you.shall.not.pass.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import you.shall.not.pass.domain.AccessLevel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class SecurityContextService {

    public Optional<AccessLevel> getCurrentAccessLevel() {
        return getGateKeeperGrant();
    }

    private Optional<AccessLevel> getGateKeeperGrant() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = ((UserDetails) principal);
            List<GrantedAuthority> targetList = new ArrayList<>(userDetails.getAuthorities());
            return targetList.stream().map(grantedAuthority ->
                    AccessLevel.valueOf(grantedAuthority.getAuthority())).findAny();
        }

        if (principal == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof SessionCookieAuthenticationToken) {
                SessionCookieAuthenticationToken authenticationToken = ((SessionCookieAuthenticationToken) authentication);
                Collection<GrantedAuthority> authorities = authenticationToken.getAuthorities();
                return authorities.stream().findFirst().map(grant -> AccessLevel.valueOf(grant.getAuthority()));
            }
        }

        return Optional.empty();
    }

    public Optional<String> getCurrentUser() {
        if (SecurityContextHolder.getContext() == null
                || SecurityContextHolder.getContext().getAuthentication() == null) {
            return Optional.empty();
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = ((UserDetails) principal);
            return Optional.of(userDetails.getUsername());
        }

        return Optional.empty();
    }

    public Optional<String> getSessionToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof SessionCookieAuthenticationToken) {
            SessionCookieAuthenticationToken authenticationToken = ((SessionCookieAuthenticationToken) authentication);
            return Optional.of(authenticationToken.getSessionId());
        }

        return Optional.empty();
    }

}
