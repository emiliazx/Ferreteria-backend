package ferreteria.carrito.repository;

import ferreteria.carrito.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByClienteId(String clienteId);
}
