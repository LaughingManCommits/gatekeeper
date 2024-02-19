package you.shall.not.pass.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Password {
    //TODO replace the hardcoded password fields within
    // UserAccount object with a list/set of Password objects
    // allowing for more types of access levels
    // Fixed
    private AccessLevel accessLevel;
    private char[] password;

    public Password(AccessLevel accessLevel, char[] password) {
        this.accessLevel = accessLevel;
        this.password = password;
    }
}
