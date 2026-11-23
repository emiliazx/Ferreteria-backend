package ferreteria.carrito.controller;

import ferreteria.carrito.dto.ActualizarCantidadRequest;
import ferreteria.carrito.dto.AgregarItemRequest;
import ferreteria.carrito.dto.CarritoResponse;
import ferreteria.carrito.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carritos")
@RequiredArgsConstructor
@Tag(name = "Carrito", description = "Carrito de compras por cliente (usuario o invitado)")
public class CarritoController {

    private final CarritoService service;

    @GetMapping("/{clienteId}")
    @Operation(summary = "Obtener el carrito del cliente (vacío si aún no tiene)")
    public CarritoResponse obtener(@PathVariable String clienteId) {
        return service.obtener(clienteId);
    }

    @PostMapping("/{clienteId}/items")
    @Operation(summary = "Agregar un producto (suma cantidad si ya estaba en el carrito)")
    public CarritoResponse agregar(@PathVariable String clienteId,
                                   @Valid @RequestBody AgregarItemRequest request) {
        return service.agregar(clienteId, request);
    }

    @PutMapping("/{clienteId}/items/{productoId}")
    @Operation(summary = "Cambiar la cantidad de un producto del carrito")
    public CarritoResponse actualizarCantidad(@PathVariable String clienteId,
                                              @PathVariable Long productoId,
                                              @Valid @RequestBody ActualizarCantidadRequest request) {
        return service.actualizarCantidad(clienteId, productoId, request);
    }

    @DeleteMapping("/{clienteId}/items/{productoId}")
    @Operation(summary = "Quitar un producto del carrito")
    public CarritoResponse eliminarItem(@PathVariable String clienteId, @PathVariable Long productoId) {
        return service.eliminarItem(clienteId, productoId);
    }

    @DeleteMapping("/{clienteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Vaciar el carrito (por ejemplo, tras pagar)")
    public void vaciar(@PathVariable String clienteId) {
        service.vaciar(clienteId);
    }
}
