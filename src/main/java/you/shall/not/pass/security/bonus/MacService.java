package you.shall.not.pass.security.bonus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import you.shall.not.pass.properties.SecurityProperties;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@RequiredArgsConstructor
public class MacService {

    private final SecurityProperties securityProperties;

    public void validate(byte[] mac, byte[] messageText, byte[] authKey) throws NoSuchAlgorithmException,
            InvalidKeyException {
        SecretKey macKey = new SecretKeySpec(authKey, securityProperties.getMacHashAlgorithm());
        Mac hmac = Mac.getInstance(securityProperties.getMacHashAlgorithm());
        hmac.init(macKey);
        hmac.update(messageText);
        byte[] refMac = hmac.doFinal();
        if (!MessageDigest.isEqual(refMac, mac)) {
            throw new SecurityException("mac validation failed");
        }
    }

    public byte[] generate(byte[] authKey, byte[] messageText) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKey macKey = new SecretKeySpec(authKey, securityProperties.getMacHashAlgorithm());
        Mac hmac = Mac.getInstance(securityProperties.getMacHashAlgorithm());
        hmac.init(macKey);
        hmac.update(messageText);
        return hmac.doFinal();
    }

}
