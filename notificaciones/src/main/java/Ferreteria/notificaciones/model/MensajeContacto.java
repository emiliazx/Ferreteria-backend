package Ferreteria.notificaciones.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mensajes_contacto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 40)
    private String telefono;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(columnDefinition = "TEXT")
    private String respuesta;

    @Column(nullable = false, length = 30)
    private String estado; // PENDIENTE | RESPONDIDO

    private LocalDateTime createdAt;
    private LocalDateTime respondidoAt;

    @OneToMany(
        mappedBy = "mensajeContacto",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @OrderBy("createdAt ASC, id ASC")
    @Builder.Default
    private List<MensajeContactoHilo> hilo = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}