
CREATE DATABASE IF NOT EXISTS canteen_firos;

USE canteen_firos;

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('Student', 'Admin') NOT NULL
);

CREATE TABLE IF NOT EXISTS menu_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(10, 2) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
);

-- Sample student and admin accounts
INSERT IGNORE INTO users (username, password, role)
VALUES
('student1', 'student123', 'Student'),
('admin', 'admin123', 'Admin');

-- Sample canteen menu
INSERT IGNORE INTO menu_items (item_name, price, stock)
VALUES
('Tea', 10.00, 50),
('Coffee', 15.00, 40),
('Veg Sandwich', 40.00, 20),
('Chicken Sandwich', 60.00, 15),
('Burger', 80.00, 25),
('Meals', 70.00, 30);

-- Check the created tables and sample data
SHOW TABLES;

SELECT * FROM users;
SELECT * FROM menu_items;
SELECT * FROM orders;
