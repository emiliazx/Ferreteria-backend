package ferreteria.carrito.service;

import ferreteria.carrito.client.ProductosClient;
import ferreteria.carrito.dto.ActualizarCantidadRequest;
import ferreteria.carrito.dto.AgregarItemRequest;
import ferreteria.carrito.dto.CarritoResponse;
import ferreteria.carrito.dto.ItemCarritoResponse;
import ferreteria.carrito.dto.ProductoInfo;
import ferreteria.carrito.model.Carrito;
import ferreteria.carrito.model.CarritoItem;
import ferreteria.carrito.repository.CarritoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository repository;
    private final ProductosClient productosClient;

    @Transactional(readOnly = true)
    public CarritoResponse obtener(String clienteId) {
        return repository.findByClienteId(clienteId)
                .map(this::toResponse)
                .orElseGet(() -> toResponse(new Carrito(clienteId)));
    }

    @Transactional
    public CarritoResponse agregar(String clienteId, AgregarItemRequest req) {
        ProductoInfo producto = productosClient.obtener(req.getProductoId());
        int cantidad = req.getCantidad() == null ? 1 : req.getCantidad();

        Carrito carrito = repository.findByClienteId(clienteId)
                .orElseGet(() -> new Carrito(clienteId));

        CarritoItem item = carrito.buscarItem(req.getProductoId()).orElse(null);
        if (item == null) {
            item = new CarritoItem();
            item.setProductoId(req.getProductoId());
            item.setCantidad(cantidad);
            carrito.agregarItem(item);
        } else {
            item.setCantidad(item.getCantidad() + cantidad);
        }

        item.setTitulo(producto.titulo());
        item.setPrecioUnitario(producto.precio());
        item.setImagen(producto.imagen());

        return toResponse(repository.save(carrito));
    }

    @Transactional
    public CarritoResponse actualizarCantidad(String clienteId, Long productoId, ActualizarCantidadRequest req) {
        Carrito carrito = buscarCarrito(clienteId);
        CarritoItem item = carrito.buscarItem(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El producto no está en el carrito"));
        item.setCantidad(req.getCantidad());
        return toResponse(repository.save(carrito));
    }

    @Transactional
    public CarritoResponse eliminarItem(String clienteId, Long productoId) {
        Carrito carrito = buscarCarrito(clienteId);
        if (!carrito.quitarItem(productoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no está en el carrito");
        }
        return toResponse(repository.save(carrito));
    }

    @Transactional
    public void vaciar(String clienteId) {
        repository.findByClienteId(clienteId).ifPresent(carrito -> {
            carrito.vaciar();
            repository.save(carrito);
        });
    }

    private Carrito buscarCarrito(String clienteId) {
        return repository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no encontrado"));
    }

    private CarritoResponse toResponse(Carrito carrito) {
        List<ItemCarritoResponse> items = carrito.getItems().stream()
                .map(i -> ItemCarritoResponse.builder()
                        .productoId(i.getProductoId())
                        .titulo(i.getTitulo())
                        .precioUnitario(i.getPrecioUnitario())
                        .imagen(i.getImagen())
                        .cantidad(i.getCantidad())
                        .subtotal(i.getPrecioUnitario() * i.getCantidad())
                        .build())
                .toList();
        return CarritoResponse.builder()
                .clienteId(carrito.getClienteId())
                .items(items)
                .totalUnidades(items.stream().mapToInt(ItemCarritoResponse::getCantidad).sum())
                .subtotal(items.stream().mapToInt(ItemCarritoResponse::getSubtotal).sum())
                .build();
    }
}
