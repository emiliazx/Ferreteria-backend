package ferreteria.productos.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ferreteria.productos.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoInicialTest {

    private static final Set<String> CATEGORIAS = Set.of("construccion", "pintura", "herramientas",
            "gasfiteria", "electricidad", "tornillos", "madera", "seguridad", "jardin");

    @Mock
    private ProductoRepository repository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void run_tablaConProductos_noCargaNada() {
        when(repository.count()).thenReturn(5L);

        new CatalogoInicial(repository, jdbcTemplate, new ObjectMapper()).run();

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void run_tablaVacia_insertaElCatalogoConsusIdsOriginales() {
        when(repository.count()).thenReturn(0L);

        new CatalogoInicial(repository, jdbcTemplate, new ObjectMapper()).run();

        ArgumentCaptor<List<Object[]>> captor = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(eq(CatalogoInicial.INSERT), captor.capture());
        List<Object[]> filas = captor.getValue();

        assertEquals(36, filas.size());
        assertTrue(filas.stream().anyMatch(f -> Long.valueOf(701L).equals(f[0])));
        assertEquals(36, filas.stream().map(f -> f[0]).distinct().count());
        for (Object[] fila : filas) {
            assertEquals(6, fila.length);
            assertTrue(CATEGORIAS.contains((String) fila[1]), "categoría inesperada: " + fila[1]);
            assertFalse(((String) fila[2]).isBlank());
            assertTrue((Integer) fila[4] > 0);
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void run_errorLeyendoElSeed_lanzaIllegalState() throws Exception {
        ObjectMapper mapperQueFalla = mock(ObjectMapper.class);
        when(repository.count()).thenReturn(0L);
        when(mapperQueFalla.readValue(any(InputStream.class), any(TypeReference.class)))
                .thenThrow(new IOException("boom"));

        CatalogoInicial loader = new CatalogoInicial(repository, jdbcTemplate, mapperQueFalla);

        IllegalStateException ex = assertThrows(IllegalStateException.class, loader::run);
        assertTrue(ex.getMessage().contains(CatalogoInicial.RECURSO));
        verifyNoInteractions(jdbcTemplate);
    }
}
