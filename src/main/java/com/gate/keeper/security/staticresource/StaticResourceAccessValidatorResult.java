package com.gate.keeper.security.staticresource;

import lombok.Builder;
import lombok.Getter;
import com.gate.keeper.domain.AccessLevel;


@Getter
@Builder
public class StaticResourceAccessValidatorResult {
    private final AccessLevel requiredAccessLevel;
    private final String requestedUri;
}
