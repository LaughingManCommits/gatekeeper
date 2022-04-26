package you.shall.not.pass.repositories;

import org.springframework.data.repository.CrudRepository;
import you.shall.not.pass.domain.Session;

import java.util.Optional;

public interface SessionRepository extends CrudRepository<Session, Long> {
    Optional<Session> findByToken(String token);
}
