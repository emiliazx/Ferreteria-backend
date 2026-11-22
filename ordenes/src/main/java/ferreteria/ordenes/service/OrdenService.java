package ferreteria.ordenes.service;

import ferreteria.ordenes.dto.*;
import ferreteria.ordenes.model.Orden;
import ferreteria.ordenes.model.OrdenItem;
import ferreteria.ordenes.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrdenService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "PENDIENTE", "PAGADO", "ENVIADO", "CANCELADO", "ENTREGADO"
    );

    private final OrdenRepository repository;

    @Transactional
    public OrdenResponse crear(CrearOrdenRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La orden debe tener al menos un ítem");
        }

        BigDecimal subtotal = req.getItems().stream()
                .map(i -> i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal despacho = req.getCostoDespacho() != null ? req.getCostoDespacho() : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(despacho);

        Orden orden = Orden.builder()
                .userId(req.getUserId())
                .email(req.getEmail().trim().toLowerCase())
                .nombreCliente(req.getNombreCliente().trim())
                .telefono(req.getTelefono())
                .direccion(req.getDireccion().trim())
                .comuna(req.getComuna())
                .region(req.getRegion())
                .referencia(req.getReferencia())
                .transportista(req.getTransportista())
                .costoDespacho(despacho)
                .subtotal(subtotal)
                .total(total)
                .estado("PENDIENTE")
                .metodoPago(req.getMetodoPago())
                .build();

        for (OrdenItemRequest itemReq : req.getItems()) {
            OrdenItem item = OrdenItem.builder()
                    .productoId(itemReq.getProductoId())
                    .titulo(itemReq.getTitulo())
                    .precioUnitario(itemReq.getPrecioUnitario())
                    .cantidad(itemReq.getCantidad())
                    .imagen(itemReq.getImagen())
                    .build();
            orden.addItem(item);
        }

        Orden guardada = repository.save(orden);
        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public OrdenResponse obtener(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarPorUsuario(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarPorEmail(String email) {
        return repository.findByEmailOrderByCreatedAtDesc(email.trim().toLowerCase()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrdenResponse actualizarEstado(Long id, String estado) {
        if (!ESTADOS_VALIDOS.contains(estado.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado inválido. Permitidos: " + ESTADOS_VALIDOS);
        }
        Orden orden = findOrThrow(id);
        orden.setEstado(estado.toUpperCase());
        return toResponse(repository.save(orden));
    }

    @Transactional
    public OrdenResponse marcarPagado(Long id) {
        Orden orden = findOrThrow(id);
        if ("CANCELADO".equals(orden.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede pagar una orden cancelada");
        }
        orden.setEstado("PAGADO");
        return toResponse(repository.save(orden));
    }
    @Transactional(readOnly = true)
public List<OrdenResponse> listarTodas() {
    return repository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .toList();
}

@Transactional(readOnly = true)
public List<OrdenResponse> listarPorEstado(String estado) {
    return repository.findByEstadoOrderByCreatedAtDesc(estado.toUpperCase()).stream()
            .map(this::toResponse)
            .toList();
}

    private Orden findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada: " + id));
    }

    private OrdenResponse toResponse(Orden o) {
        List<OrdenItemResponse> items = o.getItems().stream()
                .map(i -> OrdenItemResponse.builder()
                        .id(i.getId())
                        .productoId(i.getProductoId())
                        .titulo(i.getTitulo())
                        .precioUnitario(i.getPrecioUnitario())
                        .cantidad(i.getCantidad())
                        .imagen(i.getImagen())
                        .subtotal(i.getSubtotal())
                        .build())
                .toList();

        return OrdenResponse.builder()
                .id(o.getId())
                .userId(o.getUserId())
                .email(o.getEmail())
                .nombreCliente(o.getNombreCliente())
                .telefono(o.getTelefono())
                .direccion(o.getDireccion())
                .comuna(o.getComuna())
                .region(o.getRegion())
                .referencia(o.getReferencia())
                .transportista(o.getTransportista())
                .costoDespacho(o.getCostoDespacho())
                .subtotal(o.getSubtotal())
                .total(o.getTotal())
                .estado(o.getEstado())
                .metodoPago(o.getMetodoPago())
                .createdAt(o.getCreatedAt())
                .items(items)
                .build();
    }
}
