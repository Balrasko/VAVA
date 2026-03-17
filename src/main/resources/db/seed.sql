-- Roles (ADMIN, WAITER, CHEF)
INSERT INTO roles (name) VALUES ('ADMIN'), ('WAITER'), ('CHEF') ON CONFLICT (name) DO NOTHING;


-- Locations
INSERT INTO locations (name) VALUES ('Terrace'), ('Main hall'), ('Bar'), ('Smoking area');


-- Payment_methods (1=Cash, 2=Card, 3=Meal card)
INSERT INTO payment_methods (name) VALUES ('Cash'), ('Card'), ('Meal card') ON CONFLICT (name) DO NOTHING;


-- Categories (Menu)
INSERT INTO categories (name) VALUES ('Non-alcoholic drinks'), ('Alcoholic drinks'), ('Appetizers'),
                                     ('Main dishes'), ('Desserts'), ('Pizzas'), ('Groups');


-- Inventory Ingredients
INSERT INTO inventory_ingredients (name, quantity, unit, cost_per_unit) VALUES
('Milk', 10, 'l', 1.2),
('Coffee', 5, 'kg', 15.0),
('Lemon', 20, 'pcs', 0.5),
('Potatoes', 50, 'kg', 1.0),
('Beef', 10, 'kg', 12.0),
('Chicken', 10, 'kg', 12.0),
('Flour', 20, 'kg', 0.8),
('Tomatoes', 15, 'kg', 1.0),
('Cheese', 10, 'kg', 8.0),
('Eggs', 100, 'pcs', 0.2),
('Ham', 8, 'kg', 6.0),
('Rice', 15, 'kg', 1.5),
('Chocolate', 5, 'kg', 10.0),
('Sugar', 10, 'kg', 1.2),
('Cream', 8, 'l', 2.0),
('Garlic', 5, 'kg', 3.0),
('Bread', 20, 'pcs', 0.5),
('Salt', 2, 'kg', 0.5),
('Black pepper', 1, 'kg', 8.0),
('Onion', 10, 'kg', 1.0),
('Oil', 10, 'l', 2.5),
('Basil', 1, 'kg', 10.0),
('Beer', 100, 'pcs', 1.5),
('Red Wine', 50, 'l', 5.0),
('White Wine', 50, 'l', 5.0),
('Mineral Water', 100, 'l', 0.5),
('Mint', 1, 'kg', 8.0);


-- Users (Role IDs: 1=ADMIN, 2=WAITER, 3=CHEF)
INSERT INTO users (role_id, name, email, password) VALUES
(1, 'Mister Admin', 'admin@vava.com', 'admin123'),
(2, 'Waiter1', 'waiter1@vava.com', 'waiter123'),
(2, 'Waiter2', 'waiter2@vava.com', 'waiter123'),
(3, 'Le Chef1', 'chef1@vava.com', 'chef123'),
(3, 'Le Chef2', 'chef2@vava.com', 'chef123')
ON CONFLICT (email) DO NOTHING;


-- Tables (Location IDs: 1=Terrace, 2=Main hall, 3=Bar, 4=Smoking area)
INSERT INTO tables (location_id, table_number, pos_x, pos_y) VALUES
-- Terrace (1)
(1, 1, 30.0, 30.0),
(1, 2, 300.0, 30.0),
(1, 3, 30.0, 160.0),
(1, 4, 300.0, 160.0),
-- Main hall (2)
(2, 5, 30.0, 30.0),
(2, 6, 300.0, 30.0),
(2, 7, 30.0, 160.0),
(2, 8, 300.0, 160.0),
-- Bar (3)
(3, 9, 30.0, 30.0),
(3, 10, 300.0, 30.0),
(3, 11, 30.0, 160.0),
(3, 12, 300.0, 160.0),
-- Smoking area (4)
(4, 13, 30.0, 30.0),
(4, 14, 300.0, 30.0),
(4, 15, 30.0, 160.0)
ON CONFLICT (table_number) DO NOTHING;


