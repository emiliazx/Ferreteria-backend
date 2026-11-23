package ferreteria.carrito.service;

import ferreteria.carrito.client.ProductosClient;
import ferreteria.carrito.dto.ActualizarCantidadRequest;
import ferreteria.carrito.dto.AgregarItemRequest;
import ferreteria.carrito.dto.CarritoResponse;
import ferreteria.carrito.dto.ProductoInfo;
import ferreteria.carrito.model.Carrito;
import ferreteria.carrito.model.CarritoItem;
import ferreteria.carrito.repository.CarritoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    private static final String CLIENTE = "42";

    @Mock
    private CarritoRepository repository;

    @Mock
    private ProductosClient productosClient;

    @InjectMocks
    private CarritoService service;

    private ProductoInfo producto(Long id, int precio) {
        return new ProductoInfo(id, "Producto " + id, precio, "/img/p" + id + ".jpg");
    }

    private CarritoItem item(Long productoId, int precio, int cantidad) {
        CarritoItem item = new CarritoItem();
        item.setProductoId(productoId);
        item.setTitulo("Producto " + productoId);
        item.setPrecioUnitario(precio);
        item.setImagen("/img/p" + productoId + ".jpg");
        item.setCantidad(cantidad);
        return item;
    }

    private Carrito carritoCon(CarritoItem... items) {
        Carrito carrito = new Carrito(CLIENTE);
        for (CarritoItem i : items) {
            carrito.agregarItem(i);
        }
        return carrito;
    }

    private AgregarItemRequest agregarReq(Long productoId, Integer cantidad) {
        AgregarItemRequest req = new AgregarItemRequest();
        req.setProductoId(productoId);
        req.setCantidad(cantidad);
        return req;
    }

    private ActualizarCantidadRequest cantidadReq(int cantidad) {
        ActualizarCantidadRequest req = new ActualizarCantidadRequest();
        req.setCantidad(cantidad);
        return req;
    }

    private void stubSaveDevuelveElMismo() {
        when(repository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void assertStatus(HttpStatus esperado, ResponseStatusException ex) {
        assertEquals(esperado, ex.getStatusCode());
    }

    @Test
    void obtener_conCarrito_calculaTotales() {
        when(repository.findByClienteId(CLIENTE))
                .thenReturn(Optional.of(carritoCon(item(701L, 4990, 2), item(702L, 18900, 1))));

        CarritoResponse r = service.obtener(CLIENTE);

        assertEquals(CLIENTE, r.getClienteId());
        assertEquals(2, r.getItems().size());
        assertEquals(3, r.getTotalUnidades());
        assertEquals(4990 * 2 + 18900, r.getSubtotal());
        assertEquals(701L, r.getItems().get(0).getProductoId());
        assertEquals(9980, r.getItems().get(0).getSubtotal());
        assertEquals("/img/p701.jpg", r.getItems().get(0).getImagen());
    }

    @Test
    void obtener_sinCarrito_devuelveCarritoVacioSinGuardar() {
        when(repository.findByClienteId(CLIENTE)).thenReturn(Optional.empty());

        CarritoResponse r = service.obtener(CLIENTE);

        assertEquals(CLIENTE, r.getClienteId());
        assertTrue(r.getItems().isEmpty());
        assertEquals(0, r.getTotalUnidades());
        assertEquals(0, r.getSubtotal());
        verify(repository, never()).save(any());
    }

    @Test
    void agregar_carritoNuevo_creaCarritoConElItem() {
        when(productosClient.obtener(701L)).thenReturn(producto(701L, 4990));
        when(repository.findByClienteId(CLIENTE)).thenReturn(Optional.empty());
        stubSaveDevuelveElMismo();

        CarritoResponse r = service.agregar(CLIENTE, agregarReq(701L, 3));

        ArgumentCaptor<Carrito> captor = ArgumentCaptor.forClass(Carrito.class);
        verify(repository).save(captor.capture());
        Carrito guardado = captor.getValue();
        assertEquals(CLIENTE, guardado.getClienteId());
        assertEquals(1, guardado.getItems().size());
        assertSame(guardado, guardado.getItems().get(0).getCarrito());

        assertEquals(1, r.getItems().size());
        assertEquals("Producto 701", r.getItems().get(0).getTitulo());
        assertEquals(4990, r.getItems().get(0).getPrecioUnitario());
        assertEquals(3, r.getTotalUnidades());
        assertEquals(14970, r.getSubtotal());
    }

    @Test
    void agregar_sinCantidad_agregaUnaUnidad() {
        when(productosClient.obtener(701L)).thenReturn(producto(701L, 4990));
        when(repository.findByClienteId(CLIENTE)).thenReturn(Optional.empty());
        stubSaveDevuelveElMismo();

        CarritoResponse r = service.agregar(CLIENTE, agregarReq(701L, null));

        assertEquals(1, r.getTotalUnidades());
        assertEquals(4990, r.getSubtotal());
    }

    @Test
    void agregar_productoYaEnCarrito_sumaCantidadYRefrescaPrecio() {
        Carrito existente = carritoCon(item(701L, 4000, 2));
        when(productosClient.obtener(701L)).thenReturn(producto(701L, 4990));
        when(repository.findByClienteId(CLIENTE)).thenReturn(Optional.of(existente));
        stubSaveDevuelveElMismo();

        CarritoResponse r = service.agregar(CLIENTE, agregarReq(701L, 1));

        assertEquals(1, r.getItems().size());
        assertEquals(3, r.getItems().get(0).getCantidad());
        assertEquals(4990, r.getItems().get(0).getPrecioUnitario());
        assertEquals(14970, r.getSubtotal());
    }

    @Test
    void agregar_otroProducto_loAgregaAlCarritoExistente() {
        Carrito existente = carritoCon(item(701L, 4990, 1));
        when(productosClient.obtener(702L)).thenReturn(producto(702L, 18900));
        when(repository.findByClienteId(CLIENTE)).thenReturn(Optional.of(existente));
        stubSaveDevuelveElMismo();

        CarritoResponse r = service.agregar(CLIENTE, agregarReq(702L, 2));

        assertEquals(2, r.getItems().size());
        assertEquals(3, r.getTotalUnidades());
        assertEquals(4990 + 2 * 18900, r.getSubtotal());
    }

    @Test
    void agregar_productoInexistente_propagaErrorYNoGuarda() {
        when(productosClient.obtener(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.agregar(CLIENTE, agregarReq(999L, 1)));

        assertStatus(HttpStatus.NOT_FOUND, ex);
        verify(repository, never()).save(any());
    }

    @Test
    void actualizarCantidad_existente_cambiaLaCantidad() {
        Carrito existente = carritoCon(item(701L, 4990, 1));
        when(repository.findByClienteId(CLIENTE)).thenReturn(Optional.of(existente));
        stubSaveDevuelveElMismo();

        CarritoResponse r = service.actualizarCantidad(CLIENTE, 701L, cantidadReq(5));

        assertEquals(5, r.getItems().get(0).getCantidad());
        assertEquals(5, r.getTotalUnidades());
        assertEquals(24950, r.getSubtotal());
    }

    @Test
    void actualizarCantidad_sinCarrito_lanza404() {
        when(repository.findByClienteId(CLIENTE)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.actualizarCantidad(CLIENTE, 701L, cantidadReq(2)));

        assertStatus(HttpStatus.NOT_FOUND, ex);
        assertEquals("Carrito no encontrado", ex.getReason());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizarCantidad_productoNoEstaEnElCarrito_lanza404() {
        when(repository.findByClienteId(CLIENTE))
                .thenReturn(Optional.of(carritoCon(item(701L, 4990, 1))));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.actualizarCantidad(CLIENTE, 999L, cantidadReq(2)));

        assertStatus(HttpStatus.NOT_FOUND, ex);
        assertEquals("El producto no está en el carrito", ex.getReason());
        verify(repository, never()).save(any());
    }

    @Test
    void eliminarItem_existente_loQuitaDelCarrito() {
        Carrito existente = carritoCon(item(701L, 4990, 1), item(702L, 18900, 1));
        when(repository.findByClienteId(CLIENTE)).thenReturn(Optional.of(existente));
        stubSaveDevuelveElMismo();

        CarritoResponse r = service.eliminarItem(CLIENTE, 701L);

        assertEquals(1, r.getItems().size());
        assertEquals(702L, r.getItems().get(0).getProductoId());
        assertEquals(18900, r.getSubtotal());
    }

    @Test
    void eliminarItem_sinCarrito_lanza404() {
        when(repository.findByClienteId(CLIENTE)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.eliminarItem(CLIENTE, 701L));

        assertStatus(HttpStatus.NOT_FOUND, ex);
        verify(repository, never()).save(any());
    }

    @Test
    void eliminarItem_productoNoEstaEnElCarrito_lanza404() {
        when(repository.findByClienteId(CLIENTE))
                .thenReturn(Optional.of(carritoCon(item(701L, 4990, 1))));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.eliminarItem(CLIENTE, 999L));

        assertStatus(HttpStatus.NOT_FOUND, ex);
        verify(repository, never()).save(any());
    }

    @Test
    void vaciar_conCarrito_quitaTodosLosItemsYGuarda() {
        Carrito existente = carritoCon(item(701L, 4990, 1), item(702L, 18900, 2));
        when(repository.findByClienteId(CLIENTE)).thenReturn(Optional.of(existente));

        service.vaciar(CLIENTE);

        assertTrue(existente.getItems().isEmpty());
        verify(repository).save(existente);
    }

    @Test
    void vaciar_sinCarrito_noHaceNada() {
        when(repository.findByClienteId(CLIENTE)).thenReturn(Optional.empty());

        service.vaciar(CLIENTE);

        verify(repository, never()).save(any());
        verify(repository, never()).deleteById(anyLong());
    }
}
