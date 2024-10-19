package top.mrxiaom.sweet.checkout.func;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.qrcode.QRCode;
import top.mrxiaom.sweet.checkout.SweetCheckout;
import top.mrxiaom.sweet.checkout.nms.NMS;

import java.util.*;

@AutoRegister
public class QRCodeManager extends AbstractModule implements Listener {
    public static class MapInfo {
        public final Player player;
        public final byte[] colors;
        public final ItemStack original;
        public final ItemStack newItem;

        public MapInfo(Player player, byte[] colors, ItemStack original, ItemStack newItem) {
            this.player = player;
            this.colors = colors;
            this.original = original;
            this.newItem = newItem;
            update();
        }

        public void update() {
            Object packet = NMS.createMapPacket(mapId, colors);
            NMS.sendPacket(player, packet);
        }

        public void giveItemBack() {
            player.getInventory().setItemInMainHand(original);
        }
    }
    private final Map<UUID, MapInfo> players = new HashMap<>();
    private static Material filledMap;
    protected static int mapId = 20070831;
    private String mapName;
    private List<String> mapLore;
    private Integer mapCustomModelData;
    public QRCodeManager(SweetCheckout plugin) {
        super(plugin);
        filledMap = Util.valueOr(Material.class, "FILLED_MAP", Material.MAP);
        registerEvents();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        mapId = config.getInt("map-item.id", 20070831);
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
            mapId = Math.min(32767, mapId);
        }
        mapName = config.getString("map-item.name");
        mapLore = config.getStringList("map-item.lore");
        mapCustomModelData = Util.parseInt(config.getString("map-item.custom-model-data")).orElse(null);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        PlayerInventory inv = e.getPlayer().getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (isMap(item)) {
                inv.setItem(i, null);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        onLeave(e.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        onLeave(e.getPlayer());
    }

    private void onLeave(Player player) {
        remove(player);
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();

        MapInfo mapInfo = players.get(player.getUniqueId());
        if (mapInfo != null) {
            if (isMap(player.getInventory().getItem(e.getNewSlot()))) {
                mapInfo.update();
            } else {
                e.setCancelled(true);
                if (!isMap(player.getInventory().getItem(e.getPreviousSlot()))) {
                    for (int i = 0; i < 9; i++) {
                        if (isMap(player.getInventory().getItem(i))) {
                            player.getInventory().setHeldItemSlot(i);
                            mapInfo.update();
                            break;
                        }
                    }
                }
            }
        }
    }

    public void generateMap(Player player, QRCode code) {
        ItemStack item = AdventureItemStack.buildItem(filledMap, mapName, mapLore);
        boolean component = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4);
        NBT.modify(item, nbt -> {
            nbt.setBoolean("SWEET_CHECKOUT_MAP", true);
            if (mapCustomModelData != null) {
                nbt.setInteger("CustomModelData", mapCustomModelData);
            }
            if (!component) { // 1.8-1.20.4
                nbt.setInteger("map", mapId);
            }
        });
        if (component) { // 1.20.5+
            NBT.modifyComponents(item, nbt -> {
                if (mapCustomModelData != null) {
                    nbt.setInteger("minecraft:custom_model_data", mapCustomModelData);
                }
                nbt.setInteger("minecraft:map_id", mapId);
            });
        }
        UUID uuid = player.getUniqueId();
        ItemStack old = player.getInventory().getItemInMainHand();
        players.put(uuid, new MapInfo(player, generateMapColors(code), old, item));
        player.getInventory().setItemInMainHand(item);
    }

    public void remove(Player player) {
        MapInfo info = players.remove(player.getUniqueId());
        if (info != null) {
            info.giveItemBack();
        }
    }

    public static boolean isMap(ItemStack item) {
        return item != null && NBT.get(item, nbt -> {
            return nbt.hasTag("SWEET_CHECKOUT_MAP");
        });
    }

    public static byte[] generateMapColors(QRCode code) {
        byte dark = getMapColor(29, 3);
        byte light = getMapColor(8, 2);
        int width = code.getModuleCount();
        boolean scaling = width * 2 < 128;
        if (scaling) width *= 2;
        int start = (128 - width) / 2;
        byte[] colors = new byte[16384];
        Arrays.fill(colors, light);
        System.out.println(width + ", " + start);
        for (int z = 0; z < width; z++) {
            for (int x = 0; x < width; x++) {
                int index = (start + x) + 128 * (start + z);
                if (scaling ? code.isDark(z / 2,x / 2) : code.isDark(z, x)) {
                    colors[index] = dark;
                }
            }
        }
        return colors;
    }

    public static byte getMapColor(int baseColor, int modifier) {
        return (byte) (baseColor << 2 | modifier & 3);
    }

    public static QRCodeManager inst() {
        return instanceOf(QRCodeManager.class);
    }
}
