package ferreteria.auth.service;

import ferreteria.auth.dto.AuthResponse;
import ferreteria.auth.dto.LoginRequest;
import ferreteria.auth.dto.RegisterRequest;
import ferreteria.auth.dto.ResetPasswordRequest;
import ferreteria.auth.dto.UsuarioResponse;
import ferreteria.auth.dto.UsuarioResumenResponse;
import ferreteria.auth.model.Usuario;
import ferreteria.auth.repository.UsuarioRepository;
import ferreteria.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_conDatosValidos_debeCrearUsuarioYRetornarToken() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("  Emilia Zamora  ");
        req.setEmail("  EMILIA@CORREO.CL  ");
        req.setPassword("claveSegura123");

        when(usuarioRepository.existsByEmail("emilia@correo.cl")).thenReturn(false);
        when(passwordEncoder.encode("claveSegura123")).thenReturn("$2a$10$hashSimulado");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(1L, "emilia@correo.cl", "USER")).thenReturn("jwt-token-valido");

        AuthResponse res = authService.register(req);

     assertEquals("jwt-token-valido", res.getToken());
assertEquals(1L, res.getUserId());
assertEquals("emilia@correo.cl", res.getEmail());
assertEquals("Emilia Zamora", res.getNombre());
assertEquals("USER", res.getRol());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(jwtService, times(1)).generateToken(1L, "emilia@correo.cl", "USER");
    }

    @Test
    void register_conEmailExistente_debeLanzarConflict() {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Emilia Zamora");
        req.setEmail("emilia@correo.cl");
        req.setPassword("claveSegura123");

        when(usuarioRepository.existsByEmail("emilia@correo.cl")).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(req)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void login_conCredencialesValidas_debeRetornarToken() {
        LoginRequest req = new LoginRequest();
        req.setEmail("EMILIA@CORREO.CL");
        req.setPassword("claveSegura123");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("emilia@correo.cl");
        usuario.setNombre("Emilia Zamora");
        usuario.setPassword("$2a$10$hashSimulado");
        usuario.setRol("USER");
        usuario.setActivo(true);

        when(usuarioRepository.findByEmail("emilia@correo.cl")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("claveSegura123", "$2a$10$hashSimulado")).thenReturn(true);
        when(jwtService.generateToken(1L, "emilia@correo.cl", "USER")).thenReturn("jwt-login-ok");

        AuthResponse res = authService.login(req);

        assertEquals("jwt-login-ok", res.getToken());
        assertEquals("emilia@correo.cl", res.getEmail());
    }

    @Test
    void login_conPasswordIncorrecta_debeLanzarUnauthorized() {
        LoginRequest req = new LoginRequest();
        req.setEmail("emilia@correo.cl");
        req.setPassword("claveMala");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("emilia@correo.cl");
        usuario.setPassword("$2a$10$hashSimulado");
        usuario.setActivo(true);

        when(usuarioRepository.findByEmail("emilia@correo.cl")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("claveMala", "$2a$10$hashSimulado")).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(req)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void login_conUsuarioDesactivado_debeLanzarUnauthorized() {
        LoginRequest req = new LoginRequest();
        req.setEmail("emilia@correo.cl");
        req.setPassword("claveSegura123");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("emilia@correo.cl");
        usuario.setActivo(false);

        when(usuarioRepository.findByEmail("emilia@correo.cl")).thenReturn(Optional.of(usuario));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(req)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void me_conEmailExistente_debeRetornarUsuarioResponse() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setEmail("admin@ferreteria.cl");
        usuario.setNombre("Admin");
        usuario.setRol("ADMIN");
        usuario.setActivo(true);

        when(usuarioRepository.findByEmail("admin@ferreteria.cl")).thenReturn(Optional.of(usuario));

        UsuarioResponse res = authService.me("admin@ferreteria.cl");

        assertEquals(2L, res.getId());
        assertEquals("ADMIN", res.getRol());
        assertTrue(res.getActivo());
    }

    @Test
    void resetPassword_conCuentaActiva_debeActualizarPassword() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("emilia@correo.cl");
        req.setNewPassword("nuevaClave456");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("emilia@correo.cl");
        usuario.setActivo(true);

        when(usuarioRepository.findByEmail("emilia@correo.cl")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("nuevaClave456")).thenReturn("$2a$10$nuevoHash");

        assertDoesNotThrow(() -> authService.resetPassword(req));

        assertEquals("$2a$10$nuevoHash", usuario.getPassword());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void resetPassword_conCuentaInactiva_debeLanzarForbidden() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("inactivo@correo.cl");
        req.setNewPassword("nuevaClave456");

        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setEmail("inactivo@correo.cl");
        usuario.setActivo(false);

        when(usuarioRepository.findByEmail("inactivo@correo.cl")).thenReturn(Optional.of(usuario));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.resetPassword(req)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void listarUsuarios_debeAplicarValoresPorDefectoCuandoFaltanCampos() {
        Usuario u = new Usuario();
        u.setId(5L);
        u.setEmail("sin-nombre@correo.cl");
        u.setNombre("");
        u.setRol(null);
        u.setActivo(null);

        when(usuarioRepository.findAll()).thenReturn(List.of(u));

        List<UsuarioResumenResponse> lista = authService.listarUsuarios();

        assertEquals(1, lista.size());
        assertEquals("sin-nombre@correo.cl", lista.get(0).getNombre());
        assertEquals("USER", lista.get(0).getRol());
        assertTrue(lista.get(0).getActivo());
    }
}