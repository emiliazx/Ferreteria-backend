package ferreteria.carrito.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ferreteria.carrito.dto.ActualizarCantidadRequest;
import ferreteria.carrito.dto.AgregarItemRequest;
import ferreteria.carrito.dto.CarritoResponse;
import ferreteria.carrito.dto.ItemCarritoResponse;
import ferreteria.carrito.service.CarritoService;
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
class CarritoControllerTest {

    @Mock
    private CarritoService service;

    @InjectMocks
    private CarritoController controller;

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private CarritoResponse carrito() {
        return CarritoResponse.builder()
                .clienteId("42")
                .items(List.of(ItemCarritoResponse.builder()
                        .productoId(701L).titulo("Cemento").precioUnitario(4990)
                        .imagen("/img/contr1.jpg").cantidad(2).subtotal(9980).build()))
                .totalUnidades(2)
                .subtotal(9980)
                .build();
    }

    private String agregarJson(Long productoId, Integer cantidad) throws Exception {
        AgregarItemRequest req = new AgregarItemRequest();
        req.setProductoId(productoId);
        req.setCantidad(cantidad);
        return mapper.writeValueAsString(req);
    }

    private String cantidadJson(Integer cantidad) throws Exception {
        ActualizarCantidadRequest req = new ActualizarCantidadRequest();
        req.setCantidad(cantidad);
        return mapper.writeValueAsString(req);
    }

    @Test
    void obtener_devuelve200ConElCarrito() throws Exception {
        when(service.obtener("42")).thenReturn(carrito());

        mvc.perform(get("/api/carritos/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value("42"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productoId").value(701))
                .andExpect(jsonPath("$.totalUnidades").value(2))
                .andExpect(jsonPath("$.subtotal").value(9980));
    }

    @Test
    void agregar_valido_devuelve200() throws Exception {
        when(service.agregar(eq("42"), any(AgregarItemRequest.class))).thenReturn(carrito());

        mvc.perform(post("/api/carritos/42/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(agregarJson(701L, 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cantidad").value(2));
    }

    @Test
    void agregar_sinProductoId_devuelve400() throws Exception {
        mvc.perform(post("/api/carritos/42/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(agregarJson(null, 1)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void agregar_cantidadCero_devuelve400() throws Exception {
        mvc.perform(post("/api/carritos/42/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(agregarJson(701L, 0)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void agregar_productoInexistente_devuelve404() throws Exception {
        when(service.agregar(eq("42"), any(AgregarItemRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        mvc.perform(post("/api/carritos/42/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(agregarJson(999L, 1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void agregar_servicioDeProductosCaido_devuelve503() throws Exception {
        when(service.agregar(eq("42"), any(AgregarItemRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de productos no disponible"));

        mvc.perform(post("/api/carritos/42/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(agregarJson(701L, 1)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void actualizarCantidad_valida_devuelve200() throws Exception {
        when(service.actualizarCantidad(eq("42"), eq(701L), any(ActualizarCantidadRequest.class)))
                .thenReturn(carrito());

        mvc.perform(put("/api/carritos/42/items/701")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cantidadJson(2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(9980));
    }

    @Test
    void actualizarCantidad_invalida_devuelve400() throws Exception {
        mvc.perform(put("/api/carritos/42/items/701")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cantidadJson(0)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void actualizarCantidad_sinCantidad_devuelve400() throws Exception {
        mvc.perform(put("/api/carritos/42/items/701")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cantidadJson(null)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void actualizarCantidad_productoNoEstaEnElCarrito_devuelve404() throws Exception {
        when(service.actualizarCantidad(eq("42"), eq(999L), any(ActualizarCantidadRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no está en el carrito"));

        mvc.perform(put("/api/carritos/42/items/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cantidadJson(3)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarItem_devuelve200ConElCarritoActualizado() throws Exception {
        when(service.eliminarItem("42", 701L)).thenReturn(carrito());

        mvc.perform(delete("/api/carritos/42/items/701"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value("42"));
    }

    @Test
    void eliminarItem_noExiste_devuelve404() throws Exception {
        when(service.eliminarItem("42", 999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no está en el carrito"));

        mvc.perform(delete("/api/carritos/42/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void vaciar_devuelve204() throws Exception {
        mvc.perform(delete("/api/carritos/42"))
                .andExpect(status().isNoContent());

        verify(service).vaciar("42");
    }
}
