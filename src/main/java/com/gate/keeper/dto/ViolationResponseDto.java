package com.gate.keeper.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.gate.keeper.domain.AccessLevel;

@Getter
@Setter
@Builder
public class ViolationResponseDto {
    private Boolean csrfPassed;
    private AccessLevel requiredAccessLevel;
    private String message;
}
