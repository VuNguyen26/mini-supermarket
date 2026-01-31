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

-- ===== AUTHENTICATION TABLES =====

-- Role Table
CREATE TABLE IF NOT EXISTS role (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role_name (role_name),
    INDEX idx_role_status (status)
) ENGINE=InnoDB;

-- Permission Table
CREATE TABLE IF NOT EXISTS permission (
    perm_id INT PRIMARY KEY AUTO_INCREMENT,
    perm_code VARCHAR(50) UNIQUE NOT NULL,
    perm_name VARCHAR(100) NOT NULL,
    INDEX idx_perm_code (perm_code)
) ENGINE=InnoDB;

-- Role Permission Table (Many-to-Many)
CREATE TABLE IF NOT EXISTS role_permission (
    role_id INT NOT NULL,
    perm_id INT NOT NULL,
    PRIMARY KEY (role_id, perm_id),
    FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE CASCADE,
    FOREIGN KEY (perm_id) REFERENCES permission(perm_id) ON DELETE CASCADE,
    INDEX idx_role_perm (role_id, perm_id)
) ENGINE=InnoDB;

-- User Table
CREATE TABLE IF NOT EXISTS user (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    role_id INT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES role(role_id),
    INDEX idx_username (username),
    INDEX idx_role (role_id),
    INDEX idx_user_status (status)
) ENGINE=InnoDB;

-- ===== AUDIT LOG TABLE =====

-- Audit Log Table
CREATE TABLE IF NOT EXISTS audit_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,  -- CREATE / UPDATE / DELETE / LOGIN / LOGOUT
    entity_name VARCHAR(100),     -- e.g., 'Product', 'User'
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- ===== SALES TABLES =====

