package you.shall.not.pass.security.staticresource;

import lombok.Builder;
import lombok.Getter;
import you.shall.not.pass.domain.AccessLevel;


@Getter
@Builder
public class StaticResourceAccessValidatorResult {
    private final AccessLevel requiredAccessLevel;
    private final String requestedUri;
}
