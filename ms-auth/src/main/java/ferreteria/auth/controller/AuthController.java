package ferreteria.auth.controller;

import ferreteria.auth.dto.*;
import ferreteria.auth.model.Usuario;
import ferreteria.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registro, login, recuperación y perfil")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nuevo usuario (rol USER)")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña para cualquier rol (ADMIN, VENDEDOR, USER)")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Map.of("message", "Contraseña actualizada correctamente");
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener datos del usuario autenticado")
    public UsuarioResponse me(@AuthenticationPrincipal Usuario usuario) {
        return authService.me(usuario.getEmail());
    }
}