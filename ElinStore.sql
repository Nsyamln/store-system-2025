CREATE DATABASE ElinStore;
USE ElinStore;

CREATE TABLE users (
    user_id CHAR(5) PRIMARY KEY,
    name VARCHAR (255),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    address TEXT NOT NULL,
    phone VARCHAR(15) NOT NULL,   
    created_by CHAR(5) NOT NULL,
    updated_by CHAR(5),
    deleted_by CHAR(5),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE products (
    product_id CHAR(5) PRIMARY KEY,
    product_name VARCHAR(255),
    description TEXT,
    price DECIMAL(10, 2),
    stock INT,
    product_image VARCHAR(255),

    supplier_id CHAR(5),
    purchase_price DECIMAL(10, 2),
    created_by CHAR(5) NOT NULL,
    updated_by CHAR(5),
    deleted_by CHAR(5),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE sales (
    sale_id CHAR(5) PRIMARY KEY,
    sale_date DATE,
    total_price DECIMAL(10, 2),
    customer_id CHAR(5)
);

CREATE TABLE sale_details (
    detail_id CHAR(5) PRIMARY KEY,
    sale_id CHAR(5),
    product_id CHAR(5),
    quantity INT,
    price DECIMAL(10, 2)
);

CREATE TABLE profit_sharing (
    profit_sharing_id CHAR(5) PRIMARY KEY,
    sale_id CHAR(5),
    product_id CHAR(5),
    supplier_id CHAR(5),
    product_quantity INT,
    total_purchase_price DECIMAL(10, 2),
    total_sale_price DECIMAL(10, 2),
    status VARCHAR (32),
    payment_date DATE
);



ALTER TABLE products ADD CONSTRAINT `fk_prod1` FOREIGN KEY (`supplier_id`) REFERENCES `users` (`user_id`);
ALTER TABLE sales ADD CONSTRAINT `fk_sale1` FOREIGN KEY (customer_id) REFERENCES users(user_id);
ALTER TABLE sale_details ADD CONSTRAINT `fk_detail1` FOREIGN KEY (sale_id) REFERENCES sales(sale_id);
ALTER TABLE sale_details ADD CONSTRAINT `fk_detail2` FOREIGN KEY (product_id) REFERENCES products(product_id);
ALTER TABLE profit_sharing ADD CONSTRAINT `fk_pros1` FOREIGN KEY (sale_id) REFERENCES sales(sale_id);
ALTER TABLE profit_sharing ADD CONSTRAINT `fk_pros2` FOREIGN KEY (product_id) REFERENCES products(product_id);
ALTER TABLE profit_sharing ADD CONSTRAINT `fk_pros3` FOREIGN KEY (supplier_id) REFERENCES users(user_id);

##------------------------------------------------------------


-- Membuat trigger untuk mengenerate ID Produk secara otomatis
DELIMITER //

CREATE TRIGGER generate_product_id
BEFORE INSERT ON products
FOR EACH ROW
BEGIN
    DECLARE last_id INT;
    DECLARE new_id VARCHAR(10);

    -- Mendapatkan nilai terbesar dari ID yang ada
    SELECT COALESCE(MAX(CAST(SUBSTRING(product_id, 3) AS UNSIGNED)), 0) INTO last_id FROM products;

    -- Menambahkan 1 pada nilai terbesar untuk ID baru
    SET new_id = CONCAT('PR', LPAD(last_id + 1, 3, '0'));

    -- Mengatur ID baru pada baris yang akan dimasukkan
    SET NEW.product_id = new_id;
END //

DELIMITER ;


-- Membuat trigger untuk mengenerate ID Penjualan secara otomatis
DELIMITER //

CREATE TRIGGER generate_sale_id
BEFORE INSERT ON sales
FOR EACH ROW
BEGIN
    DECLARE last_id INT;
    DECLARE new_id VARCHAR(10);

    -- Mendapatkan nilai terbesar dari ID yang ada
    SELECT COALESCE(MAX(CAST(SUBSTRING(sale_id, 3) AS UNSIGNED)), 0) INTO last_id FROM sales;

    -- Menambahkan 1 pada nilai terbesar untuk ID baru
    SET new_id = CONCAT('JL', LPAD(last_id + 1, 3, '0'));

    -- Mengatur ID baru pada baris yang akan dimasukkan
    SET NEW.sale_id = new_id;
END //

DELIMITER ;


-- Membuat trigger untuk mengenerate ID USER secara otomatis
DELIMITER //

CREATE TRIGGER generate_user_id
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    DECLARE last_id INT;
    DECLARE new_id VARCHAR(10);

    -- Mendapatkan nilai terbesar dari ID yang ada
    SELECT COALESCE(MAX(CAST(SUBSTRING(user_id, 3) AS UNSIGNED)), 0) INTO last_id FROM users;

    -- Menambahkan 1 pada nilai terbesar untuk ID baru
    SET new_id = CONCAT('US', LPAD(last_id + 1, 3, '0'));

    -- Mengatur ID baru pada baris yang akan dimasukkan
    SET NEW.user_id = new_id;
END //

DELIMITER ;

-- Membuat trigger untuk mengenerate ID Detail Penjualan secara otomatis
DELIMITER //

CREATE TRIGGER generate_detail_id
BEFORE INSERT ON sale_details 
FOR EACH ROW
BEGIN
    DECLARE last_id INT;
    DECLARE new_id VARCHAR(10);

    -- Mendapatkan nilai terbesar dari ID yang ada
    SELECT COALESCE(MAX(CAST(SUBSTRING(detail_id, 3) AS UNSIGNED)), 0) INTO last_id FROM sale_details;

    -- Menambahkan 1 pada nilai terbesar untuk ID baru
    SET new_id = CONCAT('DS', LPAD(last_id + 1, 3, '0'));

    -- Mengatur ID baru pada baris yang akan dimasukkan
    SET NEW.detail_id = new_id;
END //
DELIMITER ;

-- Membuat trigger untuk mengenerate ID Pembagian Hasil secara otomatis
DELIMITER //

CREATE TRIGGER generate_sharing_id
BEFORE INSERT ON profit_sharing 
FOR EACH ROW
BEGIN
    DECLARE last_id INT;
    DECLARE new_id VARCHAR(10);

    -- Mendapatkan nilai terbesar dari ID yang ada
    SELECT COALESCE(MAX(CAST(SUBSTRING(profit_sharing_id , 3) AS UNSIGNED)), 0) INTO last_id FROM profit_sharing;

    -- Menambahkan 1 pada nilai terbesar untuk ID baru
    SET new_id = CONCAT('PS', LPAD(last_id + 1, 3, '0'));

    -- Mengatur ID baru pada baris yang akan dimasukkan
    SET NEW.profit_sharing_id  = new_id;
END //

DELIMITER ;

-- Membuat trigger untuk mendapatkan id penjualan terakhir
DELIMITER //

CREATE TRIGGER get_last_sale_id
AFTER INSERT ON sales 
FOR EACH ROW 
BEGIN 
    DECLARE last_id CHAR(5);
    SET last_id = LAST_INSERT_ID();
END //
DELIMITER ;

-- Membuat trigger untuk update total harga di tabel penjualan 
DELIMITER //

CREATE TRIGGER update_total_price
AFTER INSERT ON sale_details
FOR EACH ROW
BEGIN
    DECLARE new_id CHAR(5);
    SET new_id = NEW.sale_id;
    UPDATE sales SET total_price = (SELECT SUM(quantity*price) FROM sale_details WHERE sale_id = new_id)
    WHERE sale_id = new_id;
END //
DELIMITER ;

-- Membuat trigger untuk insert revenue ke tabel profit_sharing
DELIMITER //

CREATE TRIGGER insert_revenue
AFTER INSERT ON sale_details 
FOR EACH ROW 
BEGIN 
    DECLARE new_sale_id CHAR(5);
    DECLARE new_product_id CHAR(5);
    DECLARE new_quantity INT;
    DECLARE supplier_id_val CHAR(5);

    SET new_sale_id = NEW.sale_id;
    SET new_product_id = NEW.product_id;
    SET new_quantity = NEW.quantity;

    INSERT INTO profit_sharing (sale_id, product_id, supplier_id, product_quantity, total_purchase_price, total_sale_price)
    SELECT
        new_sale_id,
        new_product_id,
        pr.supplier_id,
        new_quantity,
        (new_quantity * pr.purchase_price) AS total_purchase_price,
        (new_quantity * NEW.price) AS total_sale_price
       -- CURDATE() -- Gunakan tanggal hari ini sebagai tanggal pembayaran
    FROM
        products pr
    WHERE
        pr.product_id = new_product_id
    LIMIT 1; -- Ensure only one row is selected in case of multiple consignment records for the same product
END //

DELIMITER ;



-- Membuat trigger untuk pengurangan stok di tabel produk
DELIMITER //

CREATE TRIGGER minus_stock 
AFTER INSERT ON sale_details 
FOR EACH ROW
BEGIN 
    UPDATE products 
    SET stock = stock - NEW.quantity 
    WHERE product_id = NEW.product_id;
END //
DELIMITER ;


#---------------------------------------------------------------------
USE elinstore
-- Insert dummy users
INSERT INTO users (user_id, name, email, password, role, address, phone, created_by, created_at)
VALUES 
('US001', 'Nasya', 'nasya@example.com', '$2a$10$uIBEJjpFQcMlzSM40cBOBOKrmd6QdcHiPiwtBjm/WjMkrsHgarVTO', 'admin', 'Jl. Raya No. 1, Ciamis', '081234567890', 'US001', NOW());
('US002', 'Asep', 'asep@example.com', 'password123', 'supplier', 'Jl. Kebon No. 2, Ciamis', '081234567891', 'US001', NOW()),
('US003', 'Budi', 'budi@example.com', 'password123', 'customer', 'Jl. Pasar No. 3, Ciamis', '081234567892', 'US001', NOW());

INSERT INTO products (product_id, product_name, description, price, stock, supplier_id, product_image)
VALUES 
('PR001', 'Galendo', 'Traditional coconut sweet from Ciamis', 20000.00, 50, 'US002', 'galendo.jpg'),
('PR002', 'Sale Pisang', 'Dried banana snack', 15000.00, 40, 'US002', 'salepisang.jpg'),
('PR003', 'Keripik Tempe', 'Crispy tempeh chips', 18000.00, 30, 'US002', 'keripiktempe.jpg');

INSERT INTO consignment_products (consignment_id, supplier_id, product_id, consignment_date, consignment_quantity, purchase_price)
VALUES 
('CP001', 'US002', 'PR001', '2024-07-15', 50, 18000.00),
('CP002', 'US002', 'PR002', '2024-07-15', 60, 13000.00),
('CP003', 'US002', 'PR003', '2024-07-15', 70, 16000.00);

INSERT INTO sales (sale_id, sale_date, total_price, customer_id)
VALUES 
('JL001', '2024-07-16', 0.00, 'US003'),
('JL002', '2024-07-16', 0.00, 'US003');


INSERT INTO sale_details (detail_id, sale_id, product_id, quantity, price)
VALUES 
('DS001', 'JL001', 'PR001', 2, 20000.00),
('DS002', 'JL001', 'PR002', 3, 15000.00),
('DS003', 'JL002', 'PR002', 3, 15000.00);

SELECT
    s.sale_id,
    s.sale_date,
    s.total_price,
    s.customer_id,
    sd.detail_id,
    sd.product_id,
    sd.quantity,
    sd.price
FROM
    sales s
        JOIN
    sale_details sd ON s.sale_id = sd.sale_id WHERE s.sale_date BETWEEN + req.startedAt+"AND"+req.endedAt  ;
