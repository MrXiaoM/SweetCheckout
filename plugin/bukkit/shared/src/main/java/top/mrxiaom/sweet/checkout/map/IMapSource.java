package top.mrxiaom.sweet.checkout.map;

import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.qrcode.QRCode;
import top.mrxiaom.qrcode.enums.ErrorCorrectionLevel;
import top.mrxiaom.sweet.checkout.func.IPaymentManager;
import top.mrxiaom.sweet.checkout.func.PaymentsAndQRCodeManager;

import java.util.Base64;

public interface IMapSource {
    default byte[] generate(PaymentsAndQRCodeManager manager) {
        return generate((IPaymentManager) manager);
    }
    byte[] generate(IPaymentManager manager);

    static IMapSource fromUrl(BukkitPlugin plugin, String url) {
        if (url.startsWith("file:")) {
            return new MapFile(plugin.resolve(url.substring(5)));
        } else if (url.startsWith("base64:")) {
            String string = url.substring(7);
            byte[] bytes = Base64.getDecoder().decode(string);
            return new MapByteArray(bytes);
        } else {
            QRCode code = QRCode.create(url, ErrorCorrectionLevel.M);
            return new MapQRCode(code);
        }
    }
}
