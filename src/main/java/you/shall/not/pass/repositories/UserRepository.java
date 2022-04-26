package you.shall.not.pass.repositories;

import org.springframework.data.repository.CrudRepository;
import you.shall.not.pass.domain.UserAccount;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserAccount, Long> {
    Optional<UserAccount> findByUserName(String name);
}
