package ferreteria.productos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void customOpenAPI_defineInfoYEsquemaBearerJwt() {
        OpenAPI api = new OpenApiConfig().customOpenAPI();

        assertEquals("Productos Service", api.getInfo().getTitle());
        assertEquals("1.0", api.getInfo().getVersion());
        assertEquals(1, api.getSecurity().size());
        assertTrue(api.getSecurity().get(0).containsKey("bearerAuth"));

        SecurityScheme scheme = api.getComponents().getSecuritySchemes().get("bearerAuth");
        assertNotNull(scheme);
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
    }
}
