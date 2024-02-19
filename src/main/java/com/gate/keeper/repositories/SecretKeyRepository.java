package com.gate.keeper.repositories;

import org.springframework.data.repository.CrudRepository;
import com.gate.keeper.domain.SecurityKey;

import java.util.Optional;

public interface SecretKeyRepository extends CrudRepository<SecurityKey, Long> {

    Optional<SecurityKey> findByKid(String kid);
}
