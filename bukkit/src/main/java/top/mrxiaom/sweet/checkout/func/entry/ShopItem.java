package top.mrxiaom.sweet.checkout.func.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.checkout.SweetCheckout;

import java.util.List;

public class ShopItem {
    public final String id;
    public final String display;
    public final List<String> names;
    public final boolean paymentAlipay, paymentWeChat;
    public final String price;
    public final List<String> rewards;

    public ShopItem(String id, String display, List<String> names, boolean paymentAlipay, boolean paymentWeChat, String price, List<String> rewards) {
        this.id = id;
        this.display = display;
        this.names = names;
        this.paymentAlipay = paymentAlipay;
        this.paymentWeChat = paymentWeChat;
        this.price = price;
        this.rewards = rewards;
    }

    @Nullable
    public static ShopItem load(SweetCheckout plugin, ConfigurationSection config, String id) {
        boolean paymentAlipay = config.getBoolean("payment.alipay");
        boolean paymentWeChat = config.getBoolean("payment.wechat");
        String display = config.getString("display", id);
        double price = config.getDouble("price");
        if (price < 0.01) {
            plugin.getLogger().warning("[shops] 加载 " + id + " 失败: 价格输入错误");
            return null;
        }
        List<String> names = config.getStringList("names");
        if (names.isEmpty()) {
            plugin.getLogger().warning("[shops] 加载 " + id + " 失败: 商品名称为空");
            return null;
        }
        List<String> rewards = config.getStringList("rewards");
        if (rewards.isEmpty()) {
            plugin.getLogger().warning("[shops] 加载 " + id + " 失败: 奖励命令列表为空");
            return null;
        }
        return new ShopItem(id, display, names, paymentAlipay, paymentWeChat, String.format("%.2f", price), rewards);
    }
}
