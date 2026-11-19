package Ferreteria.notificaciones.repository;

import Ferreteria.notificaciones.model.MensajeContacto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {
    List<MensajeContacto> findAllByOrderByCreatedAtDesc();
}