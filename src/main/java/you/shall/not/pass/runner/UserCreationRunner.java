package you.shall.not.pass.runner;

import you.shall.not.pass.domain.AccessLevel;
import you.shall.not.pass.domain.Password;
import java.util.ArrayList;
import java.util.List;
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
            List<Password> passwordList= new ArrayList<>();
            passwordList.add(new Password(AccessLevel.Level1,passwordEncoder.encode(String.valueOf(newUser.getLevel1Password())).toCharArray()));
            passwordList.add(new Password(AccessLevel.Level2,passwordEncoder.encode(String.valueOf(newUser.getLevel1Password())).toCharArray()));

            UserAccount user = new UserAccount(newUser.getUserName(),
                (ArrayList<Password>) passwordList);
            Optional<UserAccount> optionalUser = resp.findByUserName(newUser.getUserName());
            optionalUser.ifPresent(dbUser -> dbUser.setId(user.getId()));
            UserAccount saved = resp.save(user);
            LOG.info("User {} created for {}...", saved.getId(), newUser.getUserName());
        }

    }
}
