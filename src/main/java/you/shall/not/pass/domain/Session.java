package you.shall.not.pass.domain;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Setter
@Getter
public class Session {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String sessionId;
    private AccessLevel level;
    private Long userId;
    private String token;
    //TODO use instants FIXED
    private Instant date;
}
