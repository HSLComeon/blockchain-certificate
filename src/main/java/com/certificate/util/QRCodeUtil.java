package com.certificate.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码工具类
 */
public class QRCodeUtil {

    /**
     * 生成QR码图片的Base64字符串
     * @param content 内容
     * @param width 宽度
     * @param height 高度
     * @return Base64编码的图片
     */
    public static String generateQRCodeBase64(String content, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }

        // 添加logo
        // bufferedImage = addLogo(bufferedImage, logoPath);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);

        byte[] bytes = baos.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 添加logo
     * @param qrImage 二维码图片
     * @param logoPath logo路径
     * @return 带logo的二维码图片
     */
    private static BufferedImage addLogo(BufferedImage qrImage, String logoPath) throws IOException {
        if (logoPath == null || logoPath.isEmpty()) {
            return qrImage;
        }

        // 读取logo
        Image logoImage = ImageIO.read(QRCodeUtil.class.getResourceAsStream(logoPath));

        // 计算logo大小（二维码的1/5）
        int logoWidth = qrImage.getWidth() / 5;
        int logoHeight = qrImage.getHeight() / 5;

        // 计算logo位置（居中）
        int x = (qrImage.getWidth() - logoWidth) / 2;
        int y = (qrImage.getHeight() - logoHeight) / 2;

        // 绘制logo
        Graphics2D graphics = qrImage.createGraphics();
        graphics.drawImage(logoImage, x, y, logoWidth, logoHeight, null);
        graphics.dispose();

        return qrImage;
    }
}