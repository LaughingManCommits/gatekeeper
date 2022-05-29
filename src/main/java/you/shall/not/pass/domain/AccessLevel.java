package you.shall.not.pass.domain;

import java.util.Arrays;
import java.util.Optional;

public enum AccessLevel {
    Unknown(-1),
    Level0(0),
    Level1(1),
    Level2(2);

    public static final AccessLevel[] ACCESS_LEVELS = AccessLevel.values();
    private final int level;

    AccessLevel(int level) {
        this.level = level;
    }

    public static Optional<AccessLevel> find(String lvl) {
        return Arrays.stream(ACCESS_LEVELS).filter(gateKeeperGrant ->
                gateKeeperGrant.level == Integer.parseInt(lvl)).findFirst();
    }

    public static AccessLevel find(Integer lvl) {
        return Arrays.stream(ACCESS_LEVELS).filter(gateKeeperGrant ->
                gateKeeperGrant.level == lvl).findFirst()
                .orElseThrow();
    }

    public boolean levelIsHigher(AccessLevel sessionAccessLevel) {
        return sessionAccessLevel == null
                || this.level > sessionAccessLevel.level;
    }
}
