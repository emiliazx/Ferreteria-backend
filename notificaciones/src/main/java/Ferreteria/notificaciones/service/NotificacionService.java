
package Ferreteria.notificaciones.service;


import org.springframework.stereotype.Service;

import Ferreteria.notificaciones.dto.NotificacionRequest;
import Ferreteria.notificaciones.model.Notificacion;
import Ferreteria.notificaciones.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {
    private final NotificacionRepository repository;

    public Notificacion crear(NotificacionRequest req) {
        Notificacion n = Notificacion.builder()
                .userId(req.getUserId())
                .email(req.getEmail())
                .tipo(req.getTipo())
                .titulo(req.getTitulo())
                .mensaje(req.getMensaje())
                .leida(false)
                .build();
        return repository.save(n);
    }

    public List<Notificacion> listarPorUsuario(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notificacion> listarPorEmail(String email) {
        return repository.findByEmailOrderByCreatedAtDesc(email);
    }

    public Notificacion marcarLeida(Long id) {
        Notificacion n = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        n.setLeida(true);
        return repository.save(n);
    }
}
