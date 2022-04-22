package you.shall.not.pass.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import you.shall.not.pass.domain.Access;

@Getter
@Setter
@Builder
public class ViolationDto {
    private Boolean csrfPassed;
    private Access requiredAccess;
    private String message;
}