-- Sales Invoice Table
CREATE TABLE IF NOT EXISTS sales_invoice (
    invoice_id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_code VARCHAR(20) UNIQUE NOT NULL,
    customer_id INT,
    user_id INT NOT NULL,  -- User who created the invoice
    total_amount DECIMAL(15,2) NOT NULL,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    final_amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(50),  -- CASH / CARD / TRANSFER
    status VARCHAR(20) DEFAULT 'COMPLETED',  -- COMPLETED / CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    INDEX idx_invoice_code (invoice_code),
    INDEX idx_customer (customer_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- Sales Invoice Detail Table
CREATE TABLE IF NOT EXISTS sales_invoice_detail (
    detail_id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    discount DECIMAL(15,2) DEFAULT 0,
    total_price DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (invoice_id) REFERENCES sales_invoice(invoice_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    INDEX idx_invoice (invoice_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB;

-- ===== ADDITIONAL SAMPLE DATA =====

-- Sample Roles
INSERT INTO role (role_name, description, status) VALUES
('ADMIN', 'Administrator with full access', 'ACTIVE'),
('CASHIER', 'Cashier for sales operations', 'ACTIVE'),
('WAREHOUSE', 'Warehouse manager', 'ACTIVE');

-- Sample Permissions (based on PermissionCodes.java)
INSERT INTO permission (perm_code, perm_name) VALUES
('USER_MANAGE', 'Manage users'),
('ROLE_MANAGE', 'Manage roles'),
('PRODUCT_MANAGE', 'Manage products'),
('CATEGORY_MANAGE', 'Manage categories'),
('BRAND_MANAGE', 'Manage brands'),
('SUPPLIER_MANAGE', 'Manage suppliers'),
('CUSTOMER_MANAGE', 'Manage customers'),
('SALES_INVOICE_MANAGE', 'Manage sales invoices'),
('GOODS_RECEIPT_MANAGE', 'Manage goods receipts'),
('INVENTORY_MANAGE', 'Manage inventory'),
('STOCK_ADJUSTMENT_MANAGE', 'Manage stock adjustments'),
('REPORT_VIEW', 'View reports'),
('AUDIT_LOG_VIEW', 'View audit logs');

-- Sample Role Permissions (ADMIN has all, CASHIER has sales/report)
INSERT INTO role_permission (role_id, perm_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13),  -- ADMIN
(2, 7), (2, 8), (2, 12),  -- CASHIER
(3, 3), (3, 4), (3, 5), (3, 6), (3, 9), (3, 10), (3, 11);  -- WAREHOUSE

-- Sample Users (password: '123' hashed with BCrypt - replace with actual hash)
INSERT INTO user (username, password_hash, full_name, phone, role_id, status) VALUES
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Administrator', '0901234567', 1, 'ACTIVE'),
('cashier1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Nguyễn Văn Thu Ngân', '0909876543', 2, 'ACTIVE'),
('warehouse1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Trần Thị Kho', '0912345678', 3, 'ACTIVE');

-- Sample Audit Logs
INSERT INTO audit_log (username, action, entity_name, description, created_at) VALUES
('admin', 'LOGIN', 'User', 'Admin logged in', NOW()),
('cashier1', 'CREATE', 'SalesInvoice', 'Created invoice INV001', NOW() - INTERVAL 1 DAY);

-- Sample Sales Invoices
INSERT INTO sales_invoice (invoice_code, customer_id, user_id, total_amount, discount_amount, final_amount, payment_method, status) VALUES
('INV001', 1, 2, 50000.00, 5000.00, 45000.00, 'CASH', 'COMPLETED'),
('INV002', 2, 2, 30000.00, 0.00, 30000.00, 'CARD', 'COMPLETED');

-- Sample Sales Invoice Details
INSERT INTO sales_invoice_detail (invoice_id, product_id, quantity, unit_price, discount, total_price) VALUES
(1, 1, 2, 10000.00, 0.00, 20000.00),
(1, 2, 1, 10000.00, 0.00, 10000.00),
(1, 3, 1, 25000.00, 5000.00, 20000.00),
(2, 1, 1, 10000.00, 0.00, 10000.00),
(2, 4, 1, 20000.00, 0.00, 20000.00);

-- ===== ADDITIONAL TABLES =====

-- Goods Receipt Table
CREATE TABLE IF NOT EXISTS goods_receipt (
    receipt_id INT PRIMARY KEY AUTO_INCREMENT,
    receipt_code VARCHAR(20) UNIQUE NOT NULL,
    supplier_id INT NOT NULL,
    user_id INT NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    INDEX idx_receipt_code (receipt_code),
    INDEX idx_supplier (supplier_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Goods Receipt Detail Table
CREATE TABLE IF NOT EXISTS goods_receipt_detail (
    detail_id INT PRIMARY KEY AUTO_INCREMENT,
    receipt_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (receipt_id) REFERENCES goods_receipt(receipt_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    INDEX idx_receipt (receipt_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB;

-- Inventory Lot Table
CREATE TABLE IF NOT EXISTS inventory_lot (
    lot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    remaining_quantity INT NOT NULL,
    import_date DATE NOT NULL,
    expiry_date DATE,
    import_price DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    INDEX idx_product (product_id),
    INDEX idx_import_date (import_date),
    INDEX idx_expiry_date (expiry_date)
) ENGINE=InnoDB;

-- Payment Table
CREATE TABLE IF NOT EXISTS payment (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    FOREIGN KEY (invoice_id) REFERENCES sales_invoice(invoice_id) ON DELETE CASCADE,
    INDEX idx_invoice (invoice_id),
    INDEX idx_payment_date (payment_date)
) ENGINE=InnoDB;

-- Loyalty Point Transaction Table
CREATE TABLE IF NOT EXISTS loyalty_point_txn (
    txn_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    invoice_id INT,
    points INT NOT NULL,
    txn_type VARCHAR(20) NOT NULL,  -- EARN / REDEEM
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (invoice_id) REFERENCES sales_invoice(invoice_id),
    INDEX idx_customer (customer_id),
    INDEX idx_invoice (invoice_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- Stock Adjustment Table
CREATE TABLE IF NOT EXISTS stock_adjustment (
    adjustment_id INT PRIMARY KEY AUTO_INCREMENT,
    adjustment_code VARCHAR(20) UNIQUE NOT NULL,
    user_id INT NOT NULL,
    reason TEXT,
    total_quantity INT NOT NULL,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    INDEX idx_adjustment_code (adjustment_code),
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Stock Adjustment Detail Table
CREATE TABLE IF NOT EXISTS stock_adjustment_detail (
    detail_id INT PRIMARY KEY AUTO_INCREMENT,
    adjustment_id INT NOT NULL,
    product_id INT NOT NULL,
    old_quantity INT NOT NULL,
    new_quantity INT NOT NULL,
    adjustment_quantity INT NOT NULL,
    FOREIGN KEY (adjustment_id) REFERENCES stock_adjustment(adjustment_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    INDEX idx_adjustment (adjustment_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB;