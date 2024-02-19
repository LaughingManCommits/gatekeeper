package com.gate.keeper.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.gate.keeper.domain.UserAccount;
import com.gate.keeper.repositories.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserAccount findUserByName(String name) {
        Optional<UserAccount> optionalUser = repository.findByUserName(name);
        return optionalUser.orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Optional<UserAccount> findUserById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

}
