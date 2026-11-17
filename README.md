# Microservicios adicionales – Ferretería

Estos 3 servicios complementan los 5 originales:

| Servicio | Puerto | Base de datos | Función |
|----------|--------|---------------|---------|
| cart-service | 8084 | ferreteria_cart | Carrito persistente por usuario |
| payment-service | 8085 | ferreteria_payment | Registro de pagos (simulado) |
| notification-service | 8086 | ferreteria_notification | Notificaciones / mensajes |

## Cómo integrarlos

1. Copia las carpetas `cart-service`, `payment-service` y `notification-service` dentro de tu proyecto `ferreteria-microservices`.
2. Agrega las bases en el script MySQL init:
   ```sql
   CREATE DATABASE IF NOT EXISTS ferreteria_cart ...;
   CREATE DATABASE IF NOT EXISTS ferreteria_payment ...;
   CREATE DATABASE IF NOT EXISTS ferreteria_notification ...;
   ```
3. Agrega las rutas en el API Gateway (`application.yml`):
   ```yaml
   - id: cart-service
     uri: lb://cart-service
     predicates:
       - Path=/api/carrito/**
   - id: payment-service
     uri: lb://payment-service
     predicates:
       - Path=/api/pagos/**
   - id: notification-service
     uri: lb://notification-service
     predicates:
       - Path=/api/notificaciones/**
   ```
4. Agrega los servicios al `docker-compose.yml` (mismo patrón que auth/product/order).

## Endpoints principales

### Carrito
- `GET /api/carrito/{userId}`
- `POST /api/carrito/items`
- `PUT /api/carrito/items/{id}`
- `DELETE /api/carrito/items/{id}`

### Pagos
- `POST /api/pagos` → registra un pago
- `GET /api/pagos/{pedidoId}`

### Notificaciones
- `POST /api/notificaciones` → crea notificación
- `GET /api/notificaciones/usuario/{userId}`

Todos siguen el mismo patrón: Entity → Repository → Service → Controller + Flyway + Eureka + Swagger.
