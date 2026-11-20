package Ferreteria.notificaciones.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
@Slf4j
public class EmailService {

    private final Resend resend;
    private final String remitente;

    public EmailService(@Value("${resend.api-key}") String apiKey,
                        @Value("${resend.from}") String remitente) {
        this.remitente = remitente;
        this.resend = apiKey.isBlank() || remitente.isBlank() ? null : new Resend(apiKey);
    }

    public void enviar(String para, String asunto, String mensaje) {
        if (resend == null) {
            log.debug("Envío de correo deshabilitado, por falta de configuración ");
            return;
        }
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(remitente)
                    .to(para)
                    .subject(asunto)
                    .html("<p>" + HtmlUtils.htmlEscape(mensaje) + "</p>")
                    .build();
            resend.emails().send(params);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo ({})", e.getClass().getSimpleName());
        }
    }
}
