package you.shall.not.pass.security.staticresource;

public interface StaticResourceAccessValidator {
    StaticResourceAccessValidatorResult findAccessForPath(String requestedPath);
}
