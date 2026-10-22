package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.config.ShopProperties;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.exception.CustomerContactMissingException;
import br.com.carloslonghi.eletrolonghi.exception.ReceiptDeliveryException;
import br.com.carloslonghi.eletrolonghi.metrics.PaymentMetrics;
import br.com.carloslonghi.eletrolonghi.repository.PaymentRepository;
import br.com.carloslonghi.eletrolonghi.service.mail.ReceiptMailer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Entrega do comprovante ao cliente, usando o contato já cadastrado no {@link Customer}:
 * PDF ({@link PaymentReceiptService}), envio por e-mail ({@link ReceiptMailer}) e link
 * {@code wa.me} com a mensagem pronta para o WhatsApp.
 *
 * <p>O WhatsApp é só um link (sem API da Meta): abre a conversa com o cliente e o texto
 * preenchido; o PDF é anexado por quem envia. Transacional (somente leitura) porque o PDF
 * lê associações lazy da ordem/aparelho.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptDeliveryService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PaymentRepository paymentRepository;
    private final PaymentReceiptService paymentReceiptService;
    private final ReceiptMailer receiptMailer;
    private final ShopProperties shop;
    private final PaymentMetrics paymentMetrics;

    public Optional<byte[]> renderReceipt(Long paymentId) {
        return paymentRepository.findById(paymentId).map(paymentReceiptService::generate);
    }

    /** @return o pagamento cujo comprovante foi enviado; vazio se o pagamento não existe. */
    public Optional<Payment> sendReceiptByEmail(Long paymentId) {
        return paymentRepository.findById(paymentId).map(payment -> {
            Customer customer = payment.getRepairOrder().getCustomer();
            if (customer.getEmail() == null || customer.getEmail().isBlank()) {
                throw new CustomerContactMissingException(
                        "O cliente " + customer.getName() + " não possui e-mail cadastrado.");
            }

            byte[] pdf = paymentReceiptService.generate(payment);
            try {
                receiptMailer.send(customer.getEmail().trim(), emailSubject(payment), emailBody(payment),
                        "comprovante-" + payment.getId() + ".pdf", pdf);
            } catch (ReceiptDeliveryException exception) {
                paymentMetrics.receiptSent(PaymentMetrics.CHANNEL_EMAIL, false);
                throw exception;
            }
            paymentMetrics.receiptSent(PaymentMetrics.CHANNEL_EMAIL, true);
            return payment;
        });
    }

    public Optional<WhatsAppLink> whatsAppLink(Long paymentId) {
        return paymentRepository.findById(paymentId).map(payment -> {
            Customer customer = payment.getRepairOrder().getCustomer();
            String phone = normalizePhone(customer.getPhone()).orElseThrow(() -> new CustomerContactMissingException(
                    "O cliente " + customer.getName() + " não possui um telefone válido para WhatsApp."));

            String message = whatsAppMessage(payment);
            String url = "https://wa.me/" + phone + "?text="
                    + URLEncoder.encode(message, StandardCharsets.UTF_8).replace("+", "%20");

            paymentMetrics.receiptSent(PaymentMetrics.CHANNEL_WHATSAPP, true);
            return new WhatsAppLink(phone, message, url);
        });
    }

    /**
     * Telefone no formato do {@code wa.me}: só dígitos, com DDI. Números brasileiros com DDD
     * (10 ou 11 dígitos) ganham o 55; já com 55 (12 ou 13 dígitos) ficam como estão.
     */
    static Optional<String> normalizePhone(String phone) {
        if (phone == null) {
            return Optional.empty();
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 10 || digits.length() == 11) {
            return Optional.of("55" + digits);
        }
        if ((digits.length() == 12 || digits.length() == 13) && digits.startsWith("55")) {
            return Optional.of(digits);
        }
        return Optional.empty();
    }

    private String whatsAppMessage(Payment payment) {
        Customer customer = payment.getRepairOrder().getCustomer();
        StringBuilder message = new StringBuilder()
                .append("Olá, ").append(firstName(customer.getName())).append("! Aqui é da ").append(shopName()).append(".\n")
                .append("Segue o comprovante do serviço da *OS nº ").append(payment.getRepairOrder().getId()).append("*:\n\n")
                .append("• Aparelho: ").append(describeDevice(payment.getRepairOrder().getDevice())).append('\n')
                .append("• Valor: ").append(PaymentReceiptService.formatMoney(payment.getAmount()))
                .append(" (").append(PaymentReceiptService.describeMethod(payment)).append(")\n")
                .append("• Pagamento: ").append(payment.getStatus().getDescription());
        if (payment.getStatus() == PaymentStatus.APPROVED && payment.getPaidAt() != null) {
            message.append(" em ").append(DATE.format(payment.getPaidAt()));
        }
        message.append('\n');
        Optional<LocalDate> warrantyUntil = paymentReceiptService.warrantyUntil(payment);
        warrantyUntil.ifPresent(until -> message.append("• Garantia: ").append(shop.warrantyDays())
                .append(" dias, até ").append(DATE.format(until)).append('\n'));
        message.append("\nObrigado pela preferência!");
        return message.toString();
    }

    private String emailSubject(Payment payment) {
        return "Comprovante de serviço - OS nº " + payment.getRepairOrder().getId() + " | " + shopName();
    }

    private String emailBody(Payment payment) {
        Customer customer = payment.getRepairOrder().getCustomer();
        String warranty = paymentReceiptService.warrantyUntil(payment)
                .map(until -> "<p>Garantia do serviço: <strong>" + shop.warrantyDays() + " dias</strong>, até "
                        + DATE.format(until) + ".</p>")
                .orElse("");
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; color: #333333; line-height: 1.5;">
                    <p>Olá, %s!</p>
                    <p>Segue em anexo o comprovante do serviço da <strong>OS nº %d</strong> (%s).</p>
                    <p>Valor: <strong>%s</strong> (%s) — pagamento %s.</p>
                    %s
                    <p>Obrigado pela preferência!<br>%s</p>
                    <p style="font-size: 12px; color: #999999;">Este é um e-mail automático. Este comprovante não possui valor fiscal.</p>
                </body>
                </html>
                """.formatted(
                HtmlUtils.htmlEscape(firstName(customer.getName())),
                payment.getRepairOrder().getId(),
                HtmlUtils.htmlEscape(describeDevice(payment.getRepairOrder().getDevice())),
                PaymentReceiptService.formatMoney(payment.getAmount()),
                HtmlUtils.htmlEscape(PaymentReceiptService.describeMethod(payment)),
                payment.getStatus().getDescription().toLowerCase(),
                warranty,
                HtmlUtils.htmlEscape(shopName()));
    }

    private String shopName() {
        return shop.name() == null || shop.name().isBlank() ? "assistência técnica" : shop.name();
    }

    private static String describeDevice(Device device) {
        String brand = device.getBrand() != null ? device.getBrand().getName() + " " : "";
        return brand + device.getModel();
    }

    private static String firstName(String name) {
        String trimmed = name == null ? "" : name.trim();
        int space = trimmed.indexOf(' ');
        return space > 0 ? trimmed.substring(0, space) : trimmed;
    }

    /** Link do WhatsApp com a mensagem do comprovante pronta. */
    public record WhatsAppLink(String phone, String message, String url) {
    }
}
