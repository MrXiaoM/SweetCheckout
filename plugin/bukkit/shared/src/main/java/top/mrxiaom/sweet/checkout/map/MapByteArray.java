package top.mrxiaom.sweet.checkout.map;

import top.mrxiaom.sweet.checkout.PluginCommon;
import top.mrxiaom.sweet.checkout.func.IPaymentManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

public class MapByteArray implements IMapSource {
    private final byte[] bytes;

    public MapByteArray(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public byte[] generate(IPaymentManager manager) {
        try (InputStream input = new ByteArrayInputStream(bytes)) {
            BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.drawImage(ImageIO.read(input), 0, 0, 128, 128, null);
            graphics.dispose();
            return MapConverter.imageToBytes(image);
        } catch (Throwable t) {
            PluginCommon.getInstance().warn("读取字节流时出现异常", t);
            byte[] colors = new byte[16384];
            Arrays.fill(colors, manager.getMapLightColor());
            return colors;
        }
    }
}
