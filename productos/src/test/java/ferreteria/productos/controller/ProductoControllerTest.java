package ferreteria.productos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ferreteria.productos.dto.ProductoRequest;
import ferreteria.productos.dto.ProductoResponse;
import ferreteria.productos.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService service;

    @InjectMocks
    private ProductoController controller;

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private ProductoResponse respuesta(Long id, String categoria) {
        return ProductoResponse.builder()
                .id(id)
                .categoria(categoria)
                .titulo("Producto " + id)
                .descripcion("Descripción " + id)
                .precio(4990)
                .imagen("/img/p" + id + ".jpg")
                .build();
    }

    private ProductoRequest request() {
        ProductoRequest req = new ProductoRequest();
        req.setCategoria("pintura");
        req.setTitulo("Esmalte al Agua");
        req.setDescripcion("Pintura de secado rápido");
        req.setPrecio(14990);
        req.setImagen("/img/pint3.jpg");
        return req;
    }

    @Test
    void listar_sinFiltro_devuelve200ConLista() throws Exception {
        when(service.listar(null)).thenReturn(List.of(respuesta(1L, "pintura"), respuesta(2L, "madera")));

        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].categoria").value("madera"));
    }

    @Test
    void listar_conCategoria_pasaElFiltroAlServicio() throws Exception {
        when(service.listar("pintura")).thenReturn(List.of(respuesta(1L, "pintura")));

        mvc.perform(get("/api/productos").param("categoria", "pintura"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoria").value("pintura"));
    }

    @Test
    void listar_categoriaInvalida_devuelve400() throws Exception {
        when(service.listar("juguetes"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría inválida"));

        mvc.perform(get("/api/productos").param("categoria", "juguetes"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtener_existente_devuelve200() throws Exception {
        when(service.obtener(701L)).thenReturn(respuesta(701L, "construccion"));

        mvc.perform(get("/api/productos/701"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(701))
                .andExpect(jsonPath("$.titulo").value("Producto 701"))
                .andExpect(jsonPath("$.precio").value(4990));
    }

    @Test
    void obtener_noExiste_devuelve404() throws Exception {
        when(service.obtener(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        mvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_valido_devuelve201ConElProducto() throws Exception {
        when(service.crear(any(ProductoRequest.class))).thenReturn(respuesta(50L, "pintura"));

        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.categoria").value("pintura"));

        verify(service).crear(any(ProductoRequest.class));
    }

    @Test
    void crear_sinTituloNiPrecioValido_devuelve400SinLlamarAlServicio() throws Exception {
        ProductoRequest invalido = request();
        invalido.setTitulo("");
        invalido.setPrecio(0);

        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void crear_sinPrecio_devuelve400() throws Exception {
        ProductoRequest invalido = request();
        invalido.setPrecio(null);

        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void crear_categoriaInvalida_propagaElError400DelServicio() throws Exception {
        when(service.crear(any(ProductoRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría inválida"));

        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_valido_devuelve200() throws Exception {
        when(service.actualizar(eq(701L), any(ProductoRequest.class))).thenReturn(respuesta(701L, "pintura"));

        mvc.perform(put("/api/productos/701")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(701));
    }

    @Test
    void actualizar_invalido_devuelve400() throws Exception {
        ProductoRequest invalido = request();
        invalido.setDescripcion(" ");

        mvc.perform(put("/api/productos/701")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void actualizar_noExiste_devuelve404() throws Exception {
        when(service.actualizar(eq(99L), any(ProductoRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        mvc.perform(put("/api/productos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request())))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mvc.perform(delete("/api/productos/701"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(701L);
    }

    @Test
    void eliminar_noExiste_devuelve404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"))
                .when(service).eliminar(99L);

        mvc.perform(delete("/api/productos/99"))
                .andExpect(status().isNotFound());
    }
}
