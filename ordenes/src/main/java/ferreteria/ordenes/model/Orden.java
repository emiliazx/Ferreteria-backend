package ferreteria.ordenes.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nombreCliente;

    private String telefono;

    @Column(nullable = false)
    private String direccion;

    private String comuna;
    private String region;
    private String referencia;

    private String transportista;
    private BigDecimal costoDespacho;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false, length = 30)
    private String estado; // PENDIENTE, PAGADO, ENVIADO, CANCELADO, ENTREGADO

    private String metodoPago;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<OrdenItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (estado == null) estado = "PENDIENTE";
        if (costoDespacho == null) costoDespacho = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addItem(OrdenItem item) {
        items.add(item);
        item.setOrden(this);
    }
}
