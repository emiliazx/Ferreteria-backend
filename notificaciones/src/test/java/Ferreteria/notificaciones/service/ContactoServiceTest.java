package Ferreteria.notificaciones.service;

import Ferreteria.notificaciones.dto.ContactoRequest;
import Ferreteria.notificaciones.dto.NotificacionRequest;
import Ferreteria.notificaciones.model.MensajeContacto;
import Ferreteria.notificaciones.repository.MensajeContactoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactoServiceTest {

    @Mock
    private MensajeContactoRepository repository;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ContactoService contactoService;

    @Test
    void crearMensaje_debeGuardarConsultaConTurnoInicialCliente() {
        ContactoRequest req = new ContactoRequest();
        req.setNombre("Camila Rojas");
        req.setTelefono("+56 9 8412 3301");
        req.setEmail("camila.rojas@correo.cl");
        req.setMensaje("¿Tienen stock de taladro percutor?");

        when(repository.save(any(MensajeContacto.class))).thenAnswer(inv -> {
            MensajeContacto m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });

        MensajeContacto resultado = contactoService.crearMensaje(req);

        assertEquals(10L, resultado.getId());
        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals(1, resultado.getHilo().size());
        assertEquals("CLIENTE", resultado.getHilo().get(0).getRemitente());
        assertEquals("¿Tienen stock de taladro percutor?", resultado.getHilo().get(0).getTexto());
        verify(repository, times(1)).save(any(MensajeContacto.class));
        verify(notificacionService, times(1)).crear(any(NotificacionRequest.class));
    }

    @Test
    void responderMensaje_debeActualizarEstadoYEnviarCorreo() {
        MensajeContacto existente = MensajeContacto.builder()
                .id(10L)
                .nombre("Camila Rojas")
                .email("camila.rojas@correo.cl")
                .mensaje("¿Tienen stock de taladro percutor?")
                .estado("PENDIENTE")
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(existente));
        when(repository.save(any(MensajeContacto.class))).thenAnswer(inv -> inv.getArgument(0));

        MensajeContacto actualizado = contactoService.responderMensaje(10L, "Sí, tenemos 8 unidades.");

        assertEquals("RESPONDIDO", actualizado.getEstado());
        assertEquals("Sí, tenemos 8 unidades.", actualizado.getRespuesta());
        assertNotNull(actualizado.getRespondidoAt());
        assertEquals(1, actualizado.getHilo().size());
        assertEquals("ADMIN", actualizado.getHilo().get(0).getRemitente());
        verify(emailService, times(1)).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void agregarMensajeHilo_clienteDebeVolverEstadoPendiente() {
        MensajeContacto existente = MensajeContacto.builder()
                .id(10L)
                .nombre("Camila Rojas")
                .email("camila.rojas@correo.cl")
                .mensaje("Consulta inicial")
                .estado("RESPONDIDO")
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(existente));
        when(repository.save(any(MensajeContacto.class))).thenAnswer(inv -> inv.getArgument(0));

        MensajeContacto actualizado = contactoService.agregarMensajeHilo(10L, "CLIENTE", "¿Incluye batería?");

        assertEquals("PENDIENTE", actualizado.getEstado());
        assertEquals(1, actualizado.getHilo().size());
        assertEquals("CLIENTE", actualizado.getHilo().get(0).getRemitente());
        assertEquals("¿Incluye batería?", actualizado.getHilo().get(0).getTexto());
    }

    @Test
    void agregarMensajeHilo_idInexistenteDebeLanzarExcepcion() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> contactoService.agregarMensajeHilo(999L, "CLIENTE", "Hola")
        );
    }
}