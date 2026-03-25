package top.mrxiaom.sweet.checkout;

import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.paper.PaperFactory;
import top.mrxiaom.pluginbase.resolver.DefaultLibraryResolver;
import top.mrxiaom.pluginbase.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;
import top.mrxiaom.sweet.checkout.api.PaymentClient;
import top.mrxiaom.sweet.checkout.database.BuyCountDatabase;
import top.mrxiaom.sweet.checkout.database.TradeDatabase;
import top.mrxiaom.sweet.checkout.func.PaymentAPI;
import top.mrxiaom.sweet.checkout.nms.NMS;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public abstract class PluginCommon extends BukkitPlugin {
    public static PluginCommon getInstance() {
        return (PluginCommon) BukkitPlugin.getInstance();
    }

    public PluginCommon() throws Exception {
        super(options()
                .bungee(false)
                .adventure(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("top.mrxiaom.sweet.checkout.libs")
        );
        scheduler = new FoliaLibScheduler(this);

        info("正在检查依赖库状态");
        File librariesDir = ClassLoaderWrapper.isSupportLibraryLoader
                ? new File("libraries")
                : new File(this.getDataFolder(), "libraries");
        DefaultLibraryResolver resolver = new DefaultLibraryResolver(getLogger(), librariesDir);

        resolver.addResolvedLibrary(BuildConstants.RESOLVED_LIBRARIES);
        if (Util.isPresent("top.mrxiaom.sweet.checkout.backend.BukkitMain")) {
            if (!Util.isPresent("org.apache.commons.io.FileUtils")) {
                resolver.addResolvedLibrary("commons-io/commons-io/2.17.0/commons-io-2.17.0.jar");
            }
        }

        List<URL> libraries = resolver.doResolve();
        info("正在添加 " + libraries.size() + " 个依赖库到类加载器");
        for (URL library : libraries) {
            this.classLoader.addURL(library);
        }
    }

    @Override
    public @NotNull ItemEditor initItemEditor() {
        return PaperFactory.createItemEditor();
    }

    @Override
    public @NotNull InventoryFactory initInventoryFactory() {
        return PaperFactory.createInventoryFactory();
    }

    public boolean processingLogs;
    private TradeDatabase tradeDatabase;
    private BuyCountDatabase buyCountDatabase;

    public TradeDatabase getTradeDatabase() {
        return tradeDatabase;
    }

    public BuyCountDatabase getBuyCountDatabase() {
        return buyCountDatabase;
    }

    public abstract PaymentClient handlePaymentReload(PaymentAPI parent, @Nullable String url) throws URISyntaxException;

    @Override
    public Class<?> getConstructorType() {
        return PluginCommon.class;
    }

    @Override
    protected void beforeLoad() {
        MinecraftVersion.replaceLogger(getLogger());
        MinecraftVersion.disableUpdateCheck();
        MinecraftVersion.disableBStats();
        MinecraftVersion.getVersion();
    }

    @Override
    protected void beforeEnable() {
        if (!NMS.init()) {
            throw new IllegalStateException("不支持的游戏版本 " + MinecraftVersion.getVersion().name());
        }
        if (NMS.isUnknownVersion()) {
            warn("你的服务端版本看起来不受本插件支持，正在尝试使用最后一个支持的版本的实现，如果插件无法正常使用，请报告给插件作者");
        }
        LanguageManager.inst()
                .setLangFile("messages.yml")
                .register(Messages.class, Messages::holder)
                .register(Errors.class, Errors::holder)
                .register(CancelReasons.class, CancelReasons::holder);
        options.registerDatabase(
                tradeDatabase = new TradeDatabase(this),
                buyCountDatabase = new BuyCountDatabase(this)
        );
    }

    @Override
    protected void afterEnable() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Placeholders placeholders;
            try {
                placeholders = new PlaceholdersPersist(this);
                placeholders.persist();
            } catch (Throwable ignored) {
                placeholders = new Placeholders(this);
            }
            placeholders.register();
        }
        getLogger().info("SweetCheckout 加载完毕");
    }

    @SafeVarargs
    public final void run(Player player, List<IAction> commands, Pair<String, Object>... replacements) {
        List<Pair<String, Object>> list = replacements.length == 0 ? null : Lists.newArrayList(replacements);
        run0(this, player, commands, list, 0);
    }

    private static void run0(BukkitPlugin plugin, Player player, List<IAction> actions, List<Pair<String, Object>> replacements, int startIndex) {
        for (int i = startIndex; i < actions.size(); i++) {
            IAction action = actions.get(i);
            action.run(player, replacements);
            long delay = action.delayAfterRun();
            if (delay > 0) {
                int index = i + 1;
                plugin.getScheduler().runTaskLater(() -> run0(plugin, player, actions, replacements, index), delay);
                return;
            }
        }
    }
}
