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
 */
@Service
@RequiredArgsConstructor
public class PaymentReceiptService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color INK = new Color(15, 23, 42);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color PANEL = new Color(248, 250, 252);
    private static final Color ACCENT = new Color(37, 99, 235);

    private static final Color GREEN = new Color(21, 128, 61);
    private static final Color GREEN_BG = new Color(220, 252, 231);
    private static final Color RED = new Color(185, 28, 28);
    private static final Color RED_BG = new Color(254, 226, 226);
    private static final Color AMBER = new Color(180, 83, 9);
    private static final Color AMBER_BG = new Color(254, 243, 199);
    private static final Color SLATE_BADGE = new Color(71, 85, 105);
    private static final Color SLATE_BADGE_BG = new Color(226, 232, 240);

    private final ShopProperties shop;

    public byte[] generate(Payment payment) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try {
            PdfWriter.getInstance(document, output);
            document.open();

            document.add(header());
            document.add(accentRule());
            document.add(spacer(14f));

            document.add(titleBar(payment));
            document.add(spacer(12f));

            document.add(amountPanel(payment));
            document.add(spacer(12f));

            document.add(twoColumns(paymentCard(payment), repairOrderCard(payment.getRepairOrder())));

            if (hasText(payment.getPayerName()) || hasText(payment.getDescription())) {
                document.add(spacer(10f));
                document.add(notesCard(payment));
            }

            document.add(spacer(18f));
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
        left.addElement(new Phrase(blankToDash(shop.name()), font(16, Font.BOLD, INK)));
        String contact = shopLine();
        if (!contact.equals("-")) {
            left.addElement(spacerPhrase());
            left.addElement(new Phrase(contact, font(8.5f, Font.NORMAL, MUTED)));
        }
        table.addCell(left);

        PdfPCell right = new PdfPCell(new Phrase("COMPROVANTE DE\nPAGAMENTO", font(9, Font.BOLD, MUTED)));
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(right);

        return table;
    }

    private PdfPTable accentRule() {
        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(2.5f);
        cell.setBackgroundColor(ACCENT);
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
        left.addElement(new Phrase("Recibo de Pagamento", font(15, Font.BOLD, INK)));
        left.addElement(new Phrase("Nº " + payment.getId(), font(9.5f, Font.NORMAL, MUTED)));
        table.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.addElement(statusBadge(payment.getStatus()));
        table.addCell(right);

        return table;
    }

    private PdfPTable statusBadge(PaymentStatus status) {
        PdfPTable badge = new PdfPTable(1);
        badge.setWidthPercentage(48);
        badge.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell cell = new PdfPCell(new Phrase(status.getDescription().toUpperCase(), font(9, Font.BOLD, badgeColor(status))));
        cell.setBackgroundColor(badgeBackground(status));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        badge.addCell(cell);
        return badge;
    }

    private static Color badgeColor(PaymentStatus status) {
        return switch (status) {
            case APPROVED -> GREEN;
            case REJECTED, CANCELLED -> RED;
            case REFUNDED -> SLATE_BADGE;
            case PENDING -> AMBER;
        };
    }

    private static Color badgeBackground(PaymentStatus status) {
        return switch (status) {
            case APPROVED -> GREEN_BG;
            case REJECTED, CANCELLED -> RED_BG;
            case REFUNDED -> SLATE_BADGE_BG;
            case PENDING -> AMBER_BG;
        };
    }

    // --------------------------------------------------------- amount panel

    private PdfPTable amountPanel(Payment payment) {
        PdfPTable outer = new PdfPTable(1);
        outer.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(PANEL);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER);
        cell.setPadding(14f);

        cell.addElement(new Phrase("VALOR " + (payment.getStatus() == PaymentStatus.APPROVED ? "PAGO" : "DO PAGAMENTO"),
                font(8.5f, Font.BOLD, MUTED)));
        cell.addElement(new Phrase(formatMoney(payment.getAmount()), font(27, Font.BOLD, INK)));
        cell.addElement(spacerPhrase());

        String secondLine = describeMethod(payment) + "  •  " + dateLabel(payment) + " " + formatDate(paymentDate(payment));
        cell.addElement(new Phrase(secondLine, font(9.5f, Font.NORMAL, MUTED)));

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
        leftCell.setPaddingRight(6f);
        row.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell(right);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPadding(0f);
        rightCell.setPaddingLeft(6f);
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

        PdfPCell titleCell = new PdfPCell(new Phrase(title.toUpperCase(), font(8.5f, Font.BOLD, MUTED)));
        titleCell.setBackgroundColor(PANEL);
        titleCell.setBorder(Rectangle.BOTTOM);
        titleCell.setBorderColor(BORDER);
        titleCell.setPadding(7f);
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
        PdfPCell cell = new PdfPCell(new Phrase(text, font(9, Font.NORMAL, MUTED)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(6f);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(9.5f, Font.BOLD, INK)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(6f);
        return cell;
    }

    // ---------------------------------------------------------------- footer

    private PdfPTable footer() {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell rule = new PdfPCell();
        rule.setFixedHeight(0.75f);
        rule.setBackgroundColor(BORDER);
        rule.setBorder(Rectangle.NO_BORDER);
        table.addCell(rule);

        PdfPCell text = new PdfPCell();
        text.setBorder(Rectangle.NO_BORDER);
        text.setPaddingTop(8f);
        text.setHorizontalAlignment(Element.ALIGN_CENTER);
        text.addElement(centered("Este documento não possui valor fiscal.", font(8, Font.ITALIC, MUTED)));
        text.addElement(centered("Emitido em " + formatDate(LocalDateTime.now()), font(8, Font.ITALIC, MUTED)));
        table.addCell(text);

        return table;
    }

    private static Phrase centered(String text, Font font) {
        return new Phrase(text, font);
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

    private static Phrase spacerPhrase() {
        return new Phrase(" ", font(4, Font.NORMAL, Color.WHITE));
    }

    private static com.lowagie.text.Paragraph spacer(float height) {
        com.lowagie.text.Paragraph paragraph = new com.lowagie.text.Paragraph(" ", font(height, Font.NORMAL, Color.WHITE));
        return paragraph;
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
