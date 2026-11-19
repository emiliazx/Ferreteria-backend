package Ferreteria.notificaciones.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;

    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;
}