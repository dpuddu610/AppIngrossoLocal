package com.ingrosso.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.oned.EAN8Writer;
import com.google.zxing.oned.Code128Writer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class BarcodeUtil {
    private static final Logger logger = LoggerFactory.getLogger(BarcodeUtil.class);

    private BarcodeUtil() {}

    public static Image generateEAN13(String code, int width, int height) {
        return generateBarcode(code, BarcodeFormat.EAN_13, width, height);
    }

    public static Image generateEAN8(String code, int width, int height) {
        return generateBarcode(code, BarcodeFormat.EAN_8, width, height);
    }

    public static Image generateCode128(String code, int width, int height) {
        return generateBarcode(code, BarcodeFormat.CODE_128, width, height);
    }

    public static Image generateBarcode(String code, BarcodeFormat format, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);

            Writer writer;
            switch (format) {
                case EAN_13 -> writer = new EAN13Writer();
                case EAN_8 -> writer = new EAN8Writer();
                default -> writer = new Code128Writer();
            }

            BitMatrix bitMatrix = writer.encode(code, format, width, height, hints);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (WriterException e) {
            logger.error("Error generating barcode: {}", e.getMessage());
            return null;
        }
    }

    public static byte[] generateBarcodeBytes(String code, BarcodeFormat format, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(code, format, width, height, hints);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | IOException e) {
            logger.error("Error generating barcode bytes: {}", e.getMessage());
            return null;
        }
    }

    public static String readBarcode(byte[] imageData) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
            return readBarcode(bufferedImage);
        } catch (IOException e) {
            logger.error("Error reading barcode from bytes: {}", e.getMessage());
            return null;
        }
    }

    public static String readBarcode(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            Result result = new MultiFormatReader().decode(bitmap, hints);
            return result.getText();
        } catch (NotFoundException e) {
            logger.debug("No barcode found in image");
            return null;
        }
    }

    public static boolean isValidEAN13(String code) {
        if (code == null || code.length() != 13) return false;
        if (!code.matches("\\d+")) return false;
        return validateCheckDigit(code);
    }

    public static boolean isValidEAN8(String code) {
        if (code == null || code.length() != 8) return false;
        if (!code.matches("\\d+")) return false;
        return validateCheckDigit(code);
    }

    private static boolean validateCheckDigit(String code) {
        int sum = 0;
        for (int i = 0; i < code.length() - 1; i++) {
            int digit = Character.getNumericValue(code.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit == Character.getNumericValue(code.charAt(code.length() - 1));
    }

    public static String calculateEAN13CheckDigit(String code12) {
        if (code12 == null || code12.length() != 12 || !code12.matches("\\d+")) {
            return null;
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(code12.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return code12 + checkDigit;
    }

    public static String calculateEAN8CheckDigit(String code7) {
        if (code7 == null || code7.length() != 7 || !code7.matches("\\d+")) {
            return null;
        }
        int sum = 0;
        for (int i = 0; i < 7; i++) {
            int digit = Character.getNumericValue(code7.charAt(i));
            sum += (i % 2 == 0) ? digit * 3 : digit;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return code7 + checkDigit;
    }
}
