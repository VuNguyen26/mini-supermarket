-- Mini Supermarket Database Schema
-- Database: mini_supermarket

CREATE DATABASE IF NOT EXISTS mini_supermarket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mini_supermarket;

-- ===== MASTER DATA TABLES =====

-- Category Table
CREATE TABLE IF NOT EXISTS category (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_code VARCHAR(20) UNIQUE NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_code (category_code),
    INDEX idx_category_status (status)
) ENGINE=InnoDB;

-- Brand Table
CREATE TABLE IF NOT EXISTS brand (
    brand_id INT PRIMARY KEY AUTO_INCREMENT,
    brand_code VARCHAR(20) UNIQUE NOT NULL,
    brand_name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_brand_code (brand_code),
    INDEX idx_brand_status (status)
) ENGINE=InnoDB;

-- Product Table
CREATE TABLE IF NOT EXISTS product (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_code VARCHAR(20) UNIQUE NOT NULL,
    barcode VARCHAR(13) UNIQUE NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    category_id INT,
    brand_id INT,
    unit VARCHAR(20) NOT NULL,
    cost_price DECIMAL(15,2) NOT NULL,
    selling_price DECIMAL(15,2) NOT NULL,
    current_stock INT DEFAULT 0,
    min_stock INT DEFAULT 0,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(category_id),
    FOREIGN KEY (brand_id) REFERENCES brand(brand_id),
    INDEX idx_product_code (product_code),
    INDEX idx_barcode (barcode),
    INDEX idx_category (category_id),
    INDEX idx_brand (brand_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Product Image Table
CREATE TABLE IF NOT EXISTS product_image (
    image_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
    INDEX idx_product (product_id)
) ENGINE=InnoDB;

-- Supplier Table
CREATE TABLE IF NOT EXISTS supplier (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_code VARCHAR(20) UNIQUE NOT NULL,
    supplier_name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    tax_code VARCHAR(13),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_supplier_code (supplier_code),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Customer Table
CREATE TABLE IF NOT EXISTS customer (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_code VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100),
    address TEXT,
    birth_date DATE,
    gender VARCHAR(10),
    loyalty_points INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_code (customer_code),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- ===== SAMPLE DATA =====

-- Sample Categories
INSERT INTO category (category_code, category_name, description, status) VALUES
('CAT001', 'Đồ uống', 'Nước ngọt, nước giải khát', 'ACTIVE'),
('CAT002', 'Bánh kẹo', 'Bánh snack, kẹo các loại', 'ACTIVE'),
('CAT003', 'Sữa', 'Sữa tươi, sữa hộp', 'ACTIVE'),
('CAT004', 'Gia vị', 'Gia vị nấu ăn', 'ACTIVE'),
('CAT005', 'Mỳ gói', 'Mỳ ăn liền các loại', 'ACTIVE');

-- Sample Brands
INSERT INTO brand (brand_code, brand_name, description, status) VALUES
('BRD001', 'Coca Cola', 'Thương hiệu nước giải khát', 'ACTIVE'),
('BRD002', 'Pepsi', 'Thương hiệu nước giải khát', 'ACTIVE'),
('BRD003', 'Vinamilk', 'Thương hiệu sữa Việt Nam', 'ACTIVE'),
('BRD004', 'TH True Milk', 'Thương hiệu sữa Việt Nam', 'ACTIVE'),
('BRD005', 'Hảo Hảo', 'Thương hiệu mỳ gói', 'ACTIVE');

-- Sample Products
INSERT INTO product (product_code, barcode, product_name, category_id, brand_id, unit, cost_price, selling_price, current_stock, min_stock, description, status) VALUES
('PRD001', '8934563123456', 'Coca Cola 330ml', 1, 1, 'Lon', 7000.00, 10000.00, 100, 20, 'Nước ngọt Coca Cola lon 330ml', 'ACTIVE'),
('PRD002', '8934563123457', 'Pepsi 330ml', 1, 2, 'Lon', 7000.00, 10000.00, 150, 20, 'Nước ngọt Pepsi lon 330ml', 'ACTIVE'),
('PRD003', '8934563123458', 'Sữa tươi Vinamilk 1L', 3, 3, 'Hộp', 25000.00, 32000.00, 50, 10, 'Sữa tươi Vinamilk hộp 1 lít', 'ACTIVE'),
('PRD004', '8934563123459', 'Sữa TH True Milk 1L', 3, 4, 'Hộp', 28000.00, 35000.00, 40, 10, 'Sữa tươi TH True Milk hộp 1 lít', 'ACTIVE'),
('PRD005', '8934563123460', 'Mỳ Hảo Hảo tôm chua cay', 5, 5, 'Gói', 2500.00, 3500.00, 200, 50, 'Mỳ ăn liền Hảo Hảo vị tôm chua cay', 'ACTIVE');

-- Sample Suppliers
INSERT INTO supplier (supplier_code, supplier_name, contact_person, phone, email, address, tax_code, status) VALUES
('SUP001', 'Công ty TNHH Coca Cola VN', 'Nguyễn Văn A', '0901234567', 'contact@cocacola.vn', '123 Đường ABC, Quận 1, TP.HCM', '0123456789', 'ACTIVE'),
('SUP002', 'Công ty Vinamilk', 'Trần Thị B', '0909876543', 'contact@vinamilk.vn', '456 Đường DEF, Quận 2, TP.HCM', '9876543210', 'ACTIVE'),
('SUP003', 'Công ty Acecook VN', 'Lê Văn C', '0912345678', 'contact@acecook.vn', '789 Đường GHI, Quận 3, TP.HCM', '1234567890', 'ACTIVE');

-- Sample Customers
INSERT INTO customer (customer_code, full_name, phone, email, address, birth_date, gender, loyalty_points, status) VALUES
('CUS001', 'Nguyễn Văn An', '0987654321', 'nguyenvanan@gmail.com', '12 Nguyễn Huệ, Q1, TP.HCM', '1990-05-15', 'Nam', 100, 'ACTIVE'),
('CUS002', 'Trần Thị Bình', '0976543210', 'tranthib@gmail.com', '34 Lê Lợi, Q1, TP.HCM', '1985-08-20', 'Nữ', 250, 'ACTIVE'),
('CUS003', 'Phạm Minh Cường', '0965432109', 'phamcuong@gmail.com', '56 Hai Bà Trưng, Q3, TP.HCM', '1992-11-10', 'Nam', 50, 'ACTIVE');
