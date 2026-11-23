package ferreteria.productos.service;

import ferreteria.productos.dto.ProductoRequest;
import ferreteria.productos.dto.ProductoResponse;
import ferreteria.productos.model.Producto;
import ferreteria.productos.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoService service;

    private Producto producto(Long id, String categoria) {
        return Producto.builder()
                .id(id)
                .categoria(categoria)
                .titulo("Producto " + id)
                .descripcion("Descripción " + id)
                .precio(1000 * id.intValue())
                .imagen("/img/p" + id + ".jpg")
                .build();
    }

    private ProductoRequest request(String categoria) {
        ProductoRequest req = new ProductoRequest();
        req.setCategoria(categoria);
        req.setTitulo("  Martillo de Carpintero  ");
        req.setDescripcion("  Martillo con mango de fibra.  ");
        req.setPrecio(8990);
        req.setImagen("/img/herr1.jpg");
        return req;
    }

    private void assertStatus(HttpStatus esperado, ResponseStatusException ex) {
        assertEquals(esperado, ex.getStatusCode());
    }

    @Test
    void listar_sinCategoria_devuelveTodosLosProductos() {
        when(repository.findAllByOrderByIdAsc())
                .thenReturn(List.of(producto(1L, "pintura"), producto(2L, "madera")));

        List<ProductoResponse> resultado = service.listar(null);

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("pintura", resultado.get(0).getCategoria());
        assertEquals("madera", resultado.get(1).getCategoria());
        verify(repository, never()).findByCategoriaOrderByIdAsc(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void listar_categoriaVaciaONula_noFiltra(String categoria) {
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of());

        assertTrue(service.listar(categoria).isEmpty());

        verify(repository).findAllByOrderByIdAsc();
        verify(repository, never()).findByCategoriaOrderByIdAsc(any());
    }

    @Test
    void listar_conCategoria_normalizaYFiltra() {
        when(repository.findByCategoriaOrderByIdAsc("pintura"))
                .thenReturn(List.of(producto(3L, "pintura")));

        List<ProductoResponse> resultado = service.listar("  PINTURA ");

        assertEquals(1, resultado.size());
        assertEquals("pintura", resultado.get(0).getCategoria());
        verify(repository, never()).findAllByOrderByIdAsc();
    }

    @Test
    void listar_categoriaInvalida_lanza400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.listar("juguetes"));

        assertStatus(HttpStatus.BAD_REQUEST, ex);
        assertTrue(ex.getReason().contains("Categoría inválida"));
        verifyNoInteractions(repository);
    }

    @Test
    void obtener_existente_devuelveProducto() {
        when(repository.findById(5L)).thenReturn(Optional.of(producto(5L, "madera")));

        ProductoResponse r = service.obtener(5L);

        assertEquals(5L, r.getId());
        assertEquals("madera", r.getCategoria());
        assertEquals("Producto 5", r.getTitulo());
        assertEquals("Descripción 5", r.getDescripcion());
        assertEquals(5000, r.getPrecio());
        assertEquals("/img/p5.jpg", r.getImagen());
    }

    @Test
    void obtener_noExiste_lanza404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.obtener(99L));

        assertStatus(HttpStatus.NOT_FOUND, ex);
        assertEquals("Producto no encontrado", ex.getReason());
    }

    @Test
    void crear_valido_guardaNormalizadoYDevuelveRespuesta() {
        when(repository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProductoResponse r = service.crear(request(" Herramientas "));

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(repository).save(captor.capture());
        Producto guardado = captor.getValue();
        assertNotNull(guardado.getId());
        assertEquals("herramientas", guardado.getCategoria());
        assertEquals("Martillo de Carpintero", guardado.getTitulo());
        assertEquals("Martillo con mango de fibra.", guardado.getDescripcion());
        assertEquals(8990, guardado.getPrecio());
        assertEquals("/img/herr1.jpg", guardado.getImagen());

        assertEquals(10L, r.getId());
        assertEquals("herramientas", r.getCategoria());
        assertEquals("Martillo de Carpintero", r.getTitulo());
        assertEquals(8990, r.getPrecio());
    }

    @ParameterizedTest
    @ValueSource(strings = {"construccion", "pintura", "herramientas", "gasfiteria", "electricidad",
            "tornillos", "madera", "seguridad", "jardin"})
    void crear_aceptaTodasLasCategoriasDelCatalogo(String categoria) {
        when(repository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse r = service.crear(request(categoria));

        assertEquals(categoria, r.getCategoria());
    }

    @Test
    void categoriasPermitidas_coincidenConLasDelFrontend() {
        assertEquals(9, ProductoService.CATEGORIAS.size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "ferreteria", "pinturas"})
    void crear_categoriaInvalida_lanza400SinGuardar(String categoria) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.crear(request(categoria)));

        assertStatus(HttpStatus.BAD_REQUEST, ex);
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_existente_modificaTodosLosCampos() {
        Producto existente = producto(7L, "madera");
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse r = service.actualizar(7L, request("SEGURIDAD"));

        assertEquals(7L, r.getId());
        assertEquals("seguridad", r.getCategoria());
        assertEquals("Martillo de Carpintero", r.getTitulo());
        assertEquals("Martillo con mango de fibra.", r.getDescripcion());
        assertEquals(8990, r.getPrecio());
        assertEquals("/img/herr1.jpg", r.getImagen());
        verify(repository).save(existente);
    }

    @Test
    void actualizar_noExiste_lanza404SinGuardar() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.actualizar(42L, request("pintura")));

        assertStatus(HttpStatus.NOT_FOUND, ex);
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_categoriaInvalida_lanza400SinConsultarNiGuardar() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.actualizar(7L, request("juguetes")));

        assertStatus(HttpStatus.BAD_REQUEST, ex);
        verify(repository, never()).findById(anyLong());
        verify(repository, never()).save(any());
    }

    @Test
    void eliminar_existente_borraPorId() {
        when(repository.existsById(3L)).thenReturn(true);

        service.eliminar(3L);

        verify(repository).deleteById(3L);
    }

    @Test
    void eliminar_noExiste_lanza404SinBorrar() {
        when(repository.existsById(3L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.eliminar(3L));

        assertStatus(HttpStatus.NOT_FOUND, ex);
        verify(repository, never()).deleteById(anyLong());
    }
}
