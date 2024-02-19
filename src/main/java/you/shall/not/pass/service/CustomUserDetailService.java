package you.shall.not.pass.service;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import you.shall.not.pass.domain.AccessLevel;
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
        int maxFailedAttempts = 3;
        if (user.isDisabled()) {
            throw new AccountBlockedException("User account is blocked.");
        }

        if (user.getFailedAuthAttempts() >= maxFailedAttempts) {
            // Disable the account after reaching the maximum failed attempts
            user.setDisabled(true);
            user.setDisabledDate(Instant.now());
            repository.save(user); // Update UserAccount in the repository
            throw new AccountBlockedException("User account disabled due to too many failed attempts.");
        }

        //TODO implement failed authentication attempts --> Fixed
        //TODO reset user failed authentication attempts after successful logon --> Fixed
        //TODO disable user account after 3 failed attempts (optionally in the last week), not further logon attempts allowed --> Fixed
        //TODO throw unique exception when user account is disabled  --> Fixed

        AccessLevel grant = AccessLevel.find(lvl).orElseThrow(()
                -> new UsernameNotFoundException("requested grant not supported"));

        return UserGrantDetail.builder()
            .userName(user.getUserName())
            .password(user.getPasswords())
            .grants(getGrantedAuthorities(grant))
            .failedAttempts(user.getFailedAuthAttempts())
            .build();

    }

    private Set<GrantedAuthority> getGrantedAuthorities(AccessLevel grant) {
        Set<GrantedAuthority> grants = new HashSet<>();
        grants.add(new SimpleGrantedAuthority(grant.name()));
        return grants;
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

            // Reset failed attempts on successful login
            if (loginSuccessful()) {
                gateKeeperUser.setFailedAuthAttempts(0);
                // Update UserAccount in the repository
                repository.save(gateKeeperUser);
            }
            return new User(userGrantDetail.getUserName(),
                    String.valueOf(userGrantDetail.getPassword()), userGrantDetail.getGrants());
        }
        throw new UsernameNotFoundException("User not found");
    }
    // Additional method to simulate a successful login
    private boolean loginSuccessful() {
        // Implement your logic for successful login here
        return true;
    }

    @Builder
    @Data
    private static class UserGrantDetail {
        Set<GrantedAuthority> grants;
        String userName;
        List password;
        int failedAttempts;
    }

    // Custom exception for blocked accounts
    private static class AccountBlockedException extends UsernameNotFoundException {
        public AccountBlockedException(String message) {
            super(message);
        }
    }

}
