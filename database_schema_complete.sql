-- Additional tables for Mini Supermarket Database

USE mini_supermarket;

-- User Table
CREATE TABLE IF NOT EXISTS user (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(15),
    role_id INT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES role(role_id),
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Role Table
CREATE TABLE IF NOT EXISTS role (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role_name (role_name)
) ENGINE=InnoDB;

-- Role Permission Table
CREATE TABLE IF NOT EXISTS role_permission (
    role_permission_id INT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    permission_code VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES role(role_id),
    UNIQUE KEY unique_role_permission (role_id, permission_code),
    INDEX idx_role (role_id),
    INDEX idx_permission (permission_code)
) ENGINE=InnoDB;

-- Sales Invoice Table
CREATE TABLE IF NOT EXISTS sales_invoice (
    invoice_id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_code VARCHAR(20) UNIQUE NOT NULL,
    invoice_date TIMESTAMP NOT NULL,
    customer_id INT,
    user_id INT NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    payment_method VARCHAR(20),
    status VARCHAR(20) DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    INDEX idx_invoice_code (invoice_code),
    INDEX idx_invoice_date (invoice_date),
    INDEX idx_customer (customer_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Sales Invoice Detail Table
CREATE TABLE IF NOT EXISTS sales_invoice_detail (
    detail_id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES sales_invoice(invoice_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    INDEX idx_invoice (invoice_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB;

-- Inventory Lot Table
CREATE TABLE IF NOT EXISTS inventory_lot (
    lot_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    lot_code VARCHAR(20) UNIQUE NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    cost_price DECIMAL(15,2) NOT NULL,
    expiry_date DATE,
    manufactured_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    INDEX idx_product (product_id),
    INDEX idx_lot_code (lot_code),
    INDEX idx_expiry_date (expiry_date),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Goods Receipt Table
CREATE TABLE IF NOT EXISTS goods_receipt (
    receipt_id INT PRIMARY KEY AUTO_INCREMENT,
    receipt_code VARCHAR(20) UNIQUE NOT NULL,
    receipt_date TIMESTAMP NOT NULL,
    supplier_id INT,
    user_id INT NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    INDEX idx_receipt_code (receipt_code),
    INDEX idx_receipt_date (receipt_date),
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
    unit_cost DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    expiry_date DATE,
    manufactured_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (receipt_id) REFERENCES goods_receipt(receipt_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    INDEX idx_receipt (receipt_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB;

-- Stock Adjustment Table
CREATE TABLE IF NOT EXISTS stock_adjustment (
    adjustment_id INT PRIMARY KEY AUTO_INCREMENT,
    adjustment_code VARCHAR(20) UNIQUE NOT NULL,
    adjustment_date TIMESTAMP NOT NULL,
    user_id INT NOT NULL,
    reason TEXT,
    total_quantity INT NOT NULL DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    approved_by INT,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (approved_by) REFERENCES user(user_id),
    INDEX idx_adjustment_code (adjustment_code),
    INDEX idx_adjustment_date (adjustment_date),
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Stock Adjustment Detail Table
CREATE TABLE IF NOT EXISTS stock_adjustment_detail (
    detail_id INT PRIMARY KEY AUTO_INCREMENT,
    adjustment_id INT NOT NULL,
    product_id INT NOT NULL,
    current_stock INT NOT NULL,
    adjusted_quantity INT NOT NULL,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (adjustment_id) REFERENCES stock_adjustment(adjustment_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    INDEX idx_adjustment (adjustment_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB;

-- Payment Table
CREATE TABLE IF NOT EXISTS payment (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    payment_code VARCHAR(20) UNIQUE NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    invoice_id INT,
    amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    reference_number VARCHAR(50),
    status VARCHAR(20) DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES sales_invoice(invoice_id),
    INDEX idx_payment_code (payment_code),
    INDEX idx_payment_date (payment_date),
    INDEX idx_invoice (invoice_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Promotion Table
CREATE TABLE IF NOT EXISTS promotion (
    promotion_id INT PRIMARY KEY AUTO_INCREMENT,
    promotion_code VARCHAR(20) UNIQUE NOT NULL,
    promotion_name VARCHAR(100) NOT NULL,
    description TEXT,
    discount_type VARCHAR(20) NOT NULL, -- PERCENTAGE, FIXED_AMOUNT
    discount_value DECIMAL(15,2) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_promotion_code (promotion_code),
    INDEX idx_start_date (start_date),
    INDEX idx_end_date (end_date),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- Promotion Product Table
CREATE TABLE IF NOT EXISTS promotion_product (
    promotion_product_id INT PRIMARY KEY AUTO_INCREMENT,
    promotion_id INT NOT NULL,
    product_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (promotion_id) REFERENCES promotion(promotion_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
    UNIQUE KEY unique_promotion_product (promotion_id, product_id),
    INDEX idx_promotion (promotion_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB;

-- Loyalty Point Transaction Table
CREATE TABLE IF NOT EXISTS loyalty_point_txn (
    txn_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    invoice_id INT,
    points INT NOT NULL, -- Positive for earned, negative for redeemed
    txn_type VARCHAR(20) NOT NULL, -- EARNED, REDEEMED
    description TEXT,
    txn_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (invoice_id) REFERENCES sales_invoice(invoice_id),
    INDEX idx_customer (customer_id),
    INDEX idx_invoice (invoice_id),
    INDEX idx_txn_date (txn_date),
    INDEX idx_txn_type (txn_type)
) ENGINE=InnoDB;

-- Audit Log Table
CREATE TABLE IF NOT EXISTS audit_log (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    action VARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE, LOGIN, LOGOUT, etc.
    table_name VARCHAR(50) NOT NULL,
    record_id VARCHAR(50), -- ID of the affected record
    old_values TEXT, -- JSON string of old values
    new_values TEXT, -- JSON string of new values
    ip_address VARCHAR(45), -- IPv4 or IPv6
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_table_name (table_name),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB;

-- Sample data for users and roles
INSERT INTO role (role_name, description) VALUES
('ADMIN', 'Administrator with full access'),
('MANAGER', 'Manager with most permissions'),
('STAFF', 'Regular staff member'),
('CASHIER', 'Cashier for POS operations');

-- Sample permissions for roles
INSERT INTO role_permission (role_id, permission_code) VALUES
(1, 'DASHBOARD_VIEW'),
(1, 'PRODUCT_VIEW'),
(1, 'PRODUCT_CREATE'),
(1, 'PRODUCT_UPDATE'),
(1, 'PRODUCT_DELETE'),
(1, 'CUSTOMER_VIEW'),
(1, 'CUSTOMER_MANAGE'),
(1, 'SUPPLIER_VIEW'),
(1, 'SUPPLIER_MANAGE'),
(1, 'CATEGORY_VIEW'),
(1, 'CATEGORY_CREATE'),
(1, 'CATEGORY_UPDATE'),
(1, 'CATEGORY_DELETE'),
(1, 'POS_SELL'),
(1, 'RECEIPT_CREATE'),
(1, 'LOT_VIEW'),
(1, 'ADJUSTMENT_CREATE'),
(1, 'ADJUSTMENT_APPROVE'),
(1, 'PROMOTION_MANAGE'),
(1, 'PAYMENT_VIEW'),
(1, 'LOYALTY_VIEW'),
(1, 'REPORT_VIEW'),
(1, 'AUDIT_VIEW'),
(1, 'USER_MANAGE'),
(1, 'ROLE_PERMISSION_MANAGE'),
(1, 'INVOICE_VIEW');

-- Sample users
INSERT INTO user (username, password_hash, full_name, email, phone, role_id, status) VALUES
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Administrator', 'admin@minisupermarket.com', '0123456789', 1, 'ACTIVE'),
('manager', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Manager User', 'manager@minisupermarket.com', '0987654321', 2, 'ACTIVE'),
('staff', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Staff User', 'staff@minisupermarket.com', '0912345678', 3, 'ACTIVE'),
('cashier', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Cashier User', 'cashier@minisupermarket.com', '0934567890', 4, 'ACTIVE');