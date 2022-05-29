package you.shall.not.pass.security.bonus;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureRandomService {
    // TODO insure that CsrfTokenService uses SecureRandomService for token generation
    private static final SecureRandom secureRandom = new SecureRandom();

    public byte[] generateSecureBuffer(int size, boolean spawnNewSeed) {
        byte[] buffer = new byte[size];
        if (spawnNewSeed) {
            secureRandom.setSeed(SecureRandom.getSeed(size));
        }
        secureRandom.nextBytes(buffer);
        return buffer;
    }

}
