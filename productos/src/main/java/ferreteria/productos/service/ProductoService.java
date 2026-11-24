package ferreteria.productos.service;

import ferreteria.productos.dto.ProductoRequest;
import ferreteria.productos.dto.ProductoResponse;
import ferreteria.productos.model.Producto;
import ferreteria.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductoService {

    static final Set<String> CATEGORIAS = Set.of(
            "construccion", "pintura", "herramientas", "gasfiteria", "electricidad",
            "tornillos", "madera", "seguridad", "jardin");

    private final ProductoRepository repository;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(String categoria) {
        List<Producto> productos;
        if (categoria == null || categoria.isBlank()) {
            productos = repository.findAllByOrderByIdAsc();
        } else {
            productos = repository.findByCategoriaOrderByIdAsc(normalizarCategoria(categoria));
        }
        return productos.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        Producto producto = Producto.builder()
                .categoria(normalizarCategoria(req.getCategoria()))
                .titulo(req.getTitulo().trim())
                .descripcion(req.getDescripcion().trim())
                .precio(req.getPrecio())
                .imagen(req.getImagen())
                .build();
        return toResponse(repository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest req) {
        String categoria = normalizarCategoria(req.getCategoria());
        Producto producto = buscar(id);
        producto.setCategoria(categoria);
        producto.setTitulo(req.getTitulo().trim());
        producto.setDescripcion(req.getDescripcion().trim());
        producto.setPrecio(req.getPrecio());
        producto.setImagen(req.getImagen());
        return toResponse(repository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        repository.deleteById(id);
    }

    private Producto buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    private String normalizarCategoria(String categoria) {
        String slug = categoria == null ? "" : categoria.trim().toLowerCase(Locale.ROOT);
        if (!CATEGORIAS.contains(slug)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Categoría inválida. Use: " + String.join(", ", CATEGORIAS.stream().sorted().toList()));
        }
        return slug;
    }

    private ProductoResponse toResponse(Producto p) {
        return ProductoResponse.builder()
                .id(p.getId())
                .categoria(p.getCategoria())
                .titulo(p.getTitulo())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .imagen(p.getImagen())
                .build();
    }
}
