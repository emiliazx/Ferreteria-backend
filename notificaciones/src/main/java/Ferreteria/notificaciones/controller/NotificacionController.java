package Ferreteria.notificaciones.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Ferreteria.notificaciones.dto.NotificacionRequest;
import Ferreteria.notificaciones.model.Notificacion;
import Ferreteria.notificaciones.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {
    private final NotificacionService service;

    @PostMapping
    public ResponseEntity<Notificacion> crear(@Valid @RequestBody NotificacionRequest req) {
        return ResponseEntity.ok(service.crear(req));
    }

    @GetMapping("/usuario/{userId}")
    public List<Notificacion> porUsuario(@PathVariable Long userId) {
        return service.listarPorUsuario(userId);
    }

    @GetMapping("/email/{email}")
    public List<Notificacion> porEmail(@PathVariable String email) {
        return service.listarPorEmail(email);
    }

    @PutMapping("/{id}/leida")
    public Notificacion marcarLeida(@PathVariable Long id) {
        return service.marcarLeida(id);
    }
}
