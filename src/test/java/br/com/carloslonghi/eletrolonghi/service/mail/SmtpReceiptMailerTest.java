package br.com.carloslonghi.eletrolonghi.service.mail;

import br.com.carloslonghi.eletrolonghi.exception.ReceiptDeliveryException;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmtpReceiptMailerTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final SmtpReceiptMailer mailer = new SmtpReceiptMailer(mailSender, "loja@eletrolonghi.com");

    @Test
    void shouldSendHtmlMessageWithPdfAttachment() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));

        mailer.send("cliente@mail.com", "Comprovante", "<p>Olá</p>", "comprovante-1.pdf", "%PDF".getBytes());

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage message = captor.getValue();
        message.saveChanges();
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("cliente@mail.com");
        assertThat(message.getFrom()[0].toString()).isEqualTo("loja@eletrolonghi.com");
        assertThat(message.getSubject()).isEqualTo("Comprovante");

        Multipart multipart = (Multipart) message.getContent();
        BodyPart attachment = null;
        for (int i = 0; i < multipart.getCount(); i++) {
            if ("comprovante-1.pdf".equals(multipart.getBodyPart(i).getFileName())) {
                attachment = multipart.getBodyPart(i);
            }
        }
        assertThat(attachment).isNotNull();
        assertThat(attachment.getContentType()).startsWith("application/pdf");
    }

    @Test
    void shouldTranslateMailFailure() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        doThrow(new MailSendException("connection refused")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> mailer.send("cliente@mail.com", "s", "<p/>", "c.pdf", new byte[]{1}))
                .isInstanceOf(ReceiptDeliveryException.class)
                .hasMessageContaining("e-mail");
    }
}
