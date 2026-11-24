package ferreteria.carrito.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CarritoResponse {
    private String clienteId;
    private List<ItemCarritoResponse> items;
    private Integer totalUnidades;
    private Integer subtotal;
}
