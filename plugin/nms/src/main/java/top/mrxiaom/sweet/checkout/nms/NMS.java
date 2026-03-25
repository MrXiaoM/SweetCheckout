package top.mrxiaom.sweet.checkout.nms;

import com.google.common.collect.Iterables;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static top.mrxiaom.sweet.checkout.nms.MinecraftVersion.*;

public class NMS {
    private static IMapPacket mapPacket;
    private static boolean loaded;

    public static boolean isUnknownVersion() {
        return MinecraftVersion.getVersion().equals(UNKNOWN);
    }

    public static boolean init() {
        if (loaded) return true;
        MinecraftVersion version = MinecraftVersion.getVersion();
        if (version.equals(UNKNOWN) || version.equals(MC1_21_R7)) {
            try {
                mapPacket = new MapPacket_mojmap_1_21_11();
            } catch (Throwable ignored) {
                mapPacket = new MapPacket_v1_21_R7();
            }
            return loaded = true;
        }
        if (version.equals(MC1_21_R6)) {
            mapPacket = new MapPacket_v1_21_R6();
            return loaded = true;
        }
        if (version.equals(MC1_21_R5)) {
            mapPacket = new MapPacket_v1_21_R5();
            return loaded = true;
        }
        if (version.equals(MC1_21_R4)) {
            mapPacket = new MapPacket_v1_21_R4();
            return loaded = true;
        }
        if (version.equals(MC1_21_R3)) {
            mapPacket = new MapPacket_v1_21_R3();
            return loaded = true;
        }
        if (version.equals(MC1_21_R2)) {
            mapPacket = new MapPacket_v1_21_R2();
            return loaded = true;
        }
        if (version.equals(MC1_21_R1)) {
            mapPacket = new MapPacket_v1_21_R1();
            return loaded = true;
        }
        if (version.equals(MC1_20_R4)) {
            mapPacket = new MapPacket_v1_20_R4();
            return loaded = true;
        }
        if (version.equals(MC1_20_R3)) {
            mapPacket = new MapPacket_v1_20_R3();
            return loaded = true;
        }
        if (version.equals(MC1_20_R2)) {
            mapPacket = new MapPacket_v1_20_R2();
            return loaded = true;
        }
        if (version.equals(MC1_20_R1)) {
            mapPacket = new MapPacket_v1_20_R1();
            return loaded = true;
        }
        if (version.equals(MC1_19_R3)) {
            mapPacket = new MapPacket_v1_19_R3();
            return loaded = true;
        }
        if (version.equals(MC1_18_R2)) {
            mapPacket = new MapPacket_v1_18_R2();
            return loaded = true;
        }
        if (version.equals(MC1_17_R1)) {
            mapPacket = new MapPacket_v1_17_R1();
            return loaded = true;
        }
        if (version.equals(MC1_16_R3)) {
            mapPacket = new MapPacket_v1_16_R3();
            return loaded = true;
        }
        if (version.equals(MC1_15_R1)) {
            mapPacket = new MapPacket_v1_15_R1();
            return loaded = true;
        }
        if (version.equals(MC1_14_R1)) {
            mapPacket = new MapPacket_v1_14_R1();
            return loaded = true;
        }
        if (version.equals(MC1_13_R2)) {
            mapPacket = new MapPacket_v1_13_R2();
            return loaded = true;
        }
        if (version.equals(MC1_12_R1)) {
            mapPacket = new MapPacket_v1_12_R1();
            return loaded = true;
        }
        if (version.equals(MC1_11_R1)) {
            mapPacket = new MapPacket_v1_11_R1();
            return loaded = true;
        }
        if (version.equals(MC1_10_R1)) {
            mapPacket = new MapPacket_v1_10_R1();
            return loaded = true;
        }
        if (version.equals(MC1_9_R2)) {
            mapPacket = new MapPacket_v1_9_R2();
            return loaded = true;
        }
        if (version.equals(MC1_8_R3)) {
            mapPacket = new MapPacket_v1_8_R3();
            return loaded = true;
        }
        if (version.equals(MC1_7_R4)) {
            mapPacket = new MapPacket_v1_7_R4();
            return loaded = true;
        }
        return false;
    }

    public static Object createMapPacket(int mapId, byte[] colors) {
        return mapPacket.createMapPacket(mapId, colors);
    }

    public static void sendPacket(Player player, Object packet) {
        mapPacket.sendPacket(player, packet);
    }

    public static byte[] getColors(MapRenderer renderer) {
        return mapPacket.getColors(renderer);
    }

    public static @Nullable MapView getMap(@NotNull Integer mapId) {
        return mapPacket.getMap(mapId);
    }

    public static ItemStack overrideMapItem(ItemStack item) {
        return mapPacket.overrideMapItem(item);
    }

    public static @Nullable MapRenderer getFirstRenderer(@NotNull Integer mapId) {
        MapView mapView = getMap(mapId);
        if (mapView != null) {
            return Iterables.getFirst(mapView.getRenderers(), null);
        }
        return null;
    }
}
