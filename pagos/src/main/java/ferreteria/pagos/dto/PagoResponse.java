package ferreteria.pagos.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PagoResponse {
    private Long id;
    private Long ordenId;
    private BigDecimal monto;
    private String metodo;
    private String estado;
    private String tarjetaMask;
    private String titular;
    private String comprobante;
    private String mensaje;
    private LocalDateTime fecha;
}
