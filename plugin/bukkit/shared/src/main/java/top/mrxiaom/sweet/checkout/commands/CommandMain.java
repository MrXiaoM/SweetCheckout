package top.mrxiaom.sweet.checkout.commands;

import com.google.common.collect.Lists;
import net.kyori.adventure.inventory.Book;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.map.MapRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.checkout.Errors;
import top.mrxiaom.sweet.checkout.Messages;
import top.mrxiaom.sweet.checkout.PluginCommon;
import top.mrxiaom.sweet.checkout.database.TradeDatabase;
import top.mrxiaom.sweet.checkout.func.*;
import top.mrxiaom.sweet.checkout.func.entry.ShopItem;
import top.mrxiaom.sweet.checkout.func.modifier.Modifiers;
import top.mrxiaom.sweet.checkout.func.modifier.OrderInfo;
import top.mrxiaom.sweet.checkout.map.IMapSource;
import top.mrxiaom.sweet.checkout.nms.NMS;
import top.mrxiaom.sweet.checkout.packets.plugin.PacketPluginRequestOrder;
import top.mrxiaom.sweet.checkout.utils.NumberRange;
import top.mrxiaom.sweet.checkout.utils.Utils;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static top.mrxiaom.sweet.checkout.utils.Utils.*;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private boolean useWeChat, useAlipay, usePaypal;
    private int paymentTimeout;
    private int pointsScale;
    private Modifiers pointsModifiers;
    private List<String> pointsNames;
    private List<IAction> pointsCommands;
    private NumberRange pointsLimitRange;
    private int pointsLimitRound;
    private int statsTop;
    private boolean allowIncreasing;

    public CommandMain(PluginCommon plugin) {
        super(plugin);
        registerCommand("sweetcheckout", this);
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        plugin.processingLogs = !config.getBoolean("no-processing-logs");

        useWeChat = config.getBoolean("payment.enable.wechat");
        useAlipay = config.getBoolean("payment.enable.alipay");
        usePaypal = config.getBoolean("payment.enable.paypal");
        paymentTimeout = config.getInt("payment.timeout");

        allowIncreasing = config.getBoolean("payment.allow-increasing", false);

        pointsLimitRange = NumberRange.from(config.getString("points.limitation.range"));
        if (pointsLimitRange == null) {
            pointsLimitRange = new NumberRange(1, 9999);
        }
        pointsLimitRound = Math.max(0, Math.min(2, config.getInt("points.limitation.round", 2)));
        pointsScale = config.getInt("points.scale");
        pointsNames = config.getStringList("points.names");
        pointsCommands = ActionProviders.loadActions(config.getStringList("points.commands"));
        pointsModifiers = Modifiers.load(config, "points.modifiers");

        statsTop = config.getInt("stats.top", 5);
    }

    public int getPoints(double money) {
        return (int) Math.round(money * pointsScale);
    }

    public Modifiers getPointsModifiers() {
        return pointsModifiers;
    }

    @Override
    @SuppressWarnings({"deprecation"})
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length >= 3 && "points".equalsIgnoreCase(args[0]) && player.hasPermission("sweet.checkout.points")) {
                Map<String, String> params = collectArgs(args, 3);
                IPaymentManager manager;
                if (getBoolean(params, "mock", false)) {
                    if (!player.isOp()) {
                        return Messages.no_permission.tm(player);
                    }
                    manager = MockPaymentManager.inst();
                } else {
                    if (!PaymentAPI.inst().isConnected()) {
                        return Messages.not_connect.tm(player);
                    }
                    manager = PaymentsAndQRCodeManager.inst();
                }
                String type = args[1];
                double moneyDouble = Util.parseDouble(args[2]).orElse(0.0);
                if (!pointsLimitRange.isInRange(moneyDouble)) {
                    return Messages.commands__points__invalid_money.tm(player);
                }
                if (args[2].contains(".")) {
                    if (args[2].substring(args[2].indexOf('.') + 1).length() > pointsLimitRound) {
                        return Messages.commands__points__invalid_money.tm(player);
                    }
                }
                if ("wechat".equalsIgnoreCase(type)) {
                    if (!useWeChat) {
                        return Messages.commands__points__disabled__wechat.tm(player);
                    }
                } else if ("alipay".equalsIgnoreCase(type)) {
                    if (!useAlipay) {
                        return Messages.commands__points__disabled__alipay.tm(player);
                    }
                } else if ("paypal".equalsIgnoreCase(type)) {
                    if (!usePaypal) {
                        return Messages.commands__points__disabled__paypal.tm(player);
                    }
                } else {
                    return Messages.commands__points__unknown_type.tm(player);
                }
                if (moneyDouble < 0.01) {
                    return Messages.commands__points__invalid_money.tm(player);
                }
                int pointsDouble = getPoints(moneyDouble);
                OrderInfo orderInfo = new OrderInfo(player, moneyDouble, pointsDouble);
                try {
                    pointsModifiers.modify(orderInfo);
                } catch (Exception e) {
                    return Messages.commands__points__modifiers_error.tm(player, Pair.of("%error%", e.getMessage()));
                }
                String moneyStr = String.format("%.2f", Math.max(0.01, orderInfo.getMoney()));
                int points = orderInfo.getPoint();
                if (manager.isProcess(player)) {
                    return Messages.commands__points__processing.tm(player);
                }
                manager.putProcess(player, "points:" + moneyStr + ":" + type);
                String productName = random(pointsNames, "商品");
                Messages.commands__points__send.tm(player);
                manager.sendPacket(player, new PacketPluginRequestOrder(
                        player.getName(), type, productName, moneyStr, allowIncreasing
                ), resp -> {
                    String error = resp.getError();
                    if (!error.isEmpty()) {
                        warn("玩家 " + player.getName() + " 通过 " + type + " 下单点券 (￥" + moneyStr + ") 失败: " + error);
                        // 下单失败时提示玩家
                        Errors.fromString(error).tm(player, Pair.of("%type%", error));
                        manager.remove(player);
                        return;
                    }
                    // 下单成功操作
                    String orderId = resp.getOrderId();
                    String realMoney = resp.getMoney();
                    long now = System.currentTimeMillis();
                    long outdateTime = now + (paymentTimeout * 1000L) + 500L;
                    if (plugin.processingLogs)
                        info("玩家 " + player.getName() + " 通过 " + type + " 下单点券 (￥" + moneyStr + ") 成功，订单号为 " + orderId);
                    Messages.commands__points__sent.tm(player,
                            Pair.of("%order_id%", orderId),
                            Pair.of("%money%", realMoney),
                            Pair.of("%original_money%", moneyStr),
                            Pair.of("%timeout%", paymentTimeout));
                    // 向玩家展示二维码地图
                    IMapSource source = IMapSource.fromUrl(plugin, resp.getPaymentUrl());
                    manager.requireScan(player, source, orderId, outdateTime, money -> {
                        // 支付成功操作，给予玩家点券
                        if (moneyStr.equals(realMoney)) {
                            info("玩家 " + player.getName() + " 通过 " + type + " 支付 ￥" + money + " 获得了 " + points + " 点券 --" + productName + " " + orderId);
                        } else {
                            info("玩家 " + player.getName() + " 通过 " + type + " 支付 ￥" + money + " (实付 ￥" + realMoney + ") 获得了 " + points + " 点券 --" + productName + " " + orderId);
                        }
                        plugin.getTradeDatabase().log(player, LocalDateTime.now(), type, realMoney, "points:" + points);
                        plugin.run(player, pointsCommands,
                                Pair.of("%points%", points),
                                Pair.of("%money%", money),
                                Pair.of("%money_round%", (int) Math.round(money)));
                    });
                });
                return true;
            }
            if (args.length >= 3 && "buy".equalsIgnoreCase(args[0])) {
                Map<String, String> params = collectArgs(args, 3);
                IPaymentManager manager;
                if (getBoolean(params, "mock", false)) {
                    if (!player.isOp()) {
                        return Messages.no_permission.tm(player);
                    }
                    manager = MockPaymentManager.inst();
                } else {
                    if (!PaymentAPI.inst().isConnected()) {
                        return Messages.not_connect.tm(player);
                    }
                    manager = PaymentsAndQRCodeManager.inst();
                }
                String shopId = args[1];
                ShopItem shop = ShopManager.inst().get(shopId);
                if (shop == null || (shop.permission != null && !player.hasPermission(shop.permission))) {
                    return Messages.commands__buy__not_found.tm(player);
                }
                if (shop.checkLimitation(plugin, player)) {
                    ActionProviders.run(plugin, player, shop.limitationDenyActions);
                    return true;
                }
                String type = args[2];
                if ("wechat".equalsIgnoreCase(type)) {
                    if (!shop.paymentWeChat) {
                        return Messages.commands__buy__disabled__wechat.tm(player);
                    }
                } else if ("alipay".equalsIgnoreCase(type)) {
                    if (!shop.paymentAlipay) {
                        return Messages.commands__buy__disabled__alipay.tm(player);
                    }
                } else if ("paypal".equalsIgnoreCase(type)) {
                    if (!shop.paymentPaypal) {
                        return Messages.commands__buy__disabled__paypal.tm(player);
                    }
                } else {
                    return Messages.commands__buy__unknown_type.tm(player);
                }
                String price;
                try {
                    price = shop.getPriceOrError(player);
                } catch (Exception e) {
                    return Messages.commands__buy__modifiers_error.tm(player, Pair.of("%error%", e.getMessage()));
                }
                if (manager.isProcess(player)) {
                    return Messages.commands__buy__processing.tm(player);
                }
                manager.putProcess(player, "buy:" + shop.id + ":" + type);
                String productName = random(shop.names, "商品");
                Messages.commands__buy__send.tm(player);
                manager.sendPacket(player, new PacketPluginRequestOrder(
                        player.getName(), type, productName, price, allowIncreasing
                ), resp -> {
                    String error = resp.getError();
                    if (!error.isEmpty()) {
                        warn("玩家 " + player.getName() + " 通过 " + type + " 下单 " + shop.display + " (" + shop.id + ") 失败: " + error);
                        // 下单失败时提示玩家
                        Errors.fromString(error).tm(player, Pair.of("%type%", error));
                        manager.remove(player);
                        return;
                    }
                    // 下单成功操作
                    String orderId = resp.getOrderId();
                    String realMoney = resp.getMoney();
                    long now = System.currentTimeMillis();
                    long outdateTime = now + (paymentTimeout * 1000L) + 500L;
                    if (plugin.processingLogs)
                        info("玩家 " + player.getName() + " 通过 " + type + " 下单商品 " + shop.display + " (" + shop.id + ") 成功，订单号为 " + orderId);
                    Messages.commands__buy__sent.tm(player,
                            Pair.of("%order_id%", orderId),
                            Pair.of("%display%", shop.display),
                            Pair.of("%money%", realMoney),
                            Pair.of("%original_money%", price),
                            Pair.of("%timeout%", paymentTimeout));
                    // 向玩家展示二维码地图
                    IMapSource source = IMapSource.fromUrl(plugin, resp.getPaymentUrl());
                    manager.requireScan(player, source, orderId, outdateTime, money -> {
                        // 支付成功操作，给予玩家奖励
                        if (price.equals(realMoney)) {
                            info("玩家 " + player.getName() + " 通过 " + type + " 支付 ￥" + money + " 购买了商品 " + shop.display + " (" + shop.id + ") --" + productName + " " + orderId);
                        } else {
                            info("玩家 " + player.getName() + " 通过 " + type + " 支付 ￥" + money + " (实付 ￥" + realMoney + ") 购买了商品 " + shop.display + " (" + shop.id + ") --" + productName + " " + orderId);
                        }
                        plugin.getTradeDatabase().log(player, LocalDateTime.now(), type, realMoney, "buy:" + shop.id);
                        switch (shop.limitationMode) {
                            case GLOBAL:
                                plugin.getBuyCountDatabase().addGlobalCount(shop, 1);
                                break;
                            case PER_PLAYER:
                                plugin.getBuyCountDatabase().addPlayerCount(player, shop, 1);
                                break;
                        }
                        plugin.run(player, shop.rewards);
                    });
                });
                return true;
            }
            if (args.length > 1 && "qrcode".equalsIgnoreCase(args[0]) && sender.isOp()) {
                StringBuilder content = new StringBuilder(args[1]);
                for (int i = 2; i < args.length; i++) {
                    content.append(" ").append(args[i]);
                }
                PaymentsAndQRCodeManager manager = PaymentsAndQRCodeManager.inst();
                long now = System.currentTimeMillis();
                long outdateTime = now + (paymentTimeout * 1000L) + 500L;
                IMapSource source = IMapSource.fromUrl(plugin, content.toString());
                manager.requireScan(player, source, null, outdateTime, null);
                return true;
            }
            if (args.length >= 1 && "map".equalsIgnoreCase(args[0]) && sender.isOp()) {
                if (args.length == 2) {
                    byte[] colors = readBase64(new File(plugin.getDataFolder(), args[1]), 16384);
                    if (colors == null) {
                        return Messages.commands__map__invalid.tm(player);
                    }
                    PaymentsAndQRCodeManager manager = PaymentsAndQRCodeManager.inst();
                    long now = System.currentTimeMillis();
                    long outdateTime = now + (paymentTimeout * 1000L) + 500L;
                    manager.requireScan(player, colors, null, outdateTime, null);
                    return Messages.commands__map__given.tm(player);
                }
                MapRenderer renderer = Utils.getMapRenderer(player.getItemInHand());
                if (renderer == null) {
                    return Messages.commands__map__not_found.tm(player);
                }
                byte[] colors = NMS.getColors(renderer);
                writeBase64(new File(plugin.getDataFolder(), "output.map"), colors);
                return Messages.commands__map__success.tm(player);
            }
            if (args.length >= 1 && "check".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.checkout.check")) {
                OfflinePlayer target;
                if (args.length == 2) {
                    if (!sender.hasPermission("sweet.checkout.check.other")) {
                        return Messages.no_permission.tm(player);
                    }
                    target = Util.getOfflinePlayer(args[1]).orElse(null);
                    if (target == null) {
                        return Messages.commands__check__no_player.tm(player);
                    }
                } else {
                    target = player;
                }
                Book book = plugin.getTradeDatabase().generateBook(target);
                AdventureUtil.of(player).openBook(book);
                return true;
            }
        }
        // (sender instanceof Player) end

        if (args.length >= 1 && "stats".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.checkout.stats")) {
            LocalDate startDate, endDate;
            if (args.length == 2) {
                startDate = parseDate(args[1]);
                if (startDate == null) {
                    return Messages.commands__stats__wrong_start_date.tm(sender);
                }
                endDate = clone(startDate).plusMonths(1);
            } else if (args.length == 3) {
                startDate = parseDate(args[1]);
                if (startDate == null) {
                    return Messages.commands__stats__wrong_start_date.tm(sender);
                }
                endDate = parseDate(args[2]);
                if (endDate == null) {
                    return Messages.commands__stats__wrong_end_date.tm(sender);
                }
            } else {
                return Messages.commands__stats__wrong_format.tm(sender);
            }
            String start = startDate.format(formatter);
            String end = endDate.format(formatter);
            List<TradeDatabase.Log> logs = plugin.getTradeDatabase().get(start, end);
            double sum = 0.0;
            Map<String, Double> sumPlatform = new HashMap<>();
            Map<UUID, Pair<String, Double>> sumPlayer = new HashMap<>();
            for (TradeDatabase.Log log : logs) {
                double money = Util.parseDouble(log.money).orElse(0.0);
                if (money <= 0) continue;
                // 按玩家求和
                Pair<String, Double> pair = sumPlayer.computeIfAbsent(log.uuid, k -> Pair.of("", 0.0));
                pair.key(log.name); // 玩家最后使用的名字
                pair.value(pair.value() + money);
                // 按平台求和
                sumPlatform.put(log.type, sumPlatform.getOrDefault(log.type, 0.0) + money);
                // 总求和
                sum += money;
            }
            List<Pair<String, Double>> pairs = Lists.newArrayList(sumPlayer.values());
            pairs.sort(Comparator.comparingDouble(Pair<String, Double>::value));
            Collections.reverse(pairs);
            LogBookManager manager = LogBookManager.inst();
            List<String> message = new ArrayList<>();

            String numberFormat = "%" + String.valueOf(statsTop).length() + "d";

            for (String s : Messages.commands__stats__success__messages.list(
                    Pair.of("%start_date%", start),
                    Pair.of("%end_date%", end),
                    Pair.of("%money%", formatMoney(sum)),
                    Pair.of("%top%", statsTop)
            )) {
                if (s.equals("platform sum")) {
                    for (Map.Entry<String, Double> entry : sumPlatform.entrySet()) {
                        String platform = manager.getPayment(entry.getKey());
                        String money = formatMoney(entry.getValue());
                        message.addAll(Messages.commands__stats__success__platform.list(
                                Pair.of("%platform%", platform),
                                Pair.of("%money%", money)
                        ));
                    }
                    continue;
                }
                if (s.equals("player sum")) {
                    for (int i = 0; i < statsTop; i++) {
                        String number = String.format(numberFormat, i + 1);
                        if (i >= pairs.size()) {
                            message.addAll(Messages.commands__stats__success__player_none.list(
                                    Pair.of("%number%", number)
                            ));
                        } else {
                            Pair<String, Double> pair = pairs.get(i);
                            String money = formatMoney(pair.value());
                            message.addAll(Messages.commands__stats__success__player_exists.list(
                                    Pair.of("%number%", number),
                                    Pair.of("%player%", pair.key()),
                                    Pair.of("%money%", money)
                            ));
                        }
                    }
                    continue;
                }
                message.add(s);
            }
            for (String s : message) {
                AdventureUtil.sendMessage(sender, s);
            }
            return true;
        }
        if (args.length == 1 && "rank".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.checkout.rank")) {
            List<Pair<String, Object>> replacements = new ArrayList<>();
            RankManager manager = RankManager.inst();
            for (int i = 1; i <= manager.getTop(); i++) {
                RankManager.Rank rank = manager.get(i);
                String name = rank == null
                        ? Messages.rank__not_found__player.str()
                        : Messages.rank__exist__player.str(Pair.of("%name%", rank.name));
                String money;
                if (rank == null) {
                    money = Messages.rank__not_found__money.str();
                } else {
                    String s = String.format("%.2f", rank.money).replace(".00", "");
                    money = Messages.rank__exist__money.str(Pair.of("%money%", s));
                }
                replacements.add(Pair.of("%player_" + i + "%", name));
                replacements.add(Pair.of("%money_" + i + "%", money));
            }
            return Messages.commands__rank__success.tm(sender, replacements);
        }
        if (args.length >= 4 && "log".equalsIgnoreCase(args[0]) && sender.isOp()) {
            OfflinePlayer player = Util.getOfflinePlayer(args[1]).orElse(null);
            if (player == null) {
                return Messages.commands__log__no_player.tm(sender);
            }
            String type = args[1];
            double money = Util.parseDouble(args[2]).orElse(0.0);
            if (money <= 0) {
                return Messages.commands__log__no_number.tm(sender);
            }
            String moneyStr = String.format("%.2f", money);
            String reason = consume(args, 3, " ");
            plugin.getTradeDatabase().log(player, LocalDateTime.now(), type, moneyStr, reason);
            return Messages.commands__log__success.tm(sender,
                    Pair.of("%name%", player.getName()),
                    Pair.of("%type%", type),
                    Pair.of("%money%", moneyStr),
                    Pair.of("%reason%", reason));
        }
        if (args.length == 2 && "reset".equalsIgnoreCase(args[0]) && sender.isOp()) {
            String shopId = args[1];
            ShopItem shop = ShopManager.inst().get(shopId);
            if (shop == null) {
                return Messages.commands__reset__not_found.tm(sender);
            }
            plugin.getBuyCountDatabase().reset(shop);
            return Messages.commands__reset__done.tm(sender);
        }
        if (args.length == 1 && "clear".equalsIgnoreCase(args[0]) && sender.isOp()) {
            plugin.getBuyCountDatabase().clearCaches();
            return Messages.commands__clear__success.tm(sender);
        }
        if (args.length >= 1 && "reload".equalsIgnoreCase(args[0]) && sender.isOp()) {
            if (args.length == 2 && "database".equalsIgnoreCase(args[1])) {
                plugin.options.database().reloadConfig();
                plugin.options.database().reconnect();
                return Messages.commands__reload_database.tm(sender);
            }
            plugin.reloadConfig();
            return Messages.commands__reload.tm(sender);
        }
        return (sender.isOp()
                ? Messages.commands__help__admin
                : Messages.commands__help__normal
        ).tm(sender);
    }

    private static String formatMoney(double money) {
        return String.format("%.2f", money).replace(".00", "");
    }

    private static LocalDate clone(LocalDate date) {
        return LocalDate.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    @Nullable
    private static LocalDate parseDate(String input) {
        String[] split = input.split("-", 3);
        if (split.length == 1) {
            Integer month = Util.parseInt(split[0]).orElse(null);
            if (month == null) return null;
            return LocalDate.of(LocalDate.now().getYear(), month, 1);
        }
        if (split.length == 2) {
            Integer year = Util.parseInt(split[0]).orElse(null);
            Integer month = Util.parseInt(split[1]).orElse(null);
            if (year == null || month == null) return null;
            return LocalDate.of(year, month, 1);
        }
        if (split.length == 3) {
            Integer year = Util.parseInt(split[0]).orElse(null);
            Integer month = Util.parseInt(split[1]).orElse(null);
            Integer date = Util.parseInt(split[2]).orElse(null);
            if (year == null || month == null || date == null) return null;
            return LocalDate.of(year, month, date);
        }
        return null;
    }

    private static final List<String> emptyList = Collections.emptyList();

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("sweet.checkout.points")) list.add("points");
            list.add("buy");
            if (sender.hasPermission("sweet.checkout.check")) list.add("check");
            if (sender.hasPermission("sweet.checkout.stats")) list.add("stats");
            if (sender.hasPermission("sweet.checkout.rank")) list.add("rank");
            if (sender.isOp()) {
                list.add("log");
                list.add("map");
                list.add("qrcode");
                list.add("reset");
                list.add("clear");
                list.add("reload");
            }
            return list;
        }
        if (args.length == 2) {
            if ("points".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.checkout.points")) {
                List<String> list = new ArrayList<>();
                if (useAlipay) list.add("alipay");
                if (useWeChat) list.add("wechat");
                if (usePaypal) list.add("paypal");
                return startsWith(list, args[1]);
            }
            if ("buy".equalsIgnoreCase(args[0])) {
                return startsWith(ShopManager.inst().shops(sender), args[1]);
            }
            if ("check".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.checkout.check.other")) {
                if (args[1].length() > 2) {
                    startsWith(Util.players.keySet(), args[1]);
                }
                return null;
            }
            if (sender.isOp()) {
                if ("log".equalsIgnoreCase(args[0])) {
                    if (args[1].length() > 2) {
                        startsWith(Util.players.keySet(), args[1]);
                    }
                    return null;
                }
                if ("map".equalsIgnoreCase(args[0])) {
                    List<String> fileList = new ArrayList<>();
                    String lower = args[1].toLowerCase();
                    File[] files = plugin.getDataFolder().listFiles((dir, name) -> name.endsWith(".map"));
                    if (files != null) for (File file : files) {
                        String name = file.getName();
                        if (name.toLowerCase().startsWith(lower)) {
                            fileList.add(name);
                        }
                    }
                    return fileList;
                }
                if ("reset".equalsIgnoreCase(args[0])) {
                    return startsWith(ShopManager.inst().shops(sender), args[1]);
                }
            }
        }
        if (args.length == 3) {
            if ("buy".equalsIgnoreCase(args[0])) {
                ShopItem shop = ShopManager.inst().get(args[1]);
                if (shop == null) {
                    return emptyList;
                }
                if (shop.permission != null && !sender.hasPermission(shop.permission)) {
                    return emptyList;
                }
                List<String> list = new ArrayList<>();
                if (shop.paymentAlipay) list.add("alipay");
                if (shop.paymentWeChat) list.add("wechat");
                if (shop.paymentPaypal) list.add("paypal");
                return startsWith(list, args[1]);
            }
        }
        return emptyList;
    }

    public List<String> startsWith(Collection<String> list, String s) {
        return startsWith(null, list, s);
    }

    public List<String> startsWith(String[] addition, Collection<String> list, String s) {
        String s1 = s.toLowerCase();
        List<String> stringList = new ArrayList<>(list);
        if (addition != null) stringList.addAll(0, Lists.newArrayList(addition));
        stringList.removeIf(it -> !it.toLowerCase().startsWith(s1));
        return stringList;
    }

    public static Map<String, String> collectArgs(String[] args, int startIndex) {
        Map<String, String> map = new HashMap<>();
        for (int i = startIndex; i < args.length; i++) {
            String text = args[i];
            if (text.startsWith("--")) {
                int j = text.indexOf('=');
                if (j != -1) {
                    map.put(text.substring(2, j), text.substring(j + 1));
                } else {
                    map.put(text.substring(2), "true");
                }
            }
        }
        return map;
    }

    public static boolean getBoolean(Map<String, String> params, String key, boolean def) {
        String text = params.getOrDefault(key, String.valueOf(def));
        if (text.equals("true") || text.equals("yes")) return true;
        if (text.equals("false") || text.equals("no")) return false;
        return def;
    }

    public static CommandMain inst() {
        return instanceOf(CommandMain.class);
    }
}
