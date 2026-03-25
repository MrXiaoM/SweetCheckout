package top.mrxiaom.sweet.checkout.nms;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MapPacket_mojmap_26_1 implements IMapPacket {

    @Override
    public Object createMapPacket(int mapId, byte[] colors) {
        List<MapDecoration> mapIcons = new ArrayList<>();
        MapItemSavedData.MapPatch b = of(colors);
        return new ClientboundMapItemDataPacket(new MapId(mapId), (byte) 0, false, mapIcons, b);
    }

    private MapItemSavedData.MapPatch of(byte[] colors) {
        if (colors == null) return null;
        return new MapItemSavedData.MapPatch(0, 0, 128, 128, colors);
    }

    @Override
    public void sendPacket(Player player, Object packet) {
        ServerPlayer handle = ((CraftPlayer) player).getHandle();
        handle.connection.send((Packet<?>) packet);
    }

    @Override
    public byte[] getColors(MapRenderer renderer) {
        Class<?> type = renderer.getClass();
        try {
            Field worldMapField = type.getDeclaredField("worldMap");
            worldMapField.setAccessible(true);
            MapItemSavedData worldMap = (MapItemSavedData) worldMapField.get(renderer);
            return worldMap.colors;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(type.getName(), e);
        }
    }

    @Override
    @SuppressWarnings({"deprecation"})
    public @Nullable MapView getMap(@NotNull Integer mapId) {
        return Bukkit.getMap(mapId);
    }
}
