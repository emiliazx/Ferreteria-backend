package ferreteria.carrito.client;

import ferreteria.carrito.dto.ProductoInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ProductosClientTest {

    private static final String BASE = "http://productos:8082";
    private static final String URL = BASE + "/api/productos/701";

    private MockRestServiceServer server;
    private ProductosClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ProductosClient(builder, BASE);
    }

    @Test
    void obtener_productoExistente_mapeaLosCamposIgnorandoLosDemas() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":701,"categoria":"construccion","titulo":"Cemento 25kg",
                         "descripcion":"Saco de cemento","precio":4990,"imagen":"/img/contr1.jpg"}
                        """, MediaType.APPLICATION_JSON));

        ProductoInfo p = client.obtener(701L);

        assertEquals(701L, p.id());
        assertEquals("Cemento 25kg", p.titulo());
        assertEquals(4990, p.precio());
        assertEquals("/img/contr1.jpg", p.imagen());
        server.verify();
    }

    @Test
    void obtener_productoNoExiste_lanza404() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> client.obtener(701L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Producto no encontrado", ex.getReason());
    }

    @Test
    void obtener_errorDelServicio_lanza503() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> client.obtener(701L));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }

    @Test
    void obtener_servicioInaccesible_lanza503() {
        server.expect(requestTo(URL)).andRespond(withException(new IOException("connection refused")));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> client.obtener(701L));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }

    @Test
    void obtener_cuerpoNulo_lanza502() {
        server.expect(requestTo(URL)).andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> client.obtener(701L));

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
    }
}
