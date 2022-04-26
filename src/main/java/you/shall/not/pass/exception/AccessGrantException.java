package you.shall.not.pass.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
import you.shall.not.pass.domain.Access;


@Getter
public class AccessGrantException extends AuthenticationException {

    private final Access required;

    public AccessGrantException(Access required, String message) {
        super(message);
        this.required = required;
    }

}
