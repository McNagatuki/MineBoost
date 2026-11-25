package jp.mineboost;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;

final class Settings {
    static final double MAX = 100.0;
    static final ForgeConfigSpec SPEC;
    static final ForgeConfigSpec.ConfigValue<String> SPEED_TAG, DROP_TAG;
    static final ForgeConfigSpec.DoubleValue SPEED, DROP;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("speed");
        SPEED_TAG = b.comment("Exact scoreboard tag for mining speed.")
                .define("tag", "MB_speed_up", v -> v instanceof String s && validTag(s));
        SPEED = b.comment("Mining speed multiplier (1 = normal).")
                .defineInRange("multiplier", 2.0, 1.0, MAX);
        b.pop();
        b.push("drop");
        DROP_TAG = b.comment("Exact scoreboard tag for block drops.")
                .define("tag", "MB_drop_up", v -> v instanceof String s && validTag(s));
        DROP = b.comment("Expected item count multiplier (1 = normal).")
                .defineInRange("multiplier", 2.0, 1.0, MAX);
        b.pop();
        SPEC = b.build();
    }

    static boolean validTag(String s) {
        return s.matches("[A-Za-z0-9_.+:-]{1,128}");
    }

    static boolean speedEnabled(ServerPlayer player) {
        return player.getTags().contains(SPEED_TAG.get()) && SPEED.get() > 1.0;
    }

    static boolean dropEnabled(ServerPlayer player) {
        return player.getTags().contains(DROP_TAG.get()) && DROP.get() > 1.0;
    }
}
