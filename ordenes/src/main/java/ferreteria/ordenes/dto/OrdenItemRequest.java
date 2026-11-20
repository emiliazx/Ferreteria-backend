package ferreteria.ordenes.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrdenItemRequest {
    @NotNull
    private Long productoId;

    @NotBlank
    private String titulo;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precioUnitario;

    @NotNull
    @Min(1)
    private Integer cantidad;

    private String imagen;
}
