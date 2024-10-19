package top.mrxiaom.sweet.checkout.nms;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MapPacket_v1_20_R3 implements IMapPacket {
    @Override
    public Object createMapPacket(int mapId, byte[] colors) {
        List<MapIcon> mapIcons = new ArrayList<>();
        WorldMap.b b = colors == null ? null : new WorldMap.b(0, 0, 128, 128, colors);
        return new PacketPlayOutMap(mapId, (byte) 0, false, mapIcons, b);
    }

    @Override
    public void sendPacket(Player player, Object packet) {
        CraftPlayer p = (CraftPlayer) player;
        p.getHandle().c.b((Packet<?>) packet);
    }
}
