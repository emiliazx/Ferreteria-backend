package Ferreteria.notificaciones.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import Ferreteria.notificaciones.model.Notificacion;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notificacion> findByEmailOrderByCreatedAtDesc(String email);
}
