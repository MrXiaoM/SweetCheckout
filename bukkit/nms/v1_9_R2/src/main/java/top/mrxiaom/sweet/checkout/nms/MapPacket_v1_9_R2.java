package top.mrxiaom.sweet.checkout.nms;

import net.minecraft.server.v1_9_R2.MapIcon;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketPlayOutMap;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MapPacket_v1_9_R2 implements IMapPacket {
    @Override
    public Object createMapPacket(int mapId, byte[] colors) {
        List<MapIcon> mapIcons = new ArrayList<>();
        return new PacketPlayOutMap(mapId, (byte) 0, false, mapIcons, colors, 0, 0, 128, 128);
    }

    @Override
    public void sendPacket(Player player, Object packet) {
        CraftPlayer p = (CraftPlayer) player;
        p.getHandle().playerConnection.sendPacket((Packet<?>) packet);
    }
}