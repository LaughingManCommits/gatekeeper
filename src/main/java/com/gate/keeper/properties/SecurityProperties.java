package com.gate.keeper.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("security.signature")
//TODO implement immutable properties
public class SecurityProperties {
    private String macHashAlgorithm;
    private int macSizeBytes;
    private int kidSizeBytes;
    private int rotationSeconds;
}