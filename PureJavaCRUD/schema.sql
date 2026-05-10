CREATE DATABASE IF NOT EXISTS device_inventory;
USE device_inventory;

CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) UNIQUE NOT NULL,
    password    VARCHAR(64) NOT NULL,
    role        ENUM('admin','user') DEFAULT 'user',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by  INT DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS devices (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    device_name   VARCHAR(100) NOT NULL,
    device_type   VARCHAR(50),
    brand         VARCHAR(50),
    model         VARCHAR(100),
    serial_number VARCHAR(100) UNIQUE,
    status        ENUM('Active','Inactive','Under Repair','Retired') DEFAULT 'Active',
    location      VARCHAR(100),
    assigned_to   VARCHAR(100),
    purchase_date VARCHAR(20),
    notes         TEXT,
    created_by    INT,
    updated_by    INT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_log (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT,
    username   VARCHAR(50),
    action     VARCHAR(100),
    details    TEXT,
    logged_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Default admin account: username=admin | password=Admin@123
INSERT IGNORE INTO users (username, password, role)
VALUES ('admin', 'c3b3fc4e3f97a40f9d7dd07941bcce19cf32d0e0c5c14e8c5d6c60e1e97ba3a3', 'admin');
