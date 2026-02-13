package top.mrxiaom.sweet.checkout.map;

import top.mrxiaom.qrcode.QRCode;
import top.mrxiaom.sweet.checkout.func.IPaymentManager;

import java.util.Arrays;

public class MapQRCode implements IMapSource {
    private final QRCode code;

    public MapQRCode(QRCode code) {
        this.code = code;
    }

    @Deprecated
    public MapQRCode(top.mrxiaom.sweet.checkout.libs.qrcode.QRCode code) {
        this.code = code.impl;
    }

    public QRCode getCode() {
        return code;
    }

    @Override
    public byte[] generate(IPaymentManager manager) {
        int widthAndHeight = code.getModuleCount();
        // 如果二维码放大2倍，都比128还小，那应该缩放2倍显示
        boolean scaling = widthAndHeight * 2 < 128;
        if (scaling) widthAndHeight *= 2;
        // 左上角起始坐标
        int start = (128 - widthAndHeight) / 2;
        byte[] colors = new byte[16384];
        // 先把地图填满亮色（背景色）
        if (manager.getMapLightPattern() != null) {
            System.arraycopy(manager.getMapLightPattern(), 0, colors, 0, colors.length);
        } else {
            Arrays.fill(colors, manager.getMapLightColor());
        }
        byte mapDarkColor = manager.getMapDarkColor();
        byte[] mapDarkPattern = manager.getMapDarkPattern();
        for (int z = 0; z < widthAndHeight; z++) {
            for (int x = 0; x < widthAndHeight; x++) {
                // 再画上暗色（前景色）
                if (scaling ? code.isDark(z / 2, x / 2) : code.isDark(z, x)) {
                    int index = (start + x) + 128 * (start + z);
                    if (mapDarkPattern != null) {
                        colors[index] = mapDarkPattern[index];
                    } else {
                        colors[index] = mapDarkColor;
                    }
                }
            }
        }
        return colors;
    }
}
