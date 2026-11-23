package ferreteria.productos.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ferreteria.productos.model.Producto;
import ferreteria.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "productos.seed.enabled", havingValue = "true", matchIfMissing = true)
public class CatalogoInicial implements CommandLineRunner {

    static final String RECURSO = "seed/productos.json";
    static final String INSERT = "INSERT INTO productos (id, categoria, titulo, descripcion, precio, imagen) VALUES (?, ?, ?, ?, ?, ?)";

    private final ProductoRepository repository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        List<Producto> productos;
        try (InputStream in = new ClassPathResource(RECURSO).getInputStream()) {
            productos = objectMapper.readValue(in, new TypeReference<List<Producto>>() { });
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + RECURSO, e);
        }
        List<Object[]> filas = productos.stream()
                .map(p -> new Object[]{p.getId(), p.getCategoria(), p.getTitulo(),
                        p.getDescripcion(), p.getPrecio(), p.getImagen()})
                .toList();
        jdbcTemplate.batchUpdate(INSERT, filas);
        log.info("Catálogo inicial cargado: {} productos", filas.size());
    }
}
