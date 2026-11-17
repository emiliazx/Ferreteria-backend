CREATE TABLE notificaciones (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT         NULL,
    email       VARCHAR(150),
    tipo        VARCHAR(50)    NOT NULL,
    titulo      VARCHAR(200)   NOT NULL,
    mensaje     TEXT           NOT NULL,
    leida       BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
