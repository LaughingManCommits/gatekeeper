package you.shall.not.pass.security.bonus;

import at.favre.lib.crypto.HKDF;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import you.shall.not.pass.properties.SecurityProperties;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class KeyService {

    private final SecurityProperties securityProperties;

    public Keys expandToKeys(byte[] key) {
        byte[] authKey = HKDF.fromHmacSha256().expand(key, "authKey".getBytes(StandardCharsets.UTF_8), securityProperties.getMacSizeBytes());
        return Keys.builder().authKey(authKey).build();
    }

    @Builder
    @Getter
    public static class Keys {
        private final byte[] authKey;
    }
}
