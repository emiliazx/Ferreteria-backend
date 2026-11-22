package ferreteria.pagos.repository;

import ferreteria.pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByOrdenIdOrderByFechaDesc(Long ordenId);
    Optional<Pago> findByComprobante(String comprobante);
    List<Pago> findAllByOrderByFechaDesc();
}