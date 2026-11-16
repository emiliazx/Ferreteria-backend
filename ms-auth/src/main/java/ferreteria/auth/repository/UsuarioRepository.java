package ferreteria.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ferreteria.auth.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
