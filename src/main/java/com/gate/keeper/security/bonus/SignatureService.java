package com.gate.keeper.security.bonus;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import com.gate.keeper.domain.SecurityKey;
import com.gate.keeper.exception.CsrfViolationException;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignatureService {

    //TODO implement SignatureService, chose either csrf token or grant token and insure that the cookie
    // is signed (outgoing) and validated (incoming)
    private final SecurityKeyService securityKeyService;
    private final MessageService messageService;
    private final MacService macService;
    private final KeyService keyService;

    public String produce(String value) {
        return produce(value, null);
    }

    public String produce(String value, String kid) {
        try {
            final SecurityKey securityKey = securityKeyService.determineSecurityKey(kid);
            final KeyService.Keys keys = keyService.expandToKeys(securityKey.getKey());
            byte[] signKid = DatatypeConverter.parseBase64Binary(securityKey.getKid());
            byte[] messageText = value.getBytes(StandardCharsets.UTF_8);
            byte[] mac = macService.generate(keys.getAuthKey(), messageText);
            byte[] serializedCipher = messageService.serialize(MessageService.Message.builder()
                    .message(messageText).kid(signKid).mac(mac).build());
            String signedValue = DatatypeConverter.printBase64Binary(serializedCipher);
            log.info("signed value: {}", signedValue);
            return signedValue;
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            log.warn("could not sign, {}", ex.getMessage(), ex);
        }
        throw new SignatureException("failed to validate signature for value:"+ value + ", kid:" + kid);
    }

    public ConsumedMessage consume(String signedText) {
        ConsumedMessage.ConsumedMessageBuilder builder = ConsumedMessage.builder();
        if (StringUtils.isBlank(signedText)) {
            return builder.build();
        }

        try {
            final MessageService.Message message = messageService.deserialize(signedText);
            final String kid = getKid(message);
            final SecurityKey securityKey = securityKeyService.findSecurityKey(kid);
            final KeyService.Keys keys = keyService.expandToKeys(securityKey.getKey());
            macService.validate(message.getMac(), message.getMessage(), keys.getAuthKey());
            byte[] messageText = message.getMessage();
            String value = new String(messageText, StandardCharsets.UTF_8);
            log.info("message value: {}", value);
            builder.message(value);
            builder.mustRotate(securityKeyService.isExpiredSignatureKey(securityKey));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            log.warn("failed to sign message, {}", ex.getMessage());
        }
        return builder.signedMessage(signedText).build();
    }


    private String getKid(MessageService.Message message) {
        byte[] kid = message.getKid();
        if (kid == null) {
            throw new CsrfViolationException("could not find kid, invalid csrf cookie");
        }
        return DatatypeConverter.printBase64Binary(kid);
    }

    @Builder
    @Getter
    public static class ConsumedMessage {
        private String message;
        private String signedMessage;
        private boolean mustRotate;
    }

}