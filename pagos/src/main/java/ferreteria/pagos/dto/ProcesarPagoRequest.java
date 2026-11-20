package ferreteria.pagos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProcesarPagoRequest {

    @NotNull
    private Long ordenId;

    @NotNull
    @DecimalMin("1.0")
    private BigDecimal monto;

    @NotBlank
    private String metodo; // TARJETA, TRANSFERENCIA, WEBPAY

    // Datos de tarjeta (simulación)
    private String numeroTarjeta;   // 16 dígitos o con espacios
    private String titular;
    private String vencimiento;     // MM/YY
    private String cvv;
}
