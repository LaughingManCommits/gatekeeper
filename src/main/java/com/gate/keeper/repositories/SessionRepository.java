package com.gate.keeper.repositories;

import org.springframework.data.repository.CrudRepository;
import com.gate.keeper.domain.Session;

import java.util.Optional;

public interface SessionRepository extends CrudRepository<Session, Long> {

    Optional<Session> findByToken(String token);

}
