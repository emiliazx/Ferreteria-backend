package ferreteria.productos.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductoResponse {
    private Long id;
    private String categoria;
    private String titulo;
    private String descripcion;
    private Integer precio;
    private String imagen;
}
