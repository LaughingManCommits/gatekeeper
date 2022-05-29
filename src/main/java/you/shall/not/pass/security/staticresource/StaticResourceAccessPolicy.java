package you.shall.not.pass.security.staticresource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class StaticResourceAccessPolicy {

    private List<PolicyEntry> staticResources;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class PolicyEntry {
        private String pathPattern;
        private Integer requiredAccessLevel;
    }
}
