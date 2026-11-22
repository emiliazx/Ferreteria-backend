package Ferreteria.notificaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SeguimientoContactoRequest {

    @NotBlank(message = "El remitente es obligatorio")
    @Pattern(regexp = "CLIENTE|ADMIN", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Remitente debe ser CLIENTE o ADMIN")
    private String remitente;

    @NotBlank(message = "El mensaje no puede estar vacío")
    private String texto;
}