package you.shall.not.pass.repositories;

import org.springframework.data.repository.CrudRepository;
import you.shall.not.pass.domain.SecurityKey;

import java.util.Optional;

public interface SecretKeyRepository extends CrudRepository<SecurityKey, Long> {

    Optional<SecurityKey> findByKid(String kid);
}
