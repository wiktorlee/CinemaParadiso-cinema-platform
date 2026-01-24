package pl.cinemaparadiso.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class QRCodeService {

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    private static final String IMAGE_FORMAT = "PNG";

    @Value("${qr.code.base-url:http://localhost:8080}")
    private String baseUrl;

    public byte[] generateQRCodeImage(String data) throws WriterException, IOException {
        log.debug("Generowanie QR code dla danych: {}", data);
        
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
            data, 
            BarcodeFormat.QR_CODE, 
            QR_CODE_WIDTH, 
            QR_CODE_HEIGHT, 
            hints
        );
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
        byte[] qrCodeImage = outputStream.toByteArray();
        
        log.debug("QR code wygenerowany pomyślnie, rozmiar: {} bajtów", qrCodeImage.length);
        return qrCodeImage;
    }

    public byte[] generateQRCodeWithURL(String token) throws WriterException, IOException {
        String url = baseUrl + "/verify-ticket.html?token=" + token;
        log.info("Generowanie QR code z URL: {}", url);
        return generateQRCodeImage(url);
    }

    public byte[] generateQRCodeWithToken(String token) throws WriterException, IOException {
        return generateQRCodeImage(token);
    }
}

