package you.shall.not.pass.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import you.shall.not.pass.domain.AccessLevel;

@Getter
@Setter
@Builder
public class ViolationResponseDto {
    private Boolean csrfPassed;
    private AccessLevel requiredAccessLevel;
    private String message;
}
