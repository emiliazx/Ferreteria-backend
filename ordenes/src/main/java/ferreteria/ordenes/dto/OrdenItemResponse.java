package ferreteria.ordenes.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrdenItemResponse {
    private Long id;
    private Long productoId;
    private String titulo;
    private BigDecimal precioUnitario;
    private Integer cantidad;
    private String imagen;
    private BigDecimal subtotal;
}
