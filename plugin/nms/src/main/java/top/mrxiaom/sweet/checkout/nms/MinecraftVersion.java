package top.mrxiaom.sweet.checkout.nms;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * <a href="https://github.com/tr7zw/Item-NBT-API/blob/master/item-nbt-api/src/main/java/de/tr7zw/changeme/nbtapi/utils/MinecraftVersion.java">Thanks tr7zw</a>
 */
public enum MinecraftVersion {
    UNKNOWN(Integer.MAX_VALUE),
    MC1_7_R4(174), MC1_8_R3(183), MC1_9_R1(191), MC1_9_R2(192), MC1_10_R1(1101), MC1_11_R1(1111), MC1_12_R1(1121),
    MC1_13_R1(1131), MC1_13_R2(1132), MC1_14_R1(1141), MC1_15_R1(1151), MC1_16_R1(1161), MC1_16_R2(1162),
    MC1_16_R3(1163), MC1_17_R1(1171), MC1_18_R1(1181, true), MC1_18_R2(1182, true), MC1_19_R1(1191, true),
    MC1_19_R2(1192, true), MC1_19_R3(1193, true), MC1_20_R1(1201, true), MC1_20_R2(1202, true), MC1_20_R3(1203, true),
    MC1_20_R4(1204, true), MC1_21_R1(1211, true), MC1_21_R2(1212, true), MC1_21_R3(1213, true), MC1_21_R4(1214, true),
    MC1_21_R5(1215, true), MC1_21_R6(1216, true), MC1_21_R7(1217, true),

    MC26_1(260100, true);

    private static MinecraftVersion version;

    private static final Map<String, MinecraftVersion> VERSION_TO_REVISION = new HashMap<>() {
        {
            this.put("1.20", MC1_20_R1);
            this.put("1.20.1",  MC1_20_R1);
            this.put("1.20.2", MC1_20_R2);
            this.put("1.20.3", MC1_20_R3);
            this.put("1.20.4", MC1_20_R3);
            this.put("1.20.5", MC1_20_R4);
            this.put("1.20.6", MC1_20_R4);
            this.put("1.21", MC1_21_R1);
            this.put("1.21.1", MC1_21_R1);
            this.put("1.21.2", MC1_21_R2);
            this.put("1.21.3", MC1_21_R2);
            this.put("1.21.4", MC1_21_R3);
            this.put("1.21.5", MC1_21_R4);
            this.put("1.21.6", MC1_21_R5);
            this.put("1.21.7", MC1_21_R5);
            this.put("1.21.8", MC1_21_R5);
            this.put("1.21.9", MC1_21_R6);
            this.put("1.21.10", MC1_21_R6);
            this.put("1.21.11", MC1_21_R7);
            this.put("26.1", MC26_1);
        }
    };

    private final int versionId;
    private final boolean mojangMapping;
    MinecraftVersion(int versionId) {
        this(versionId, false);
    }
    MinecraftVersion(int versionId, boolean mojangMapping) {
        this.versionId = versionId;
        this.mojangMapping = mojangMapping;
    }

    public int getVersionId() {
        return versionId;
    }

    public boolean isMojangMapping() {
        return mojangMapping;
    }

    @NotNull
    public static MinecraftVersion getVersion() {
        if (version != null) {
            return version;
        }
        try {
            final String ver = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            version = MinecraftVersion.valueOf(ver.replace("v", "MC"));
        } catch (Exception ex) {
            version = VERSION_TO_REVISION.getOrDefault(Bukkit.getServer().getBukkitVersion().split("-")[0], UNKNOWN);
        }
        return version;
    }

}
