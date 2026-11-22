package ferreteria.ordenes.repository;

import ferreteria.ordenes.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Orden> findByEmailOrderByCreatedAtDesc(String email);
    List<Orden> findAllByOrderByCreatedAtDesc();
    List<Orden> findByEstadoOrderByCreatedAtDesc(String estado);
}