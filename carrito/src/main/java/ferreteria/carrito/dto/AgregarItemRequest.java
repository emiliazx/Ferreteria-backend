package ferreteria.carrito.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgregarItemRequest {

    @NotNull
    private Long productoId;

    @Min(1)
    private Integer cantidad;
}
