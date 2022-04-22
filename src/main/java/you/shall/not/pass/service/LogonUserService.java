package you.shall.not.pass.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import you.shall.not.pass.domain.Access;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LogonUserService {

    public Optional<Access> getCurrentAccessLevel() {
        return getGateKeeperGrant();
    }

    private Optional<Access> getGateKeeperGrant() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = ((UserDetails) principal);
            List<GrantedAuthority> targetList = new ArrayList<>(userDetails.getAuthorities());
            return targetList.stream().map(grantedAuthority ->
                    Access.valueOf(grantedAuthority.getAuthority())).findAny();
        }

        return Optional.empty();
    }

    public Optional<String> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = ((UserDetails) principal);
            return Optional.of(userDetails.getUsername());
        }

        return Optional.empty();
    }

}
