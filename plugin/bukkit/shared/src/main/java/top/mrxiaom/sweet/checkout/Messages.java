package top.mrxiaom.sweet.checkout;

import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import java.util.List;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

@Language(prefix = "messages.")
public enum Messages implements IHolderAccessor {
    not_connect("&e插件未连接到后端，请联系服务器管理员"),

    market__not_allow("&e不允许上架该商品到全球市场"),

    player__not_online("&e玩家不在线&7 (或不存在)"),
    player__only("该命令只能由玩家执行"),

    commands__reload("&a配置文件已重载"),
    commands__reload_database("&a已重新连接到数据库"),
    commands__points__disabled__wechat("&e管理员已禁用微信支付"),
    commands__points__disabled__alipay("&e管理员已禁用支付宝支付"),
    commands__points__disabled__paypal("&e管理员已禁用 Paypal 支付"),
    commands__points__unknown_type("&e未知支付类型"),
    commands__points__processing("&e请先完成你正在进行的订单"),
    commands__points__invalid_money("&e请输入正确的金额"),
    commands__points__modifiers_error("&e订单数据修饰器执行错误，请联系服务器管理员 &7(%error%)"),
    commands__points__send("&f正在请求…"),
    commands__points__sent("",
            "&a&l下单成功&r",
            "&f 订单号: &e&l%order_id%&r",
            "&f 金额: &c&l￥%money%&r",
            "&a &l在支付时如果要求输入金额",
            "&a &l请务必输入正确的金额以确保成功支付",
            "&7&m-----------------------------&r",
            "&e 请在&b %timeout% &e秒内扫码支付。",
            "&e 支付期间请不要离开服务器，完成后，",
            "&e 奖励将发放到你的账户，",
            "&7&m-----------------------------&r",
            "&f 按下&c&lＱ&r&f键取消订单",
            "&7&m-----------------------------&r",
            "&b 支付此订单，即代表你已同意服务器的相关条款",
            "&7&m-----------------------------&r",
            "", ""),
    commands__buy__not_found("&e无效的商品ID"),
    commands__buy__disabled__wechat("&e管理员已禁用微信支付"),
    commands__buy__disabled__alipay("&e管理员已禁用支付宝支付"),
    commands__buy__disabled__paypal("&e管理员已禁用 Paypal 支付"),
    commands__buy__unknown_type("&e未知支付类型"),
    commands__buy__modifiers_error("&e订单数据修饰器执行错误，请联系服务器管理员 &7(%error%)"),
    commands__buy__processing("&e请先完成你正在进行的订单"),
    commands__buy__send("&f正在请求…"),
    commands__buy__sent("",
            "&a&l下单成功&r",
            "&f 订单号: &e&l%order_id%&r",
            "&f 商品: &e&l%display%&r",
            "&f 金额: &c&l￥%money%&r",
            "&a &l在支付时如果要求输入金额",
            "&a &l请务必输入正确的金额以确保成功支付",
            "&7&m-----------------------------&r",
            "&e 请在&b %timeout% &e秒内扫码支付。",
            "&e 支付期间请不要离开服务器，完成后，",
            "&e 奖励将发放到你的账户，",
            "&7&m-----------------------------&r",
            "&f 按下&c&lＱ&r&f键取消订单",
            "&7&m-----------------------------&r",
            "&b 支付此订单，即代表你已同意服务器的相关条款",
            "&7&m-----------------------------&r",
            "", ""),
    commands__map__not_found("&e请手持一张有效的地图&7（注：1.13 以下无法使用导出功能）"),
    commands__map__success("&a地图数据已导出到 output.map 文件"),
    commands__map__invalid("&e该文件不是有效的地图文件"),
    commands__map__given("&e已开启二维码扫描模拟"),
    cancelled("已取消付款，原因: %reason%"),

    commands__rank__success("",
            "&e&l充值排行榜&r",
            " &c1. %player_1%    %money_1%&r",
            " &c2. %player_2%    %money_2%&r",
            " &c3. %player_3%    %money_3%&r",
            " &c4. %player_4%    %money_4%&r",
            " &c5. %player_5%    %money_5%&r",
            " &c6. %player_6%    %money_6%&r",
            " &c7. %player_7%    %money_7%&r",
            " &c8. %player_8%    %money_8%&r",
            " &c9. %player_9%    %money_9%&r",
            "&c10. %player_10%    %money_10%&r",
            "&b排行榜每隔&e1&b分钟刷新一次", ""),

