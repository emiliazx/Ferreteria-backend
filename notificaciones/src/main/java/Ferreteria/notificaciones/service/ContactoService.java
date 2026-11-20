package Ferreteria.notificaciones.service;

import Ferreteria.notificaciones.dto.ContactoRequest;
import Ferreteria.notificaciones.dto.NotificacionRequest;
import Ferreteria.notificaciones.model.MensajeContacto;
import Ferreteria.notificaciones.repository.MensajeContactoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactoService {

    private final MensajeContactoRepository repository;
    private final NotificacionService notificacionService;
    private final EmailService emailService;

    public MensajeContacto crearMensaje(ContactoRequest req) {
        MensajeContacto nuevo = MensajeContacto.builder()
                .nombre(req.getNombre().trim())
                .telefono(req.getTelefono() != null ? req.getTelefono().trim() : null)
                .email(req.getEmail().trim())
                .mensaje(req.getMensaje().trim())
                .estado("PENDIENTE")
                .build();

        MensajeContacto guardado = repository.save(nuevo);

        // Registrar notificación interna (sin bloquear si Resend falla)
        try {
            NotificacionRequest notif = new NotificacionRequest();
            notif.setEmail(guardado.getEmail());
            notif.setTipo("CONTACTO");
            notif.setTitulo("Consulta recibida de " + guardado.getNombre());
            notif.setMensaje("Hemos recibido tu mensaje: \"" + guardado.getMensaje() + "\"");
            notificacionService.crear(notif);
        } catch (Exception ignored) {
        }

        return guardado;
    }

    public List<MensajeContacto> listarTodos() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public MensajeContacto responderMensaje(Long id, String respuestaTexto) {
        MensajeContacto msg = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mensaje de contacto no encontrado"));

        msg.setRespuesta(respuestaTexto.trim());
        msg.setEstado("RESPONDIDO");
        msg.setRespondidoAt(LocalDateTime.now());

        MensajeContacto actualizado = repository.save(msg);

        // Enviar correo de respuesta al cliente
        emailService.enviar(
                actualizado.getEmail(),
                "Respuesta a tu consulta — Ferretería Los Maestros",
                "Hola <strong>" + actualizado.getNombre() + "</strong>,<br><br>"
                        + "Respecto a tu consulta: <em>\"" + actualizado.getMensaje() + "\"</em><br><br>"
                        + "<strong>Respuesta de nuestro equipo:</strong><br>" + actualizado.getRespuesta()
        );

        return actualizado;
    }
}