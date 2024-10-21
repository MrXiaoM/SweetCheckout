package top.mrxiaom.sweet.checkout.backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.java_websocket.WebSocketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ConsoleMain extends SimpleTerminalConsole {
    @Getter
    private static PaymentServer server;
    @Getter
    private static Configuration config;
    @Getter
    private boolean running = true;
    @Getter
    private static Logger logger = LoggerFactory.getLogger("Server");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static void main(String[] args) {
        logger.info("正在运行 SweetCheckout 后端");
        reloadConfig();
        Configurator.setLevel(WebSocketImpl.class, Level.ALL);
        server = new PaymentServer(logger, config.getPort());
        server.start();

        new ConsoleMain().start();
    }

    public static void reloadConfig() {
        try {
            File file = new File("config.json");
            if (file.exists()) {
                String configRaw = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                config = gson.fromJson(configRaw, Configuration.class);
            } else {
                config = new Configuration();
            }
            String configRaw = gson.toJson(config);
            FileUtils.writeStringToFile(file, configRaw, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("加载配置文件时出现异常", e);
        }
    }

    @Override
    protected void runCommand(String s) {
        if ("reload".equals(s)) {
            logger.info("配置文件已重载.");
            return;
        }
        if ("stop".equals(s)) {
            running = false;
            logger.info("再见.");
            System.exit(0);
            return;
        }
        logger.info("未知命令.");
    }

    @Override
    protected void shutdown() {

    }
}
