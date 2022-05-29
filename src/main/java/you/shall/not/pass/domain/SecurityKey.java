package you.shall.not.pass.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Setter
@Getter
public class SecurityKey {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private String kid;
    private byte[] key;
    //TODO use instants
    private Date date;
}
