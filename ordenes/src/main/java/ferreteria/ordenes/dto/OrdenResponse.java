package ferreteria.ordenes.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrdenResponse {
    private Long id;
    private Long userId;
    private String email;
    private String nombreCliente;
    private String telefono;
    private String direccion;
    private String comuna;
    private String region;
    private String referencia;
    private String transportista;
    private BigDecimal costoDespacho;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String estado;
    private String metodoPago;
    private LocalDateTime createdAt;
    private List<OrdenItemResponse> items;
}
