package com.gate.keeper.security.staticresource;

public interface StaticResourceAccessValidator {
    StaticResourceAccessValidatorResult findAccessForPath(String requestedPath);
}
