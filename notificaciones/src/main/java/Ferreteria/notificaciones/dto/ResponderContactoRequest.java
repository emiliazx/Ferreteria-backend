package Ferreteria.notificaciones.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResponderContactoRequest {

    @NotBlank(message = "La respuesta no puede estar vacía")
    private String respuesta;
}