-- Menu Items
INSERT INTO menu_items (category_id, item_code, name, price, description, to_kitchen) VALUES
-- Non-alcoholic drinks (1)
(1, 100, 'Espresso', 2.50, 'Classic Italian espresso', false),
(1, 101, 'Lemonade', 3.00, 'Homemade lemon lemonade with fresh mint', false),
(1, 102, 'Cappuccino', 3.20, 'Coffee with smooth milk foam', false),
-- Alcoholic drinks (2)
(2, 200, 'Beer', 3.00, 'Slovak pale beer', false),
(2, 201, 'Red Wine', 4.50, 'Dry red wine', false),
(2, 202, 'White Wine', 4.50, 'Dry white wine', false),
-- Appetizers (3)
(3, 300, 'Salami Sandwich', 2.50, 'Sandwich with salami, cheese, and tomato', true),
(3, 301, 'Bruschetta', 3.50, 'Toasted bread with tomatoes and basil', true),
(3, 302, 'Beef Tartare', 6.50, 'Beef tartare with toast', true),
-- Main dishes (4)
(4, 400, 'Beef Goulash', 8.50, 'Traditional beef goulash with boiled potatoes', true),
(4, 401, 'Fried Cheese', 7.50, 'Fried cheese with fries and tartar sauce', true),
(4, 402, 'Chicken Steak', 9.00, 'Grilled chicken steak with rice', true),
-- Desserts (5)
(5, 500, 'Pancakes', 4.50, 'Sweet pancakes with jam and whipped cream', true),
(5, 501, 'Chocolate Cake', 4.00, 'Homemade chocolate cake', true),
(5, 502, 'Ice Cream', 3.50, 'Vanilla ice cream with chocolate topping', true),
-- Pizzas (6)
(6, 600, 'Margherita', 6.00, 'Classic pizza with mozzarella and tomatoes', true),
(6, 601, 'Prosciutto', 7.50, 'Pizza with ham and cheese', true),
(6, 602, 'Quattro Formaggi', 8.00, 'Pizza with four types of cheese', true),
-- Soups (7)
(7, 700, 'Garlic Soup', 3.00, 'Garlic soup with croutons', true),
(7, 701, 'Tomato Soup', 3.20, 'Tomato soup with basil', true),
(7, 702, 'Chicken Broth', 3.50, 'Strong chicken broth with noodles', true)
ON CONFLICT (item_code) DO NOTHING;


