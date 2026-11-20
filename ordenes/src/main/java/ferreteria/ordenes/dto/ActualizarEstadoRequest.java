package ferreteria.ordenes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActualizarEstadoRequest {
    @NotBlank
    private String estado;
}
