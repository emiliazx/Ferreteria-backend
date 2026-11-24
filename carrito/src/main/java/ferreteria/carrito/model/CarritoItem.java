package ferreteria.carrito.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "carrito_items")
@Getter
@Setter
@NoArgsConstructor
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false)
    private Integer precioUnitario;

    private String imagen;

    @Column(nullable = false)
    private Integer cantidad;
}
