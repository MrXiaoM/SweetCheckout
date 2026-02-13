package top.mrxiaom.sweet.checkout.func;

import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTReflectionUtil;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import de.tr7zw.changeme.nbtapi.utils.nmsmappings.ReflectionMethod;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.qrcode.QRCode;
import top.mrxiaom.sweet.checkout.Messages;
import top.mrxiaom.sweet.checkout.PluginCommon;
import top.mrxiaom.sweet.checkout.func.entry.PaymentInfo;
import top.mrxiaom.sweet.checkout.func.entry.ShopItem;
import top.mrxiaom.sweet.checkout.map.IMapSource;
import top.mrxiaom.sweet.checkout.map.MapQRCode;
import top.mrxiaom.sweet.checkout.nms.NMS;
import top.mrxiaom.sweet.checkout.packets.common.IPacket;
import top.mrxiaom.sweet.checkout.packets.common.IResponsePacket;
import top.mrxiaom.sweet.checkout.packets.plugin.PacketPluginCancelOrder;
import top.mrxiaom.sweet.checkout.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@AutoRegister
public class PaymentsAndQRCodeManager extends AbstractModule implements IPaymentManager, Listener {
    public static final String FLAG_SWEET_CHECKOUT_MAP = "SWEET_CHECKOUT_MAP";
    private static Material filledMap;

