package ferreteria.ordenes.controller;

import ferreteria.ordenes.dto.*;
import ferreteria.ordenes.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
@Tag(name = "Órdenes", description = "Creación y consulta de pedidos")
public class OrdenController {

    private final OrdenService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una nueva orden (snapshot de productos del carrito)")
    public OrdenResponse crear(@Valid @RequestBody CrearOrdenRequest request) {
        return service.crear(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden por ID")
    public OrdenResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/usuario/{userId}")
    @Operation(summary = "Listar órdenes de un usuario")
    public List<OrdenResponse> porUsuario(@PathVariable Long userId) {
        return service.listarPorUsuario(userId);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Listar órdenes por email")
    public List<OrdenResponse> porEmail(@PathVariable String email) {
        return service.listarPorEmail(email);
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de la orden")
    public OrdenResponse actualizarEstado(@PathVariable Long id,
                                          @Valid @RequestBody ActualizarEstadoRequest request) {
        return service.actualizarEstado(id, request.getEstado());
    }

    @PutMapping("/{id}/pagado")
    @Operation(summary = "Marcar orden como PAGADO (llamado desde ms-pagos)")
    public OrdenResponse marcarPagado(@PathVariable Long id) {
        return service.marcarPagado(id);
    }
}