-- Menu Item Ingredients
INSERT INTO menu_item_ingredients (ingredient_id, menu_item_id, quantity_needed) VALUES
-- Espresso (1)
(2, 1, 0.015),
-- Lemonade (2)
(3, 2, 0.5),       -- Lemon
(26, 2, 0.3),      -- Mineral Water
(27, 2, 0.01),     -- Mint
(14, 2, 0.02),     -- Sugar
-- Cappuccino (3)
(2, 3, 0.015),     -- Coffee
(1, 3, 0.2),       -- Milk
-- Beer (4)
(23, 4, 1),
-- Red Wine (5)
(24, 5, 0.2),
-- White Wine (6)
(25, 6, 0.2),
-- Salami Sandwich (7)
(17, 7, 0.1),      -- Bread
(11, 7, 0.1),      -- Ham
(9, 7, 0.05),      -- Cheese
(8, 7, 0.05),      -- Tomatoes
-- Bruschetta (8)
(17, 8, 0.1),      -- Bread
(8, 8, 0.1),       -- Tomatoes
(16, 8, 0.01),     -- Garlic
(22, 8, 0.01),     -- Basil
(21, 8, 0.01),     -- Oil
-- Beef Tartare (9)
(5, 9, 0.15),      -- Beef
(17, 9, 0.1),      -- Bread
(20, 9, 0.05),     -- Onion
(18, 9, 0.005),    -- Salt
(19, 9, 0.002),    -- Black pepper
-- Beef Goulash (10)
(5, 10, 0.2),      -- Beef
(4, 10, 0.3),      -- Potatoes
(20, 10, 0.1),     -- Onion
(16, 10, 0.02),    -- Garlic
(18, 10, 0.01),    -- Salt
(19, 10, 0.005),   -- Black pepper
(21, 10, 0.02),    -- Oil
-- Fried Cheese (11)
(9, 11, 0.2),      -- Cheese
(7, 11, 0.1),      -- Flour
(10, 11, 1),       -- Eggs
(21, 11, 0.05),    -- Oil
-- Chicken Steak (12)
(6, 12, 0.25),     -- Chicken
(12, 12, 0.2),     -- Rice
(21, 12, 0.02),    -- Oil
(18, 12, 0.005),   -- Salt
-- Pancakes (13)
(7, 13, 0.1),      -- Flour
(10, 13, 2),       -- Eggs
(1, 13, 0.2),      -- Milk
(14, 13, 0.05),    -- Sugar
(21, 13, 0.01),    -- Oil
-- Chocolate Cake (14)
(7, 14, 0.15),     -- Flour
(10, 14, 2),       -- Eggs
(13, 14, 0.1),     -- Chocolate
(14, 14, 0.1),     -- Sugar
(1, 14, 0.1),      -- Milk
-- Ice Cream (15)
(1, 15, 0.2),      -- Milk
(14, 15, 0.05),    -- Sugar
(15, 15, 0.1),     -- Cream
-- Margherita (16)
(7, 16, 0.2),      -- Flour
(9, 16, 0.15),     -- Cheese
(8, 16, 0.15),     -- Tomatoes
(21, 16, 0.02),    -- Oil
-- Prosciutto (17)
(7, 17, 0.2),      -- Flour
(9, 17, 0.15),     -- Cheese
(11, 17, 0.15),    -- Ham
-- Quattro Formaggi (18)
(7, 18, 0.2),      -- Flour
(9, 18, 0.3),      -- Cheese
-- Garlic Soup (19)
(16, 19, 0.03),    -- Garlic
(17, 19, 0.1),     -- Bread
(21, 19, 0.01),    -- Oil
-- Tomato Soup (20)
(8, 20, 0.2),      -- Tomatoes
(16, 20, 0.01),    -- Garlic
(22, 20, 0.01),    -- Basil
-- Chicken Broth (21)
(6, 21, 0.2),      -- Chicken
(20, 21, 0.05),    -- Onion
(18, 21, 0.005);   -- Salt


-- Payments (method_id: 1=Cash, 2=Card, 3=Meal card)
-- Tip in %
INSERT INTO payments (waiter_id, method_id, amount, tip, created_at) VALUES
(3, 2, 306.43, 10, '2026-03-13 12:00:00'),
(3, 1,  54.20,  5, '2026-03-13 10:00:00'),
(3, 2,  18.90,  0, '2026-03-12 15:00:00'),
(3, 3, 124.50,  8, '2026-03-12 12:00:00'),
(3, 1,  75.00, 15, '2026-03-11 13:00:00'),
(3, 2, 89.30, 12, '2026-03-14 18:30:00'),
(3, 1, 42.75,  0, '2026-03-14 11:15:00'),
(3, 3, 110.00,  5, '2026-03-15 13:45:00'),
(3, 2, 67.80, 10, '2026-03-15 19:00:00'),
(3, 1,  35.60,  0, '2026-03-16 09:30:00');