    rank__exist__player("&e%name%"),
    rank__exist__money("&f&l￥%money%"),
    rank__not_found__player("&7虚位以待"),
    rank__not_found__money(""),

    commands__log__no_player("&e找不到该玩家"),
    commands__log__no_number("&e你输入的不是一个正确的金额"),
    commands__log__success("&a你已为玩家&e %name% &a添加一条交易记录，使用&e %type% &a支付 &e￥%money%&a，理由为&e %reason%"),

    commands__stats__wrong_start_date("&e输入的起始时间格式有误，应为",
            "&f  月",
            "&f  年-月",
            "&f  年-月-日",
            "&e三种格式中的其中一种"),
    commands__stats__wrong_end_date("&e输入的结束时间格式有误，应为",
            "&f  月",
            "&f  年-月",
            "&f  年-月-日",
            "&e三种格式中的其中一种"),
    commands__stats__wrong_format("&e命令格式不正确，应为",
            "&f  /checkout stats <起始时间>",
            "&f  /checkout stats <起始时间> <结束时间>",
            "&e两种格式中的其中一种"),
    commands__stats__success__messages("",
            "&3============ &b交易统计 &3============",
            "&f  时间:&e %start_date%&f ->&e %end_date%",
            "&f  共收款:&e ￥%money%",
            "platform sum",
            "&f  贡献前&e%top%&f的玩家如下:",
            "player sum",
            "&3============ &b交易统计 &3============",
            ""),
    commands__stats__success__platform("&f  来自&e %platform% &f共&e ￥%money%"),
    commands__stats__success__player_exists("&c  %number%.&b %player% &e￥%money%"),
    commands__stats__success__player_none("&c  %number%.&7 虚位以待"),

    commands__reset__not_found("&e无效的商品ID"),
    commands__reset__done("&a商品限购数据已重置完成，详细信息请查阅控制台"),

    commands__clear__success("&a已清空数据库缓存"),

    commands__check__no_player("&e找不到该玩家"),

    no_permission("&c你没有进行此操作的权限"),

    commands__help__normal(
            "&e&lSweetCheckout&r &b支付系统",
            "&f/checkout points <类型> <金额> &7通过微信(wechat)或支付宝(alipay)下单指定金额的点券",
            "&f/checkout buy <商品ID> <类型> &7通过微信(wechat)或支付宝(alipay)下单指定商品",
            "&f/checkout check &7查看充值记录",
            "&f/checkout rank &7查看充值排行榜",
            ""),
    commands__help__admin(
            "&e&lSweetCheckout&r &b支付系统",
            "&f/checkout points <类型> <金额> [--mock] &7通过微信(wechat)或支付宝(alipay)下单指定金额的点券",
            "&f/checkout buy <商品ID> <类型> [--mock] &7通过微信(wechat)或支付宝(alipay)下单指定商品",
            "&f/checkout check [玩家] &7查看自己或某人的充值记录",
            "&f/checkout rank &7查看充值排行榜",
            "&f/checkout stats <起始时间> [结束时间] &7查看指定时间段内的交易统计信息",
            "    &7其时间格式可用 &e月&7, &e年-月&7, &e年-月-日&7 三种格式",
            "&f/checkout map [文件名] &7不输入文件名时，将手中的地图保存到&f output.map &7文件；输入文件名时，通过地图预览文件以测试文件是否正常",
            "&f/checkout log <玩家> <类型> <金额> <原因...> &7手动添加充值记录",
            "&f/checkout reset <商品ID> &7重置某个商品的限购数量和周期时间",
            "&f/checkout clear &7清空数据缓存。如果你有多个服区，建议在执行 reset 后在其它服区执行该命令",
            "&f/checkout reload database &7重新连接数据库",
            "&f/checkout reload &7重载配置文件",
            ""),


    ;

    Messages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }

    Messages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }

    Messages(List<String> defaultValue) {
        holder = wrap(this, defaultValue);
    }

    private final LanguageEnumAutoHolder<Messages> holder;

    public LanguageEnumAutoHolder<Messages> holder() {
        return holder;
    }
}
