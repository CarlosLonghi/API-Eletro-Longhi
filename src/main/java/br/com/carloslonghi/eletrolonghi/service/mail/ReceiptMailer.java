package br.com.carloslonghi.eletrolonghi.service.mail;

/**
 * Porta de saída para enviar o comprovante por e-mail. A implementação atual é SMTP
 * ({@link SmtpReceiptMailer}); um adaptador assíncrono (ex.: publicar num RabbitMQ consumido
 * pelo Microservice-Mail-Sender) pode substituí-la sem mexer em service/controller.
 *
 * <p>Falhas de envio devem sair como {@code ReceiptDeliveryException} (→ 502).
 */
public interface ReceiptMailer {

    void send(String to, String subject, String html, String attachmentName, byte[] pdf);
}
