package ferreteria.auth.dto;

public class UsuarioResponse {

    private Long id;
    private String email;
    private String nombre;
    private String rol;
    private Boolean activo;

    public UsuarioResponse() {}

    public UsuarioResponse(Long id, String email, String nombre, String rol, Boolean activo) {
        this.id = id;
        this.email = email;
        this.nombre = nombre;
        this.rol = rol;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
