package com.gate.keeper.repositories;

import org.springframework.data.repository.CrudRepository;
import com.gate.keeper.domain.UserAccount;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserAccount, Long> {
    Optional<UserAccount> findByUserName(String name);
}
