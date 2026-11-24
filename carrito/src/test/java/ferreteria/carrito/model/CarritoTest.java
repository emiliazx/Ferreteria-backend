package ferreteria.carrito.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarritoTest {

    private CarritoItem item(Long productoId) {
        CarritoItem item = new CarritoItem();
        item.setProductoId(productoId);
        item.setTitulo("Producto " + productoId);
        item.setPrecioUnitario(1000);
        item.setCantidad(1);
        return item;
    }

    @Test
    void nuevoCarrito_empiezaVacioYConClienteId() {
        Carrito carrito = new Carrito("42");

        assertEquals("42", carrito.getClienteId());
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void agregarItem_loVinculaAlCarrito() {
        Carrito carrito = new Carrito("42");
        CarritoItem item = item(701L);

        carrito.agregarItem(item);

        assertEquals(1, carrito.getItems().size());
        assertSame(carrito, item.getCarrito());
    }

    @Test
    void buscarItem_devuelveElItemOVacio() {
        Carrito carrito = new Carrito("42");
        carrito.agregarItem(item(701L));

        assertTrue(carrito.buscarItem(701L).isPresent());
        assertTrue(carrito.buscarItem(999L).isEmpty());
    }

    @Test
    void quitarItem_indicaSiEncontroElProducto() {
        Carrito carrito = new Carrito("42");
        carrito.agregarItem(item(701L));
        carrito.agregarItem(item(702L));

        assertTrue(carrito.quitarItem(701L));
        assertFalse(carrito.quitarItem(701L));
        assertEquals(1, carrito.getItems().size());
        assertEquals(702L, carrito.getItems().get(0).getProductoId());
    }

    @Test
    void vaciar_quitaTodosLosItems() {
        Carrito carrito = new Carrito("42");
        carrito.agregarItem(item(701L));
        carrito.agregarItem(item(702L));

        carrito.vaciar();

        assertTrue(carrito.getItems().isEmpty());
    }
}
