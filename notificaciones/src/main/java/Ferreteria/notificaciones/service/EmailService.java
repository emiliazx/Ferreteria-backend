package Ferreteria.notificaciones.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;

    public EmailService(@Value("${resend.api-key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

   public void enviar(String para, String asunto, String mensaje) {
    System.out.println(">>> Intentando enviar email a: " + para);
    try {
        CreateEmailOptions params = CreateEmailOptions.builder()
            .from("Ferretería Los Maestros <emi.zamora@duocuc.cl>")
            .to(para)
            .subject(asunto)
            .html("<h2>" + asunto + "</h2><p>" + mensaje + "</p>")
            .build();
        
        var response = resend.emails().send(params);
        System.out.println(">>> Respuesta de Resend: " + response);
    } catch (Exception e) {
        System.err.println(">>> ERROR enviando email: " + e.getMessage());
        e.printStackTrace();
    }
}
}