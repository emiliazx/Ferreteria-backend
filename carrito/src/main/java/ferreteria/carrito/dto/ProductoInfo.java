package ferreteria.carrito.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoInfo(Long id, String titulo, Integer precio, String imagen) {
}
