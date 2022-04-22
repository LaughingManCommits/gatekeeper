package you.shall.not.pass.repositories;

import org.springframework.data.repository.CrudRepository;
import you.shall.not.pass.domain.UserDetail;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserDetail, Long> {

    Optional<UserDetail> findByUserName(String name);

}
