# Ferretería Maestros – Microservicios

Backend desacoplado con **Spring Boot 3 + Eureka + API Gateway + JWT + Flyway + MySQL (Laragon) + Swagger + pruebas unitarias**.

## Arquitectura

```
React (Vite)  →  API Gateway :8080  →  Eureka :8761
                      │
          ┌───────────┼───────────┐
          ▼           ▼           ▼
   auth-service  product-service  order-service
      :8081          :8082           :8083
   ferreteria_auth  ferreteria_product  ferreteria_order
```

## Requisitos

- **Docker Desktop** (recomendado) **o** JDK 17 + Maven 3.9 + Laragon
- Node.js (solo para el frontend React)

## Arranque con Docker Compose (recomendado)

```bash
docker compose up --build
```

Detalle completo en **DOCKER.md**.

| Servicio   | URL |
|------------|-----|
| Gateway    | http://localhost:8080 |
| Eureka     | http://localhost:8761 |
| MySQL      | localhost:3307 (root/root) |

## Arranque manual (Laragon + Maven)

Crear las BD en MySQL y luego:

```bash
cd eureka-server   && mvn spring-boot:run
cd auth-service    && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd order-service   && mvn spring-boot:run
cd api-gateway     && mvn spring-boot:run
```

## Endpoints (siempre por el Gateway :8080)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/auth/register | Registro |
| POST | /api/auth/login | Login → JWT |
| GET | /api/auth/me | Usuario actual |
| GET | /api/productos | Listar productos |
| GET | /api/productos/{id} | Detalle |
| GET | /api/categorias | Categorías |
| GET | /api/categorias/{slug}/productos | Por categoría |
| POST | /api/productos | Crear (ADMIN) |
| PUT | /api/productos/{id} | Actualizar (ADMIN) |
| DELETE | /api/productos/{id} | Eliminar (ADMIN) |
| POST | /api/pedidos | Crear pedido |
| GET | /api/pedidos/mios | Mis pedidos |
| GET | /api/pedidos/{id} | Detalle pedido |
| PATCH | /api/pedidos/{id}/estado | Cambiar estado (VENDEDOR/ADMIN) |

## Usuarios de prueba (después de migraciones)

Se insertan por Flyway en auth-service:

- admin@ferreteria.cl / admin123  (ADMIN)
- vendedor@ferreteria.cl / vendedor123  (VENDEDOR)
- user@ferreteria.cl / user123  (USER)

## Frontend React

Configura:

```
VITE_API_URL=http://localhost:8080
```

Reemplaza el array hardcodeado de productos por llamadas a `/api/productos` y `/api/categorias`.
Envía el header `Authorization: Bearer <token>` en las rutas protegidas.

## Pruebas

```bash
cd auth-service && mvn test
cd product-service && mvn test
cd order-service && mvn test
```
