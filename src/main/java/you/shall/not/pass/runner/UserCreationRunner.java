package you.shall.not.pass.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import you.shall.not.pass.domain.UserAccount;
import you.shall.not.pass.properties.UserProperties;
import you.shall.not.pass.repositories.UserRepository;

import java.util.Optional;

@Component
public class UserCreationRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(UserCreationRunner.class);

    private final UserRepository resp;
    private final PasswordEncoder passwordEncoder;
    private final UserProperties userProperties;

    public UserCreationRunner(UserRepository resp, PasswordEncoder passwordEncoder, UserProperties userProperties) {
        this.resp = resp;
        this.passwordEncoder = passwordEncoder;
        this.userProperties = userProperties;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {

        for (UserProperties.User newUser : userProperties.getUsers()) {
            UserAccount user = new UserAccount();
            user.setUserName(newUser.getUserName());
            user.setLevel1Password(passwordEncoder.encode(String.valueOf(newUser.getLevel1Password())).toCharArray());
            user.setLevel2Password(passwordEncoder.encode(String.valueOf(newUser.getLevel2Password())).toCharArray());
            Optional<UserAccount> optionalUser = resp.findByUserName(newUser.getUserName());
            optionalUser.ifPresent(dbUser -> dbUser.setId(user.getId()));
            UserAccount saved = resp.save(user);
            LOG.info("User {} created for {}...", saved.getId(), newUser.getUserName());
        }

    }
}
