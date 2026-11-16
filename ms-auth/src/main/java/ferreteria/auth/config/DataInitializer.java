package ferreteria.auth.config;

import ferreteria.auth.model.Usuario;
import ferreteria.auth.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Asegura usuarios de demo con contraseñas conocidas al arrancar.
 * Contraseñas: admin123 / vendedor123 / user123
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            upsert(repo, encoder, "admin@ferreteria.cl", "admin123", "Administrador", "ADMIN");
            upsert(repo, encoder, "vendedor@ferreteria.cl", "vendedor123", "Vendedor Demo", "VENDEDOR");
            upsert(repo, encoder, "user@ferreteria.cl", "user123", "Usuario Demo", "USER");
        };
    }

    private void upsert(UsuarioRepository repo, PasswordEncoder encoder,
                        String email, String rawPassword, String nombre, String rol) {
        Usuario u = repo.findByEmail(email).orElseGet(Usuario::new);
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setNombre(nombre);
        u.setRol(rol);
        u.setActivo(true);
        repo.save(u);
    }
}
