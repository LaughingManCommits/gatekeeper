package you.shall.not.pass.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import you.shall.not.pass.domain.UserDetail;
import you.shall.not.pass.repositories.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserDetail findUserByName(String name) {
        Optional<UserDetail> optionalUser = repository.findByUserName(name);
        return optionalUser.orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Optional<UserDetail> findUserById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

}
