package Ferreteria.notificaciones.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_contacto_hilo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeContactoHilo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mensaje_contacto_id", nullable = false)
    @JsonIgnore
    private MensajeContacto mensajeContacto;

    @Column(nullable = false, length = 20)
    private String remitente; // CLIENTE | ADMIN

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}