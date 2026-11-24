package ferreteria.carrito.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "carritos")
@Getter
@Setter
@NoArgsConstructor
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String clienteId;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CarritoItem> items = new ArrayList<>();

    public Carrito(String clienteId) {
        this.clienteId = clienteId;
    }

    public Optional<CarritoItem> buscarItem(Long productoId) {
        return items.stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst();
    }

    public void agregarItem(CarritoItem item) {
        item.setCarrito(this);
        items.add(item);
    }

    public boolean quitarItem(Long productoId) {
        return items.removeIf(i -> i.getProductoId().equals(productoId));
    }

    public void vaciar() {
        items.clear();
    }
}
