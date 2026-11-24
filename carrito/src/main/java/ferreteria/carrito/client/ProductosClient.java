package ferreteria.carrito.client;

import ferreteria.carrito.dto.ProductoInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProductosClient {

    private final RestClient client;

    public ProductosClient(RestClient.Builder builder,
                           @Value("${productos.service-url:http://productos:8082}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public ProductoInfo obtener(Long productoId) {
        try {
            ProductoInfo producto = client.get()
                    .uri("/api/productos/{id}", productoId)
                    .retrieve()
                    .body(ProductoInfo.class);
            if (producto == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Respuesta inválida del servicio de productos");
            }
            return producto;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Servicio de productos no disponible");
        }
    }
}