-- User Sessions
INSERT INTO user_sessions (user_id, login_time, logout_time) VALUES
-- Mister Admin (1)
(1, '2026-03-13 09:00:00', '2026-03-13 17:00:00'),
(1, '2026-03-14 09:00:00', '2026-03-14 17:00:00'),
-- Waiter1 (2)
(2, '2026-03-13 09:00:00', '2026-03-13 17:00:00'),
(2, '2026-03-14 09:30:00', '2026-03-14 17:30:00'),
-- Waiter2 (3)
(3, '2026-03-13 09:00:00', '2026-03-13 17:00:00'),
(3, '2026-03-14 09:15:00', '2026-03-14 17:15:00'),
-- Le Chef1 (4)
(4, '2026-03-13 10:00:00', '2026-03-13 18:00:00'),
(4, '2026-03-14 10:00:00', '2026-03-14 18:00:00'),
-- Le Chef2 (5)
(5, '2026-03-13 10:30:00', '2026-03-13 18:30:00'),
(5, '2026-03-14 10:30:00', '2026-03-14 18:30:00');


-- Order Items
INSERT INTO order_items (menu_item_id, payment_id, waiter_id, table_id, quantity, discount, price, note, status) VALUES
(1, 1, 2, 1, 2, 0, 5.00, 'Extra espresso shot', 'DONE'),
(2, 1, 2, 1, 1, 0, 3.00, 'No sugar', 'DONE'),
(7, 2, 2, 2, 1, 0, 2.50, NULL, 'WAITING'),
(10, 3, 2, 3, 2, 10, 15.30, 'No salt', 'IN_PROGRESS'),
(13, 4, 2, 4, 1, 0, 4.50, NULL, 'DONE'),
(16, 5, 2, 5, 1, 0, 6.00, 'Extra cheese', 'WAITING'),
(19, 6, 3, 1, 2, 0, 6.00, NULL, 'DONE'),
(20, 7, 3, 2, 1, 0, 3.20, NULL, 'IN_PROGRESS'),
(18, 8, 3, 3, 1, 0, 8.00, NULL, 'WAITING'),
(11, 9, 3, 4, 2, 5, 15.00, 'No pepper', 'DONE'),
(2, 10, 3, 5, 3, 0, 9.00, 'Extra lemonade', 'WAITING'),
(3, 1, 2, 2, 1, 0, 3.20, 'Skim milk', 'IN_PROGRESS'),
(8, 2, 2, 3, 1, 0, 3.00, NULL, 'DONE'),
(4, 3, 3, 1, 2, 0, 9.00, NULL, 'WAITING'),
(11, 4, 3, 2, 1, 0, 4.50, NULL, 'DONE'),
(16, 5, 2, 3, 1, 0, 6.00, 'Extra cheese', 'IN_PROGRESS'),
(1, 1, 2, 6, 2, 0, 5.00, 'Extra espresso shot', 'DONE'),
(2, 1, 2, 6, 1, 0, 3.00, 'No sugar', 'DONE'),
(7, 2, 2, 8, 1, 0, 2.50, NULL, 'WAITING'),
(10, 3, 2, 9, 2, 10, 15.30, 'No salt', 'IN_PROGRESS'),
(13, 4, 2, 9, 1, 0, 4.50, NULL, 'DONE'),
(16, 5, 2, 9, 1, 0, 6.00, 'Extra cheese', 'WAITING'),
(19, 6, 3, 13, 2, 0, 6.00, NULL, 'DONE'),
(20, 7, 3, 15, 1, 0, 3.20, NULL, 'IN_PROGRESS'),
(18, 8, 3, 12, 1, 0, 8.00, NULL, 'WAITING'),
(11, 9, 3, 12, 2, 5, 15.00, 'No pepper', 'DONE'),
(2, 10, 3, 8, 3, 0, 9.00, 'Extra lemonade', 'WAITING'),
(3, 1, 2, 8, 1, 0, 3.20, 'Skim milk', 'IN_PROGRESS'),
(8, 2, 2, 13, 1, 0, 3.00, NULL, 'DONE'),
(4, 3, 3, 13, 2, 0, 9.00, NULL, 'WAITING'),
(11, 4, 3, 11, 1, 0, 4.50, NULL, 'DONE'),
(16, 5, 2, 14, 1, 0, 6.00, 'Extra cheese', 'IN_PROGRESS'),
(17, 6, 3, 4, 2, 0, 15.00, 'No ham', 'WAITING');
