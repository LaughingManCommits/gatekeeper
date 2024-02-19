package you.shall.not.pass.domain;

import java.time.Instant;
import java.util.List;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Setter
@Getter
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String userName;
    private int failedAuthAttempts;
    private boolean disabled;
    //TODO use instants FIXED
    private Instant disabledDate;
    @Transient
    private List<Password> passwords;
//    private char[] level1Password;
//    private char[] level2Password;
     //TODO implement this
    // Fixed

    public UserAccount(String userName, List<Password> passwords) {
        this.userName = userName;
        this.passwords = passwords;
    }

    public UserAccount() {

    }
}