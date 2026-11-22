package ferreteria.pagos.controller;

import ferreteria.pagos.dto.PagoResponse;
import ferreteria.pagos.dto.ProcesarPagoRequest;
import ferreteria.pagos.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Procesamiento de pagos simulados con comprobante")
public class PagoController {

    private final PagoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Procesar un pago (simula tarjeta / transferencia / webpay y genera comprobante)")
    public PagoResponse procesar(@Valid @RequestBody ProcesarPagoRequest request) {
        return service.procesar(request);
    }
    @GetMapping
@Operation(summary = "Listar todos los pagos (admin/vendedor)")
public List<PagoResponse> listarTodos() {
    return service.listarTodos();
}

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pago por ID")
    public PagoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/orden/{ordenId}")
    @Operation(summary = "Listar pagos de una orden")
    public List<PagoResponse> porOrden(@PathVariable Long ordenId) {
        return service.porOrden(ordenId);
    }

    @GetMapping("/comprobante/{codigo}")
    @Operation(summary = "Buscar pago por código de comprobante")
    public PagoResponse porComprobante(@PathVariable String codigo) {
        return service.porComprobante(codigo);
    }
}
