package Ferreteria.notificaciones.controller;

import Ferreteria.notificaciones.dto.ContactoRequest;
import Ferreteria.notificaciones.dto.ResponderContactoRequest;
import Ferreteria.notificaciones.model.MensajeContacto;
import Ferreteria.notificaciones.service.ContactoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones/contacto")
@RequiredArgsConstructor
public class ContactoController {

    private final ContactoService contactoService;

    @PostMapping
    public ResponseEntity<MensajeContacto> enviar(@Valid @RequestBody ContactoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactoService.crearMensaje(req));
    }

    @GetMapping
    public List<MensajeContacto> listar() {
        return contactoService.listarTodos();
    }

    @PutMapping("/{id}/responder")
    public MensajeContacto responder(
            @PathVariable Long id,
            @Valid @RequestBody ResponderContactoRequest req) {
        return contactoService.responderMensaje(id, req.getRespuesta());
    }
}