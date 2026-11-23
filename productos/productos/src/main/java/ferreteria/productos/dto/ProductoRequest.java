package ferreteria.productos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductoRequest {

    @NotBlank
    private String categoria;

    @NotBlank
    @Size(max = 200)
    private String titulo;

    @NotBlank
    @Size(max = 1000)
    private String descripcion;

    @NotNull
    @Min(1)
    private Integer precio;

    @Size(max = 255)
    private String imagen;
}
