package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.config.ShopProperties;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gera o recibo (comprovante <strong>não-fiscal</strong>) de um pagamento em PDF.
 * Os dados da loja vêm de {@link ShopProperties} ({@code shop.*}).
 */
@Service
@RequiredArgsConstructor
public class PaymentReceiptService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ShopProperties shop;

    public byte[] generate(Payment payment) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);

            document.add(paragraph(blankToDash(shop.name()), titleFont));
            document.add(paragraph(shopLine(), bodyFont));
            document.add(spacer());

            document.add(paragraph("RECIBO DE PAGAMENTO Nº " + payment.getId(), headingFont));
            document.add(spacer());

            document.add(paragraph("Valor: " + formatMoney(payment.getAmount()), bodyFont));
            document.add(paragraph("Forma de pagamento: " + describeMethod(payment), bodyFont));
            document.add(paragraph("Situação: " + payment.getStatus().getDescription(), bodyFont));
            document.add(paragraph("Data: " + formatDate(paymentDate(payment)), bodyFont));
            if (payment.getDescription() != null && !payment.getDescription().isBlank()) {
                document.add(paragraph("Observações: " + payment.getDescription(), bodyFont));
            }
            if (payment.getPayerName() != null && !payment.getPayerName().isBlank()) {
                document.add(paragraph("Pagador: " + payment.getPayerName()
                        + (payment.getPayerDocument() != null && !payment.getPayerDocument().isBlank()
                        ? " (" + payment.getPayerDocument() + ")" : ""), bodyFont));
            }
            document.add(spacer());

            document.add(paragraph("Ordem de reparo", headingFont));
            document.add(paragraph("Nº: " + payment.getRepairOrder().getId()
                    + " — " + payment.getRepairOrder().getStatus().getDescription(), bodyFont));
            document.add(paragraph("Cliente: " + payment.getRepairOrder().getCustomer().getName()
                    + " — " + payment.getRepairOrder().getCustomer().getPhone(), bodyFont));
            document.add(paragraph("Aparelho: " + payment.getRepairOrder().getDevice().getModel()
                    + " (série " + payment.getRepairOrder().getDevice().getSerialNumber() + ")", bodyFont));
            document.add(spacer());

            document.add(paragraph("Emitido em " + formatDate(LocalDateTime.now())
                    + ". Este documento não possui valor fiscal.", footerFont));

            document.close();
        } catch (DocumentException exception) {
            throw new IllegalStateException("Falha ao gerar o recibo do pagamento " + payment.getId(), exception);
        }

        return output.toByteArray();
    }

    private String shopLine() {
        StringBuilder line = new StringBuilder();
        appendIfPresent(line, shop.document(), "CNPJ: ");
        appendIfPresent(line, shop.address(), "");
        appendIfPresent(line, shop.phone(), "Tel: ");
        appendIfPresent(line, shop.email(), "");
        return line.isEmpty() ? "-" : line.toString();
    }

    private static void appendIfPresent(StringBuilder builder, String value, String label) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append("  |  ");
        }
        builder.append(label).append(value.trim());
    }

    private static String describeMethod(Payment payment) {
        String method = payment.getMethod().getDescription();
        if (payment.getMethod() == PaymentMethod.CARD && payment.getInstallments() != null && payment.getInstallments() > 1) {
            return method + " em " + payment.getInstallments() + "x";
        }
        return method;
    }

    private static LocalDateTime paymentDate(Payment payment) {
        return payment.getPaidAt() != null ? payment.getPaidAt() : payment.getCreatedAt();
    }

    private static String formatDate(LocalDateTime value) {
        return value == null ? "-" : DATE_TIME.format(value);
    }

    private static String formatMoney(BigDecimal value) {
        return value == null ? "-" : "R$ " + value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static Paragraph paragraph(String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        return paragraph;
    }

    private static Paragraph spacer() {
        return new Paragraph(" ");
    }
}
