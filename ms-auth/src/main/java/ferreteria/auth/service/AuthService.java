package ferreteria.auth.service;

import ferreteria.auth.dto.*;
import ferreteria.auth.model.Usuario;
import ferreteria.auth.repository.UsuarioRepository;
import ferreteria.auth.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail().toLowerCase().trim());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNombre(request.getNombre().trim());
        usuario.setRol("USER");
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario.getId(), usuario.getEmail(), usuario.getRol());
        return new AuthResponse(token, usuario.getId(), usuario.getEmail(), usuario.getNombre(), usuario.getRol());
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario desactivado");
        }
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwtService.generateToken(usuario.getId(), usuario.getEmail(), usuario.getRol());
        return new AuthResponse(token, usuario.getId(), usuario.getEmail(), usuario.getNombre(), usuario.getRol());
    }

    public UsuarioResponse me(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return new UsuarioResponse(usuario.getId(), usuario.getEmail(), usuario.getNombre(),
                usuario.getRol(), usuario.getActivo());
    }
   public void resetPassword(ResetPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe una cuenta registrada con ese correo"));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "La cuenta se encuentra inactiva");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);
    }
}
