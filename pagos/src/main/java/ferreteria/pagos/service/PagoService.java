package ferreteria.pagos.service;

import ferreteria.pagos.dto.PagoResponse;
import ferreteria.pagos.dto.ProcesarPagoRequest;
import ferreteria.pagos.model.Pago;
import ferreteria.pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {

    private static final Set<String> METODOS = Set.of("TARJETA", "TRANSFERENCIA", "WEBPAY");

    private final PagoRepository repository;

    @Value("${ordenes.service-url:http://ordenes:8083}")
    private String ordenesServiceUrl;

    private final RestClient.Builder restClientBuilder = RestClient.builder();

    @Transactional
    public PagoResponse procesar(ProcesarPagoRequest req) {
        String metodo = req.getMetodo().trim().toUpperCase();
        if (!METODOS.contains(metodo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Método inválido. Use: TARJETA, TRANSFERENCIA o WEBPAY");
        }

        String tarjetaMask = null;
        String titular = req.getTitular();
        String mensaje;
        String estado;

        if ("TARJETA".equals(metodo)) {
            validarTarjeta(req);
            String digits = req.getNumeroTarjeta().replaceAll("\\D", "");
            tarjetaMask = "**** **** **** " + digits.substring(digits.length() - 4);

            // Simulación: rechazar si termina en 0000 o CVV = 000
            boolean rechazar = digits.endsWith("0000")
                    || (req.getCvv() != null && req.getCvv().equals("000"));

            if (rechazar) {
                estado = "RECHAZADO";
                mensaje = "Pago rechazado por el emisor de la tarjeta (simulación).";
            } else {
                estado = "APROBADO";
                mensaje = "Pago con tarjeta aprobado correctamente.";
            }
        } else {
            // Transferencia / Webpay: siempre aprobado en simulación
            estado = "APROBADO";
            mensaje = "Pago por " + metodo + " registrado correctamente (simulación).";
        }

        String comprobante = generarComprobante();

        Pago pago = Pago.builder()
                .ordenId(req.getOrdenId())
                .monto(req.getMonto())
                .metodo(metodo)
                .estado(estado)
                .tarjetaMask(tarjetaMask)
                .titular(titular)
                .comprobante(comprobante)
                .mensaje(mensaje)
                .fecha(LocalDateTime.now())
                .build();

        Pago guardado = repository.save(pago);

        // Si se aprobó, intentar marcar la orden como PAGADO
        if ("APROBADO".equals(estado)) {
            try {
                marcarOrdenPagada(req.getOrdenId());
            } catch (Exception e) {
                log.warn("No se pudo marcar orden {} como PAGADO: {}", req.getOrdenId(), e.getMessage());
            }
        }

        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public PagoResponse obtener(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado")));
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> porOrden(Long ordenId) {
        return repository.findByOrdenIdOrderByFechaDesc(ordenId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagoResponse porComprobante(String comprobante) {
        return repository.findByComprobante(comprobante)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comprobante no encontrado"));
    }
    @Transactional(readOnly = true)
public List<PagoResponse> listarTodos() {
    return repository.findAllByOrderByFechaDesc().stream()
            .map(this::toResponse)
            .toList();
}

    private void validarTarjeta(ProcesarPagoRequest req) {
        if (req.getNumeroTarjeta() == null || req.getNumeroTarjeta().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Número de tarjeta requerido");
        }
        String digits = req.getNumeroTarjeta().replaceAll("\\D", "");
        if (!req.getNumeroTarjeta().matches("[0-9 -]+") || digits.length() < 13
                || digits.length() > 19 || !esNumeroValido(digits)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Número de tarjeta inválido");
        }
        if (req.getTitular() == null || req.getTitular().trim().length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Titular de tarjeta requerido");
        }
        if (req.getCvv() == null || !req.getCvv().matches("\\d{3,4}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CVV inválido");
        }
        if (req.getVencimiento() == null || !req.getVencimiento().matches("\\d{2}/\\d{2}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vencimiento debe ser MM/YY");
        }
        String vencimiento = req.getVencimiento();
        int mes = Integer.parseInt(vencimiento.substring(0, 2));
        int anio = 2000 + Integer.parseInt(vencimiento.substring(3));
        if (mes < 1 || mes > 12 || YearMonth.of(anio, mes).isBefore(YearMonth.now(ZoneId.of("America/Santiago")))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vencimiento inválido o tarjeta vencida");
        }
    }

    private boolean esNumeroValido(String digits) {
        int suma = 0;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digito = digits.charAt(i) - '0';
            if ((digits.length() - i) % 2 == 0) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
        }
        return suma > 0 && suma % 10 == 0;
    }

    private String generarComprobante() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "COMP-" + fecha + "-" + random;
    }

    private void marcarOrdenPagada(Long ordenId) {
        RestClient client = restClientBuilder.baseUrl(ordenesServiceUrl).build();
        client.put()
                .uri("/api/ordenes/{id}/pagado", ordenId)
                .retrieve()
                .toBodilessEntity();
    }

    private PagoResponse toResponse(Pago p) {
        return PagoResponse.builder()
                .id(p.getId())
                .ordenId(p.getOrdenId())
                .monto(p.getMonto())
                .metodo(p.getMetodo())
                .estado(p.getEstado())
                .tarjetaMask(p.getTarjetaMask())
                .titular(p.getTitular())
                .comprobante(p.getComprobante())
                .mensaje(p.getMensaje())
                .fecha(p.getFecha())
                .build();
    }
}
