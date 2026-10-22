package br.com.carloslonghi.eletrolonghi.service.mail;

import br.com.carloslonghi.eletrolonghi.exception.ReceiptDeliveryException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Envia o comprovante por SMTP ({@code spring.mail.*}), de forma síncrona: quem chama
 * fica sabendo na hora se o servidor aceitou a mensagem.
 */
@Component
public class SmtpReceiptMailer implements ReceiptMailer {

    private static final Logger log = LoggerFactory.getLogger(SmtpReceiptMailer.class);

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpReceiptMailer(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(String to, String subject, String html, String attachmentName, byte[] pdf) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.addAttachment(attachmentName, new ByteArrayResource(pdf), "application/pdf");

            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            log.warn("Falha ao enviar o comprovante {} por e-mail: {}", attachmentName, exception.getMessage());
            throw new ReceiptDeliveryException("Não foi possível enviar o comprovante por e-mail.", exception);
        }
    }
}