    private final Map<UUID, PaymentInfo> players = new HashMap<>();
    private final Map<UUID, String> processPlayers = new HashMap<>();
    private @NotNull Integer mapId = 20070831;
    private String mapName;
    private List<String> mapLore;
    private Integer mapCustomModelData;
    private String paymentActionBarProcess;
    private String paymentActionBarDone;
    private String paymentActionBarTimeout;
    private String paymentActionBarCancel;
    private byte mapDarkColor, mapLightColor;
    private byte[] mapDarkPattern, mapLightPattern;
    private final boolean useComponent = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4);
    private final boolean useLegacyMapId = !MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1);

    public PaymentsAndQRCodeManager(PluginCommon plugin) {
        super(plugin);
        filledMap = Util.valueOr(Material.class, "FILLED_MAP", Material.MAP);
        registerEvents();
        plugin.getScheduler().runTaskTimer(this::everySecond, 20L, 20L);
    }

    private void everySecond() {
        long now = System.currentTimeMillis();
        List<PaymentInfo> list = Lists.newArrayList(players.values());
        for (PaymentInfo info : list) {
            if (info.orderId == null) continue;
            Player player = info.player;
            if (now >= info.outdateTime) {
                if (plugin.processingLogs) {
                    info("玩家 " + player.getName() + " 超时未支付，已取消订单 " + info.orderId);
                }
                PaymentAPI.inst().send(new PacketPluginCancelOrder(info.orderId));
                info.giveItemBack();
                t(player, "超时未支付，已自动取消");
                if (paymentActionBarTimeout != null) {
                    AdventureUtil.sendActionBar(player, PAPI.setPlaceholders(player, paymentActionBarTimeout));
                }
                remove(info.orderId);
                continue;
            } else if (paymentActionBarProcess != null) {
                int timeout = (int) Math.floor((info.outdateTime - now) / 1000.0);
                AdventureUtil.sendActionBar(player, PAPI.setPlaceholders(player, paymentActionBarProcess.replace("%timeout%", String.valueOf(timeout))));
            }
            info.updateMapColors();
        }
    }

    @Deprecated
    public void putProcess(Player player) {
        putProcess(player, "deprecated");
    }

    @Override
    public void putProcess(Player player, String tag) {
        processPlayers.put(player.getUniqueId(), tag);
    }

    @Override
    public boolean isProcess(Player player) {
        UUID uuid = player.getUniqueId();
        return processPlayers.containsKey(uuid) || players.containsKey(uuid);
    }

    public int getProcessingCount(ShopItem shopItem) {
        int count = 0;
        String target = "buy:" + shopItem.id + ":";
        for (String tag : processPlayers.values()) {
            if (tag.startsWith(target)) count++;
        }
        return count;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        mapId = Math.max(0, config.getInt("map-item.id", 20070831));
        if (useLegacyMapId && mapId > 32767) {
            mapId = 0;
        }
        mapName = config.getString("map-item.name");
        mapLore = config.getStringList("map-item.lore");
        mapCustomModelData = Util.parseInt(config.getString("map-item.custom-model-data")).orElse(null);
        mapLightColor = getMapColor(
                config.getInt("map-item.colors.light.base", 8),
                config.getInt("map-item.colors.light.modifier", 2));
        mapDarkColor = getMapColor(
                config.getInt("map-item.colors.dark.base", 29),
                config.getInt("map-item.colors.dark.modifier", 3));
        mapLightPattern = Utils.readBase64(new File(plugin.getDataFolder(), "qrcode_light.map"), 16384);
        mapDarkPattern = Utils.readBase64(new File(plugin.getDataFolder(), "qrcode_dark.map"), 16384);

        paymentActionBarProcess = config.getString("payment.action-bar.process", null);
        paymentActionBarDone = config.getString("payment.action-bar.done", null);
        paymentActionBarTimeout = config.getString("payment.action-bar.timeout", null);
        paymentActionBarCancel = config.getString("payment.action-bar.cancel", null);
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

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (isMap(e.getItem())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();

        // 如果正在支付中
        PaymentInfo paymentInfo = players.get(player.getUniqueId());
        if (paymentInfo != null) {
            if (isMap(player.getInventory().getItem(e.getNewSlot()))) {
                // 手持的是二维码地图，就发送地图画面更新
                paymentInfo.updateMapColors();
            } else {
                e.setCancelled(true);
                if (isMap(player.getInventory().getItem(e.getPreviousSlot()))) return;
                // 手持的不是二维码地图，就强制手持二维码地图
                for (int i = 0; i < 9; i++) {
                    if (isMap(player.getInventory().getItem(i))) {
                        player.getInventory().setHeldItemSlot(i);
                        paymentInfo.updateMapColors();
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (e.isCancelled()) return;
        // 手持二维码地图按Q键
        if (!isMap(e.getItemDrop().getItemStack())) return;
        Player player = e.getPlayer();

        if (remove(player) != null) {
            Item item = e.getItemDrop();
            item.getItemStack().setAmount(0);
            item.setItemStack(new ItemStack(Material.AIR));
            item.remove();
            if (paymentActionBarCancel != null) {
                AdventureUtil.sendActionBar(player, PAPI.setPlaceholders(player, paymentActionBarCancel));
            }
            restoreMap(player, mapId);
        }
    }

    public void restoreMap(Player player, int mapId) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3)) {
            MapRenderer renderer = NMS.getFirstRenderer(mapId);
            if (renderer != null) {
                byte[] colors = NMS.getColors(renderer);
                Object packet = NMS.createMapPacket(mapId, colors);
                NMS.sendPacket(player, packet);
            }
        } else {
            MapView map = NMS.getMap(mapId);
            if (map != null) {
                player.sendMap(map);
            }
        }
    }

    @Override
    public <R extends IResponsePacket> void sendPacket(Player player, IPacket<R> packet, Consumer<R> resp) {
        if (!PaymentAPI.inst().send(packet, resp)) {
            Messages.not_connect.tm(player);
            PaymentsAndQRCodeManager.inst().remove(player);
        }
    }

    @Deprecated
    public void requireScan(Player player, QRCode code, String orderId, long outdateTime, Consumer<Double> done) {
        requireScan(player, new MapQRCode(code), orderId, outdateTime, done);
    }

    @Override
    public void requireScan(Player player, IMapSource source, String orderId, long outdateTime, Consumer<Double> done) {
        byte[] colors = source.generate(this);
        requireScan(player, colors, orderId, outdateTime, done);
    }

    @Override
    @SuppressWarnings({"deprecation"})
    public void requireScan(Player player, byte[] colors, String orderId, long outdateTime, Consumer<Double> done) {
        ItemStack item = AdventureItemStack.buildItem(filledMap, mapName, mapLore);
        if (useLegacyMapId) { // 1.8 - 1.12.2
            item.setDurability(mapId.shortValue());
        }
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3)) {
            NBT.modify(item, nbt -> {
                nbt.setBoolean(FLAG_SWEET_CHECKOUT_MAP, true);
                if (!useComponent) { // 1.8-1.20.4
                    if (mapCustomModelData != null) {
                        nbt.setInteger("CustomModelData", mapCustomModelData);
                    }
                    nbt.setInteger("map", mapId);
                }
            });
            item = NMS.overrideMapItem(item);
        } else {
            Object nmsItem; // 1.7 的 Bukkit 接口不支持通过 ItemMeta 传递 NBT
            // 详见 org.bukkit.craftbukkit.v1_7_R4.inventory.CraftMetaItem#applyToItem(NBTTagCompound)
            // 从 1.8 开始，才有 unhandledTags 字段，储存非原版的 NBT 标签

            nmsItem = ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, item);
            NBTContainer nbt = NBTReflectionUtil.convertNMSItemtoNBTCompound(nmsItem);
            NBTCompound tag = nbt.getOrCreateCompound("tag");
            tag.setBoolean(FLAG_SWEET_CHECKOUT_MAP, true);
            if (mapCustomModelData != null) {
                tag.setInteger("CustomModelData", mapCustomModelData);
            }
            tag.setInteger("map", mapId);
            nmsItem = NBTReflectionUtil.convertNBTCompoundtoNMSItem(nbt);
            item = (ItemStack) ReflectionMethod.ITEMSTACK_BUKKITMIRROR.run(null, nmsItem);
            item = NMS.overrideMapItem(item);
        }
        if (useComponent) { // 1.20.5+
            NBT.modifyComponents(item, nbt -> {
                if (mapCustomModelData != null) {
                    nbt.setInteger("minecraft:custom_model_data", mapCustomModelData);
                }
                nbt.setInteger("minecraft:map_id", mapId);
            });
        }
        UUID uuid = player.getUniqueId();
        ItemStack old = player.getInventory().getItemInHand();
        players.put(uuid, new PaymentInfo(mapId, player, colors, old, item, orderId, outdateTime, done));
        player.getInventory().setItemInHand(item);
    }

    public PaymentInfo remove(String orderId) {
        PaymentInfo payment = null;
        for (PaymentInfo pi : players.values()) {
            if (orderId.equals(pi.orderId)) {
                payment = pi;
                break;
            }
        }
        if (payment == null) return null;
        UUID uuid = payment.player.getUniqueId();
        processPlayers.remove(uuid);
        players.remove(uuid);
        return payment;
    }

    @Override
    public PaymentInfo remove(Player player) {
        UUID uuid = player.getUniqueId();
        processPlayers.remove(uuid);
        PaymentInfo info = players.remove(uuid);
        if (info != null) {
            if (info.orderId != null) {
                if (plugin.processingLogs) {
                    info("玩家 " + player.getName() + " 主动取消了订单 " + info.orderId);
                }
                PaymentAPI.inst().send(new PacketPluginCancelOrder(info.orderId));
            }
            info.giveItemBack();
        }
        return info;
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            remove(player);
        }
    }

    public void markDone(String orderId, String money) {
        PaymentInfo payment = remove(orderId);
        if (payment == null) return;
        payment.giveItemBack();
        Double moneyValue = Util.parseDouble(money).orElse(null);
        if (moneyValue == null) {
            t(payment.player, "内部错误"); // TODO: 移到语言文件
            return;
        }
        if (paymentActionBarDone != null) {
            AdventureUtil.sendActionBar(payment.player, PAPI.setPlaceholders(payment.player, paymentActionBarDone));
        }
        if (payment.done != null) {
            payment.done.accept(moneyValue);
        }
    }

    public static boolean isMap(ItemStack item) {
        return item != null && item.getType().equals(filledMap) && NBT.get(item, nbt -> {
            return nbt.hasTag(FLAG_SWEET_CHECKOUT_MAP);
        });
    }

    @Override
    public byte getMapDarkColor() {
        return mapDarkColor;
    }

    @Override
    public byte getMapLightColor() {
        return mapLightColor;
    }

    @Override
    public byte[] getMapDarkPattern() {
        return mapDarkPattern;
    }

    @Override
    public byte[] getMapLightPattern() {
        return mapLightPattern;
    }

    /**
     * 来自 Minecraft Wiki: <a href="https://zh.minecraft.wiki/w/%E5%9C%B0%E5%9B%BE%E5%AD%98%E5%82%A8%E6%A0%BC%E5%BC%8F#%E5%9C%B0%E5%9B%BE%E6%95%B0%E6%8D%AE%E5%AD%98%E5%82%A8%E6%A0%BC%E5%BC%8F">地图存储格式</a>
     */
    public static byte getMapColor(int baseColor, int modifier) {
        return (byte) (baseColor << 2 | modifier & 3);
    }

    public static PaymentsAndQRCodeManager inst() {
        return instanceOf(PaymentsAndQRCodeManager.class);
    }
}
