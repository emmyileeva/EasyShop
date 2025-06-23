-- Eerst alle bestaande tabellen verwijderen (in de juiste volgorde vanwege foreign keys)
DROP TABLE IF EXISTS shopping_cart;
DROP TABLE IF EXISTS order_line_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS profiles;
DROP TABLE IF EXISTS users;
GO

-- Tabellen opnieuw aanmaken met T-SQL syntax
CREATE TABLE users (
    user_id INT IDENTITY(1,1) NOT NULL,
    username NVARCHAR(50) NOT NULL,
    hashed_password NVARCHAR(255) NOT NULL,
    role NVARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id)
);

CREATE TABLE profiles (
    user_id INT NOT NULL,
    first_name NVARCHAR(50) NOT NULL,
    last_name NVARCHAR(50) NOT NULL,
    phone NVARCHAR(20) NOT NULL,
    email NVARCHAR(200) NOT NULL,
    address NVARCHAR(200) NOT NULL,
    city NVARCHAR(50) NOT NULL,
    state NVARCHAR(50) NOT NULL,
    zip NVARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE categories (
    category_id INT IDENTITY(1,1) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(MAX),
    PRIMARY KEY (category_id)
);

CREATE TABLE products (
    product_id INT IDENTITY(1,1) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category_id INT NOT NULL,
    description NVARCHAR(MAX),
    color NVARCHAR(20),
    image_url NVARCHAR(200),
    stock INT NOT NULL DEFAULT 0,
    featured BIT NOT NULL DEFAULT 0,
    PRIMARY KEY (product_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- (De andere tabellen 'orders' en 'order_line_items' kunnen op dezelfde manier worden geconverteerd als je ze nodig hebt)

CREATE TABLE shopping_cart (
   user_id INT NOT NULL,
   product_id INT NOT NULL,
   quantity INT NOT NULL DEFAULT 1,
   PRIMARY KEY (user_id, product_id),
   FOREIGN KEY (user_id) REFERENCES users(user_id),
   FOREIGN KEY (product_id) REFERENCES products(product_id)
);
GO

/*  INSERT Data  */
INSERT INTO users (username, hashed_password, role)
VALUES  ('user','$2a$10$NkufUPF3V8dEPSZeo1fzHe9ScBu.LOay9S3N32M84yuUM2OJYEJ/.','ROLE_USER'),
        ('admin','$2a$10$lfQi9jSfhZZhfS6/Kyzv3u3418IgnWXWDQDk7IbcwlCFPgxg9Iud2','ROLE_ADMIN');

INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip)
VALUES  (1, 'Joe', 'Joesephus', '800-555-1234', 'joejoesephus@email.com', '789 Oak Avenue', 'Dallas', 'TX', '75051'),
        (2, 'Adam', 'Admamson', '800-555-1212', 'aaadamson@email.com', '456 Elm Street','Dallas','TX','75052');

INSERT INTO categories (name, description)
VALUES  ('Electronics', 'Explore the latest gadgets and electronic devices.'),
        ('Fashion', 'Discover trendy clothing and accessories for men and women.'),
        ('Home & Kitchen', 'Find everything you need to decorate and equip your home.');

INSERT INTO products (name, price, category_id, description, image_url, stock, featured, color)
VALUES  ('Smartphone', 499.99, 1, 'A powerful and feature-rich smartphone for all your communication needs.', 'smartphone.jpg', 50, 0, 'Black'),
        ('Laptop', 899.99, 1, 'A high-performance laptop for work and entertainment.', 'laptop.jpg', 30, 0, 'Gray'),
        ('Headphones', 99.99, 1, 'Immerse yourself in music with these high-quality headphones.', 'headphones.jpg', 100, 1, 'White'),
        ('Men''s T-Shirt', 29.99, 2, 'A comfortable and stylish t-shirt for everyday wear.', 'mens-tshirt.jpg', 50, 1, 'Charcoal'),
        ('Women''s Dress', 79.99, 2, 'A beautiful and elegant dress for special occasions.', 'womens-dress.jpg', 50, 0, 'Mint'),
        ('Cookware Set', 149.99, 3, 'A comprehensive set of high-quality cookware for all your culinary needs.', 'cookware-set.jpg', 50, 1, 'Red');
GO