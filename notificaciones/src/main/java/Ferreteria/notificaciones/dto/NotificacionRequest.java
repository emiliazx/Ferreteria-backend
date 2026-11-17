package Ferreteria.notificaciones.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificacionRequest {
    private Long userId;
    private String email;

    @NotBlank
    private String tipo;

    @NotBlank
    private String titulo;

    @NotBlank
    private String mensaje;
}
