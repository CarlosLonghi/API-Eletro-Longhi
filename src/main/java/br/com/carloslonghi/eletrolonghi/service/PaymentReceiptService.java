package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.config.ShopProperties;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gera o recibo (comprovante <strong>não-fiscal</strong>) de um pagamento em PDF.
 * Os dados da loja vêm de {@link ShopProperties} ({@code shop.*}).
 *
 * <p>Layout deliberadamente compacto e em preto e branco (sem preenchimentos coloridos),
 * pensado para impressão econômica em papel comum.
 */
@Service
@RequiredArgsConstructor
public class PaymentReceiptService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color BLACK = Color.BLACK;
    private static final Color GRAY = new Color(90, 90, 90);
    private static final Color BORDER = new Color(190, 190, 190);
    private static final Color PANEL = new Color(240, 240, 240);

    private final ShopProperties shop;

    public byte[] generate(Payment payment) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 32, 32, 26, 26);

        try {
            PdfWriter.getInstance(document, output);
            document.open();

            document.add(header());
            document.add(rule(0.75f));
            document.add(spacer(6f));

            document.add(titleBar(payment));
            document.add(spacer(6f));

            document.add(amountPanel(payment));
            document.add(spacer(6f));

            document.add(twoColumns(paymentCard(payment), repairOrderCard(payment.getRepairOrder())));

            if (hasText(payment.getPayerName()) || hasText(payment.getDescription())) {
                document.add(spacer(5f));
                document.add(notesCard(payment));
            }

            document.add(spacer(10f));
            document.add(footer());

            document.close();
        } catch (DocumentException exception) {
            throw new IllegalStateException("Falha ao gerar o recibo do pagamento " + payment.getId(), exception);
        }

        return output.toByteArray();
    }

    // ---------------------------------------------------------------- header

    private PdfPTable header() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{2.2f, 1f});
        } catch (DocumentException ignored) {
            // larguras fixas e válidas, nunca lança
        }

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.addElement(new Phrase(blankToDash(shop.name()), font(13, Font.BOLD, BLACK)));
        String contact = shopLine();
        if (!contact.equals("-")) {
            left.addElement(new Phrase(contact, font(7.5f, Font.NORMAL, GRAY)));
        }
        table.addCell(left);

        PdfPCell right = new PdfPCell(new Phrase("COMPROVANTE DE PAGAMENTO", font(8, Font.BOLD, GRAY)));
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(right);

        return table;
    }

    private PdfPTable rule(float thickness) {
        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(thickness);
        cell.setBackgroundColor(BLACK);
        cell.setBorder(Rectangle.NO_BORDER);
        rule.addCell(cell);
        return rule;
    }

    // ------------------------------------------------------------ title bar

    private PdfPTable titleBar(Payment payment) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{2f, 1f});
        } catch (DocumentException ignored) {
            // larguras fixas e válidas, nunca lança
        }

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setVerticalAlignment(Element.ALIGN_MIDDLE);
        left.addElement(new Phrase("Recibo de Pagamento", font(12, Font.BOLD, BLACK)));
        left.addElement(new Phrase("Nº " + payment.getId(), font(8, Font.NORMAL, GRAY)));
        table.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.addElement(statusBadge(payment.getStatus().getDescription()));
        table.addCell(right);

        return table;
    }

    private PdfPTable statusBadge(String description) {
        PdfPTable badge = new PdfPTable(1);
        badge.setWidthPercentage(42);
        badge.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell cell = new PdfPCell(new Phrase(description.toUpperCase(), font(8, Font.BOLD, BLACK)));
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BLACK);
        cell.setBorderWidth(0.75f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        badge.addCell(cell);
        return badge;
    }

    // --------------------------------------------------------- amount panel

    private PdfPTable amountPanel(Payment payment) {
        PdfPTable outer = new PdfPTable(1);
        outer.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER);
        cell.setPadding(8f);

        cell.addElement(new Phrase("VALOR DO PAGAMENTO", font(7.5f, Font.BOLD, GRAY)));
        cell.addElement(new Phrase(formatMoney(payment.getAmount()), font(18, Font.BOLD, BLACK)));

        String secondLine = describeMethod(payment) + "  •  " + dateLabel(payment) + " " + formatDate(paymentDate(payment));
        cell.addElement(new Phrase(secondLine, font(8, Font.NORMAL, GRAY)));

        outer.addCell(cell);
        return outer;
    }

    private static String dateLabel(Payment payment) {
        return payment.getStatus() == PaymentStatus.APPROVED && payment.getPaidAt() != null ? "pago em" : "registrado em";
    }

    // ---------------------------------------------------------------- cards

    private PdfPTable twoColumns(PdfPTable left, PdfPTable right) {
        PdfPTable row = new PdfPTable(2);
        row.setWidthPercentage(100);
        try {
            row.setWidths(new float[]{1f, 1f});
        } catch (DocumentException ignored) {
            // larguras fixas e válidas, nunca lança
        }

        PdfPCell leftCell = new PdfPCell(left);
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPadding(0f);
        leftCell.setPaddingRight(4f);
        row.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell(right);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPadding(0f);
        rightCell.setPaddingLeft(4f);
        row.addCell(rightCell);

        return row;
    }

    private PdfPTable paymentCard(Payment payment) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Forma de pagamento", describeMethod(payment)});
        rows.add(new String[]{"Situação", payment.getStatus().getDescription()});
        rows.add(new String[]{dateLabel(payment).substring(0, 1).toUpperCase() + dateLabel(payment).substring(1),
                formatDate(paymentDate(payment))});
        if (hasText(payment.getGatewayPaymentId())) {
            rows.add(new String[]{"ID da transação", payment.getGatewayPaymentId()});
        }
        return card("Pagamento", rows);
    }

    private PdfPTable repairOrderCard(RepairOrder repairOrder) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Ordem de serviço", "Nº " + repairOrder.getId() + " — " + repairOrder.getStatus().getDescription()});
        rows.add(new String[]{"Cliente", repairOrder.getCustomer().getName()});
        rows.add(new String[]{"Telefone", repairOrder.getCustomer().getPhone()});
        rows.add(new String[]{"Aparelho", repairOrder.getDevice().getModel()
                + " (série " + repairOrder.getDevice().getSerialNumber() + ")"});
        return card("Ordem de Serviço", rows);
    }

    private PdfPTable notesCard(Payment payment) {
        List<String[]> rows = new ArrayList<>();
        if (hasText(payment.getPayerName())) {
            String payer = payment.getPayerName();
            if (hasText(payment.getPayerDocument())) {
                payer += " (" + payment.getPayerDocument() + ")";
            }
            rows.add(new String[]{"Pagador", payer});
        }
        if (hasText(payment.getDescription())) {
            rows.add(new String[]{"Observações", payment.getDescription()});
        }
        return card("Informações Complementares", rows);
    }

    private PdfPTable card(String title, List<String[]> rows) {
        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Phrase(title.toUpperCase(), font(7.5f, Font.BOLD, GRAY)));
        titleCell.setBackgroundColor(PANEL);
        titleCell.setBorder(Rectangle.BOTTOM);
        titleCell.setBorderColor(BORDER);
        titleCell.setPadding(4f);
        inner.addCell(titleCell);

        PdfPTable body = new PdfPTable(2);
        body.setWidthPercentage(100);
        try {
            body.setWidths(new float[]{1.1f, 2f});
        } catch (DocumentException ignored) {
            // larguras fixas e válidas, nunca lança
        }
        for (String[] row : rows) {
            body.addCell(labelCell(row[0]));
            body.addCell(valueCell(row[1]));
        }
        PdfPCell bodyCell = new PdfPCell(body);
        bodyCell.setBorder(Rectangle.NO_BORDER);
        bodyCell.setPadding(0f);
        inner.addCell(bodyCell);

        PdfPTable outer = new PdfPTable(1);
        outer.setWidthPercentage(100);
        PdfPCell outerCell = new PdfPCell(inner);
        outerCell.setBorder(Rectangle.BOX);
        outerCell.setBorderColor(BORDER);
        outerCell.setPadding(0f);
        outer.addCell(outerCell);
        return outer;
    }

    private PdfPCell labelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(8, Font.NORMAL, GRAY)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3.5f);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(8.5f, Font.BOLD, BLACK)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3.5f);
        return cell;
    }

    // ---------------------------------------------------------------- footer

    private PdfPTable footer() {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell rule = new PdfPCell();
        rule.setFixedHeight(0.5f);
        rule.setBackgroundColor(BORDER);
        rule.setBorder(Rectangle.NO_BORDER);
        table.addCell(rule);

        PdfPCell text = new PdfPCell();
        text.setBorder(Rectangle.NO_BORDER);
        text.setPaddingTop(5f);
        text.setHorizontalAlignment(Element.ALIGN_CENTER);
        text.addElement(new Phrase("Este documento não possui valor fiscal.", font(7, Font.ITALIC, GRAY)));
        text.addElement(new Phrase("Emitido em " + formatDate(LocalDateTime.now()), font(7, Font.ITALIC, GRAY)));
        table.addCell(text);

        return table;
    }

    // --------------------------------------------------------------- shared

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
        return value == null ? "-" : "R$ " + value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static Font font(float size, int style, Color color) {
        return FontCache.get(size, style, color);
    }

    private static com.lowagie.text.Paragraph spacer(float height) {
        return new com.lowagie.text.Paragraph(" ", font(height, Font.NORMAL, Color.WHITE));
    }

    /**
     * {@link com.lowagie.text.FontFactory} não faz cache por cor, então evitamos recriar
     * um {@link Font} do zero a cada chamada com uma combinação (tamanho, estilo, cor) já vista.
     */
    private static final class FontCache {
        private static final java.util.Map<String, Font> CACHE = new java.util.concurrent.ConcurrentHashMap<>();

        static Font get(float size, int style, Color color) {
            String key = size + ":" + style + ":" + color.getRGB();
            return CACHE.computeIfAbsent(key, ignored -> new Font(Font.HELVETICA, size, style, color));
        }
    }
}
