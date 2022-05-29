package you.shall.not.pass.security.bonus;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import you.shall.not.pass.properties.SecurityProperties;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageService {

    private final SecurityProperties securityProperties;

    public byte[] serialize(Message message) {
        log.info("message.kid.length:{}", message.kid.length);
        // add byte for length checks
        ByteBuffer byteBuffer = ByteBuffer.allocate(1 + message.mac.length + 1
                + message.kid.length + message.message.length);

        // mac
        byteBuffer.put((byte) message.mac.length);
        byteBuffer.put(message.mac);

        // kid
        byteBuffer.put((byte) message.kid.length);
        byteBuffer.put(message.kid);

        // cipher
        byteBuffer.put(message.message);

        // return array
        return byteBuffer.array();
    }

    public Message deserialize(String value) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(DatatypeConverter.parseBase64Binary(value));
        //TODO add proper exceptions for failed validation

        // mac
        int macLength = (byteBuffer.get());
        if (macLength != securityProperties.getMacSizeBytes()) {
            throw new RuntimeException("invalid mac length");
        }

        byte[] mac = new byte[macLength];
        byteBuffer.get(mac);

        // kid
        int kidLength = (byteBuffer.get());
        if (kidLength != securityProperties.getKidSizeBytes()) {
            throw new RuntimeException("invalid kid length");
        }

        byte[] kid = new byte[kidLength];
        byteBuffer.get(kid);

        // cipher
        byte[] message = new byte[byteBuffer.remaining()];
        byteBuffer.get(message);

        // return message
        return Message.builder().mac(mac).message(message).kid(kid).build();
    }

    @Builder
    @Getter
    public static class Message {
        byte[] mac;
        byte[] message;
        byte[] kid;
    }

}
