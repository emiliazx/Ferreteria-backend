package ferreteria.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResumenResponse {
    private Long id;
    private String nombre;
    private String email;
    private String rol;
    private Boolean activo;
}