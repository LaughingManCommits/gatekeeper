package you.shall.not.pass.security.bonus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import you.shall.not.pass.domain.SecurityKey;
import you.shall.not.pass.properties.SecurityProperties;
import you.shall.not.pass.repositories.SecretKeyRepository;
import you.shall.not.pass.service.DateService;

import javax.xml.bind.DatatypeConverter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityKeyService {

    private final SecretKeyRepository secretKeyRepository;
    private final SecureRandomService secureRandomService;
    private final SecurityProperties securityProperties;

    public SecurityKey determineSecurityKey(String kid) {
        return StringUtils.isBlank(kid) ? create() : findSecurityKey(kid);
    }

    private byte[] overSaltedKey() {
        return secureRandomService.generateSecureBuffer(securityProperties.getMacSizeBytes(),true);
    }

    public boolean isExpiredSignatureKey(SecurityKey securityKey) {
        final LocalDateTime creationDate = DateService.asLocalDateTime(securityKey.getDate()).plusSeconds(securityProperties.getRotationSeconds());
        long diff = LocalDateTime.now().until(creationDate, ChronoUnit.SECONDS);
        log.info("csrf secret key rotation in {} secs", diff);
        return diff <= 0;
    }

    private SecurityKey create() {
        final SecurityKey securityKey = new SecurityKey();
        securityKey.setKey(overSaltedKey());
        securityKey.setKid(DatatypeConverter.printBase64Binary(secureRandomService.generateSecureBuffer(securityProperties.getKidSizeBytes(), true)));
        securityKey.setDate(DateService.asDate(LocalDateTime.now()));
        return secretKeyRepository.save(securityKey);
    }

    public SecurityKey findSecurityKey(String kid) {
        Optional<SecurityKey> OptionalUser = secretKeyRepository.findByKid(kid);
        //TODO improve this error handling
        return OptionalUser.orElseThrow(() -> new RuntimeException("kid not found"));
    }
}
