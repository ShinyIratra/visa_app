package app.visa.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeQrService {
    @Value("${app.frontend-base-url}")
    private String frontEndBaseUrl;

    // Ito no miasa raha tonga dia image/png no tiana averin'ny navigateur 
    // (aleoko ito amzay tsy mivaky loha css)
    public byte[] genererCodeQrDemandeBytes(String numeroDemande) {
        String url = frontEndBaseUrl + "/details/" + numeroDemande;
        return genererCodeQrBytes(url);
    }

    // ito no miasa raha tiana apoitra anaty balise img le izy
    public String genererCodeQrDemandeString(String numeroDemande) {
        String url = frontEndBaseUrl + "/details/" + numeroDemande;
        return "data:image/png;base64," + genererCodeQrString(url);
    }

    private String genererCodeQrString(String text) {
        return Base64.getEncoder().encodeToString(genererCodeQrBytes(text));
    }

    private byte[] genererCodeQrBytes(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    text,
                    BarcodeFormat.QR_CODE,
                    300,
                    300,
                    hints
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] qrBytes = outputStream.toByteArray();

            return qrBytes;

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération QR Code", e);
        }
    }
}