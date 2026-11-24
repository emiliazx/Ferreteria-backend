package ferreteria.carrito.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemCarritoResponse {
    private Long productoId;
    private String titulo;
    private Integer precioUnitario;
    private String imagen;
    private Integer cantidad;
    private Integer subtotal;
}
