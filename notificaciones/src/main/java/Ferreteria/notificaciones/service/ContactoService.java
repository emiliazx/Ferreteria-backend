package Ferreteria.notificaciones.service;

import Ferreteria.notificaciones.dto.ContactoRequest;
import Ferreteria.notificaciones.dto.NotificacionRequest;
import Ferreteria.notificaciones.model.MensajeContacto;
import Ferreteria.notificaciones.model.MensajeContactoHilo;
import Ferreteria.notificaciones.repository.MensajeContactoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactoService {

    private final MensajeContactoRepository repository;
    private final NotificacionService notificacionService;
    private final EmailService emailService;

    @Transactional
    public MensajeContacto crearMensaje(ContactoRequest req) {
        String mensajeLimpio = req.getMensaje().trim();
        String emailLimpio = req.getEmail().trim();
        String nombreLimpio = req.getNombre().trim();

        MensajeContacto nuevo = MensajeContacto.builder()
                .nombre(nombreLimpio)
                .telefono(req.getTelefono() != null ? req.getTelefono().trim() : null)
                .email(emailLimpio)
                .mensaje(mensajeLimpio)
                .estado("PENDIENTE")
                .build();

        MensajeContactoHilo turnoInicial = MensajeContactoHilo.builder()
                .mensajeContacto(nuevo)
                .remitente("CLIENTE")
                .texto(mensajeLimpio)
                .createdAt(LocalDateTime.now())
                .build();
        nuevo.getHilo().add(turnoInicial);

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

    @Transactional(readOnly = true)
    public List<MensajeContacto> listarTodos() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public MensajeContacto responderMensaje(Long id, String respuestaTexto) {
        MensajeContacto msg = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mensaje de contacto no encontrado"));

        String respuestaLimpia = respuestaTexto.trim();
        LocalDateTime ahora = LocalDateTime.now();

        msg.setRespuesta(respuestaLimpia);
        msg.setEstado("RESPONDIDO");
        msg.setRespondidoAt(ahora);

        MensajeContactoHilo turnoAdmin = MensajeContactoHilo.builder()
                .mensajeContacto(msg)
                .remitente("ADMIN")
                .texto(respuestaLimpia)
                .createdAt(ahora)
                .build();
        msg.getHilo().add(turnoAdmin);

        MensajeContacto actualizado = repository.save(msg);

        // Enviar correo de respuesta al cliente
        try {
            emailService.enviar(
                    actualizado.getEmail(),
                    "Respuesta a tu consulta — Ferretería Los Maestros",
                    "Hola <strong>" + actualizado.getNombre() + "</strong>,<br><br>"
                            + "Respecto a tu consulta: <em>\"" + actualizado.getMensaje() + "\"</em><br><br>"
                            + "<strong>Respuesta de nuestro equipo:</strong><br>" + actualizado.getRespuesta()
            );
        } catch (Exception ignored) {
        }

        return actualizado;
    }

    @Transactional
    public MensajeContacto agregarMensajeHilo(Long id, String remitenteRaw, String textoRaw) {
        MensajeContacto msg = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mensaje de contacto no encontrado"));

        String remitente = remitenteRaw != null ? remitenteRaw.trim().toUpperCase() : "CLIENTE";
        if (!"CLIENTE".equals(remitente) && !"ADMIN".equals(remitente)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Remitente debe ser CLIENTE o ADMIN");
        }

        String textoLimpio = textoRaw != null ? textoRaw.trim() : "";
        if (textoLimpio.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El texto del mensaje es obligatorio");
        }

        LocalDateTime ahora = LocalDateTime.now();

        MensajeContactoHilo turno = MensajeContactoHilo.builder()
                .mensajeContacto(msg)
                .remitente(remitente)
                .texto(textoLimpio)
                .createdAt(ahora)
                .build();
        msg.getHilo().add(turno);

        if ("ADMIN".equals(remitente)) {
            msg.setRespuesta(textoLimpio);
            msg.setEstado("RESPONDIDO");
            msg.setRespondidoAt(ahora);
        } else {
            msg.setEstado("PENDIENTE");
        }

        return repository.save(msg);
    }
}