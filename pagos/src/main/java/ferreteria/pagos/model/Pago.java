package ferreteria.pagos.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ordenId;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false, length = 30)
    private String metodo; // TARJETA, TRANSFERENCIA, WEBPAY

    @Column(nullable = false, length = 30)
    private String estado; // APROBADO, RECHAZADO, PENDIENTE

    /** Últimos 4 dígitos de la tarjeta (máscara) */
    private String tarjetaMask;

    private String titular;

    /** Código de comprobante generado (ej. COMP-2026-XXXX) */
    @Column(nullable = false, unique = true)
    private String comprobante;

    private String mensaje;

    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) fecha = LocalDateTime.now();
        if (estado == null) estado = "PENDIENTE";
    }
}
