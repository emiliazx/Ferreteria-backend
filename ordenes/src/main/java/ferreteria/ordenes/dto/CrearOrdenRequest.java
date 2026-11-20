package ferreteria.ordenes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CrearOrdenRequest {
    private Long userId;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 3)
    private String nombreCliente;

    private String telefono;

    @NotBlank
    private String direccion;

    private String comuna;
    private String region;
    private String referencia;

    private String transportista;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal costoDespacho;

    private String metodoPago;

    @NotEmpty
    @Valid
    private List<OrdenItemRequest> items;
}
