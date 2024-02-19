package com.gate.keeper.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
import com.gate.keeper.domain.AccessLevel;


@Getter
public class AccessGrantException extends AuthenticationException {

    private final AccessLevel required;

    public AccessGrantException(AccessLevel required, String message) {
        super(message);
        this.required = required;
    }

}
