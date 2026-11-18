CREATE DATABASE IF NOT EXISTS supermercado_mcp
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE supermercado_mcp;

CREATE TABLE IF NOT EXISTS producto (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255) NOT NULL,
  categoria VARCHAR(255),
  precio DOUBLE,
  stock INT,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pedido (
  id BIGINT NOT NULL AUTO_INCREMENT,
  cliente VARCHAR(255) NOT NULL,
  direccion VARCHAR(255) NOT NULL,
  estado VARCHAR(255),
  total DOUBLE,
  fecha VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pedido_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  pedido_id BIGINT,
  producto_id BIGINT,
  cantidad INT,
  subtotal DOUBLE,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO producto (nombre, categoria, precio, stock) VALUES
('Leche entera 1L','Lácteos',4200,50),
('Queso campesino 500g','Lácteos',12500,20),
('Yogurt natural 1L','Lácteos',6800,30),
('Manzana roja kg','Frutas',7500,40),
('Banano kg','Frutas',3200,60),
('Arroz 1kg','Granos',4800,80),
('Pan tajado','Panadería',5900,25),
('Huevos x30','Huevos',16500,15),
('Detergente 1kg','Aseo',14900,35),
('Jabón de baño x3','Aseo',8900,45);