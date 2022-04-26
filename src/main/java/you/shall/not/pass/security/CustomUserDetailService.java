package you.shall.not.pass.security;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import you.shall.not.pass.domain.Access;
import you.shall.not.pass.domain.UserAccount;
import you.shall.not.pass.repositories.UserRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository repository;

    public CustomUserDetailService(UserRepository repository) {
        this.repository = repository;
    }

    private UserGrantDetail getDetails(String lvl, UserAccount user) throws UsernameNotFoundException {
        // todo implement failed authentication attempts
        // todo reset user failed authentication attempts after successful logon
        // todo disable user account after 3 failed attempts (optionally in the last week), not further logon attempts allowed
        // todo throw unique exception when user account is disabled
        UserGrantDetail.UserGrantDetailBuilder builder = UserGrantDetail.builder();
        Set<GrantedAuthority> grants = new HashSet<>();
        Access grant = Access.find(lvl).orElseThrow(()
                -> new UsernameNotFoundException("requested grant not supported"));
        builder.userName(user.getUserName());
        if (Access.Level1 == grant) {
            builder.password(user.getLevel1Password());
        } else if (Access.Level2 == grant) {
            builder.password(user.getLevel2Password());
        }
        grants.add(new SimpleGrantedAuthority(grant.name()));
        return builder.grants(grants).build();
    }

    @Override
    public UserDetails loadUserByUsername(String user) throws UsernameNotFoundException {
        String[] userArray = user.split("#");
        if (userArray.length == 2) {
            String lvl = userArray[0];
            String userName = userArray[1];
            Optional<UserAccount> optionalUser = repository.findByUserName(userName);
            UserAccount gateKeeperUser = optionalUser
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            UserGrantDetail userGrantDetail = getDetails(lvl, gateKeeperUser);

            return new User(userGrantDetail.getUserName(),
                    new String(userGrantDetail.getPassword()), userGrantDetail.getGrants());
        }
        throw new UsernameNotFoundException("User not found");
    }

    @Builder
    @Data
    private static class UserGrantDetail {
        Set<GrantedAuthority> grants;
        String userName;
        char[] password;
    }
}
