-- Roles (ADMIN, WAITER, CHEF)
INSERT INTO roles (name) VALUES ('ADMIN'), ('WAITER'), ('CHEF') ON CONFLICT (name) DO NOTHING;


-- Locations
INSERT INTO locations (name) VALUES ('Terrace'), ('Main hall'), ('Bar'), ('Smoking area')
ON CONFLICT DO NOTHING;


-- Payment_methods (1=Cash, 2=Card, 3=Meal card)
INSERT INTO payment_methods (name) VALUES ('Cash'), ('Card'), ('Meal card') ON CONFLICT (name) DO NOTHING;


-- Categories (Menu)
INSERT INTO categories (name) VALUES
  ('Non-alcoholic drinks'), ('Alcoholic drinks'), ('Appetizers'),
  ('Main dishes'), ('Desserts'), ('Pizzas'), ('Groups')
ON CONFLICT DO NOTHING;


-- Inventory Ingredients
INSERT INTO inventory_ingredients (name, quantity, minimal_quantity, unit, cost_per_unit) VALUES
('Milk',  10,  5,  'l',   1.2),
('Coffee', 5,  2,  'kg', 15.0),
('Lemon', 20, 10,  'pcs', 0.5),
('Potatoes', 50, 25,  'kg',  1.0),
('Beef',  10,  8,  'kg', 12.0),
('Chicken',10, 12,  'kg', 12.0),
('Flour', 20, 25,  'kg',  0.8),
('Tomatoes', 15, 10,  'kg',  1.0),
('Cheese',10, 12,  'kg',  8.0),
('Eggs', 100, 40,  'pcs', 0.2),
('Ham',    8, 10,  'kg',  6.0),
('Rice',  15, 12,  'kg',  1.5),
('Chocolate', 5,  4,  'kg', 10.0),
('Sugar', 10,  8,  'kg',  1.2),
('Cream',  8,  6,  'l',   2.0),
('Garlic', 5,  4,  'kg',  3.0),
('Bread', 20, 15,  'pcs', 0.5),
('Salt',   2,  5,  'kg',  0.5),
('Black pepper',   1,  2,  'kg',  8.0),
('Onion', 10,  7,  'kg',  1.0),
('Oil',   10,  8,  'l',   2.5),
('Basil',  1,  2,  'kg', 10.0),
('Beer', 100, 40,  'pcs', 1.5),
('Red Wine', 50, 20,  'l',   5.0),
('White Wine',    50, 20,  'l',   5.0),
('Mineral Water', 100, 35, 'l',   0.5),
('Mint',   1,  2,  'kg',  8.0);


-- Users (Role IDs: 1=ADMIN, 2=WAITER, 3=CHEF)
INSERT INTO users (role_id, name, email, password) VALUES
(1, 'Mister Admin', 'admin@vava.com',   'admin123'),
(2, 'Waiter1', 'waiter1@vava.com', 'waiter123'),
(2, 'Waiter2', 'waiter2@vava.com', 'waiter123'),
(3, 'Le Chef1','chef1@vava.com',   'chef123'),
(3, 'Le Chef2','chef2@vava.com',   'chef123'),
-- TODO: delete these
(1, 'a', 'a', 'a'),
(2, 'w', 'w', 'w'),
(3, 'c', 'c', 'c')
ON CONFLICT (email) DO NOTHING;


-- Tables (Location IDs: 1=Terrace, 2=Main hall, 3=Bar, 4=Smoking area)
INSERT INTO tables (location_id, table_number, pos_x, pos_y) VALUES
-- Terrace (1)
(1, 1,  30.0,  30.0),
(1, 2, 300.0,  30.0),
(1, 3,  30.0, 160.0),
(1, 4, 300.0, 160.0),
-- Main hall (2)
(2, 1,  30.0,  30.0),
(2, 2, 300.0,  30.0),
(2, 3,  30.0, 160.0),
(2, 4, 300.0, 160.0),
(2, 5, 30.0, 290.0),
(2, 6, 300.0, 290.0),
-- Bar (3)
(3, 1,  30.0,  30.0),
(3, 2, 300.0,  30.0),
(3, 3,  30.0, 160.0),
(3, 4, 300.0, 160.0),
-- Smoking area (4)
(4, 1,  30.0,  30.0),
(4, 2, 300.0,  30.0),
(4, 3,  30.0, 160.0)
ON CONFLICT (location_id, table_number) DO NOTHING;


-- Menu Items
INSERT INTO menu_items (category_id, item_code, name, price, description, to_kitchen) VALUES
-- Non-alcoholic drinks (1)
(1, 100, 'Espresso',2.50, 'Classic Italian espresso', false),
(1, 101, 'Lemonade',3.00, 'Homemade lemon lemonade with fresh mint', false),
(1, 102, 'Cappuccino',   3.20, 'Coffee with smooth milk foam',  false),
-- Alcoholic drinks (2)
(2, 200, 'Beer', 3.00, 'Slovak pale beer', false),
(2, 201, 'Red Wine',4.50, 'Dry red wine',  false),
(2, 202, 'White Wine',   4.50, 'Dry white wine',false),
-- Appetizers (3)
(3, 300, 'Salami Sandwich', 2.50, 'Sandwich with salami, cheese, and tomato', true),
(3, 301, 'Bruschetta', 3.50, 'Toasted bread with tomatoes and basil',    true),
(3, 302, 'Beef Tartare',    6.50, 'Beef tartare with toast',  true),
-- Main dishes (4)
(4, 400, 'Beef Goulash',   8.50, 'Traditional beef goulash with boiled potatoes', true),
(4, 401, 'Fried Cheese',   7.50, 'Fried cheese with fries and tartar sauce', true),
(4, 402, 'Chicken Steak',  9.00, 'Grilled chicken steak with rice',true),
-- Desserts (5)
(5, 500, 'Pancakes',4.50, 'Sweet pancakes with jam and whipped cream', true),
(5, 501, 'Chocolate Cake', 4.00, 'Homemade chocolate cake',   true),
(5, 502, 'Ice Cream', 3.50, 'Vanilla ice cream with chocolate topping',  true),
-- Pizzas (6)
(6, 600, 'Margherita',6.00, 'Classic pizza with mozzarella and tomatoes', true),
(6, 601, 'Prosciutto',7.50, 'Pizza with ham and cheese',  true),
(6, 602, 'Quattro Formaggi',  8.00, 'Pizza with four types of cheese',    true),
-- Groups/Soups (7)
(7, 700, 'Garlic Soup',   3.00, 'Garlic soup with croutons', true),
(7, 701, 'Tomato Soup',   3.20, 'Tomato soup with basil',    true),
(7, 702, 'Chicken Broth', 3.50, 'Strong chicken broth with noodles', true)
ON CONFLICT (item_code) DO NOTHING;


-- Menu Item Ingredients
INSERT INTO menu_item_ingredients (ingredient_id, menu_item_id, quantity_needed) VALUES
-- Espresso (1)
(2, 1, 0.015),
-- Lemonade (2)
(3,  2, 0.5),
(26, 2, 0.3),
(27, 2, 0.01),
(14, 2, 0.02),
-- Cappuccino (3)
(2, 3, 0.015),
(1, 3, 0.2),
-- Beer (4)
(23, 4, 1),
-- Red Wine (5)
(24, 5, 0.2),
-- White Wine (6)
(25, 6, 0.2),
-- Salami Sandwich (7)
(17, 7, 0.1),
(11, 7, 0.1),
(9,  7, 0.05),
(8,  7, 0.05),
-- Bruschetta (8)
(17, 8, 0.1),
(8,  8, 0.1),
(16, 8, 0.01),
(22, 8, 0.01),
(21, 8, 0.01),
-- Beef Tartare (9)
(5,  9, 0.15),
(17, 9, 0.1),
(20, 9, 0.05),
(18, 9, 0.005),
(19, 9, 0.002),
-- Beef Goulash (10)
(5,  10, 0.2),
(4,  10, 0.3),
(20, 10, 0.1),
(16, 10, 0.02),
(18, 10, 0.01),
(19, 10, 0.005),
(21, 10, 0.02),
-- Fried Cheese (11)
(9,  11, 0.2),
(7,  11, 0.1),
(10, 11, 1),
(21, 11, 0.05),
-- Chicken Steak (12)
(6,  12, 0.25),
(12, 12, 0.2),
(21, 12, 0.02),
(18, 12, 0.005),
-- Pancakes (13)
(7,  13, 0.1),
(10, 13, 2),
(1,  13, 0.2),
(14, 13, 0.05),
(21, 13, 0.01),
-- Chocolate Cake (14)
(7,  14, 0.15),
(10, 14, 2),
(13, 14, 0.1),
(14, 14, 0.1),
(1,  14, 0.1),
-- Ice Cream (15)
(1,  15, 0.2),
(14, 15, 0.05),
(15, 15, 0.1),
-- Margherita (16)
(7,  16, 0.2),
(9,  16, 0.15),
(8,  16, 0.15),
(21, 16, 0.02),
-- Prosciutto (17)
(7,  17, 0.2),
(9,  17, 0.15),
(11, 17, 0.15),
-- Quattro Formaggi (18)
(7, 18, 0.2),
(9, 18, 0.3),
-- Garlic Soup (19)
(16, 19, 0.03),
(17, 19, 0.1),
(21, 19, 0.01),
-- Tomato Soup (20)
(8,  20, 0.2),
(16, 20, 0.01),
(22, 20, 0.01),
-- Chicken Broth (21)
(6,  21, 0.2),
(20, 21, 0.05),
(18, 21, 0.005);


-- Payments (method_id: 1=Cash, 2=Card, 3=Meal card)
-- Tip in %
INSERT INTO payments (waiter_id, method_id, amount, tip, created_at) VALUES
(3, 2, 306.43, 10, '2026-03-13 12:00:00+01:00'),
(3, 1,  54.20,  5, '2026-03-13 10:00:00+01:00'),
(3, 2,  18.90,  0, '2026-03-12 15:00:00+01:00'),
(3, 3, 124.50,  8, '2026-03-12 12:00:00+01:00'),
(3, 1,  75.00, 15, '2026-03-11 13:00:00+01:00'),
(3, 2,  89.30, 12, '2026-03-14 18:30:00+01:00'),
(3, 1,  42.75,  0, '2026-03-14 11:15:00+01:00'),
(3, 3, 110.00,  5, '2026-03-15 13:45:00+01:00'),
(3, 2,  67.80, 10, '2026-03-15 19:00:00+01:00'),
(3, 1,  35.60,  0, '2026-03-16 09:30:00+01:00');


-- User Sessions
INSERT INTO user_sessions (user_id, login_time, logout_time) VALUES
(1, '2026-03-13 09:00:00+01:00', '2026-03-13 17:00:00+01:00'),
(1, '2026-03-14 09:00:00+01:00', '2026-03-14 17:00:00+01:00'),
(2, '2026-03-13 09:00:00+01:00', '2026-03-13 17:00:00+01:00'),
(2, '2026-03-14 09:30:00+01:00', '2026-03-14 17:30:00+01:00'),
(3, '2026-03-13 09:00:00+01:00', '2026-03-13 17:00:00+01:00'),
(3, '2026-03-14 09:15:00+01:00', '2026-03-14 17:15:00+01:00'),
(4, '2026-03-13 10:00:00+01:00', '2026-03-13 18:00:00+01:00'),
(4, '2026-03-14 10:00:00+01:00', '2026-03-14 18:00:00+01:00'),
(5, '2026-03-13 10:30:00+01:00', '2026-03-13 18:30:00+01:00'),
(5, '2026-03-14 10:30:00+01:00', '2026-03-14 18:30:00+01:00');


-- Order Items
-- payment_id = NULL → unpaid, appears on the kitchen board (if to_kitchen = true)
-- payment_id = 1–10 → paid, appears in the history
INSERT INTO order_items (menu_item_id, payment_id, waiter_id, table_id, quantity, discount, price, note, status) VALUES
(1,  1, 2, 1, 2, 0,   5.00, 'Extra espresso shot', 'DONE'),
(2,  1, 2, 1, 1, 0,   3.00, 'No sugar',    'DONE'),
(3,  1, 2, 1, 1, 0,   3.20, 'Skim milk',   'DONE'),
(10, 2, 2, 2, 2, 10, 15.30, 'No salt','DONE'),
(7,  2, 2, 2, 1, 0,   2.50, NULL,  'DONE'),
(13, 3, 2, 3, 1, 0,   4.50, NULL,  'DONE'),
(8,  3, 2, 3, 1, 0,   3.00, NULL,  'DONE'),
(16, 4, 2, 4, 1, 0,   6.00, 'Extra cheese','DONE'),
(11, 4, 3, 4, 2, 5,  15.00, 'No pepper',   'DONE'),
(19, 5, 3, 1, 2, 0,   6.00, NULL,  'DONE'),
(4,  5, 3, 1, 2, 0,   9.00, NULL,  'DONE'),
(20, 6, 3, 2, 1, 0,   3.20, NULL,  'DONE'),
(18, 6, 3, 3, 1, 0,   8.00, NULL,  'DONE'),
(11, 7, 3, 4, 1, 0,   4.50, NULL,  'DONE'),
(2,  7, 3, 5, 3, 0,   9.00, 'Extra lemonade', 'DONE'),
(17, 8, 3, 4, 2, 0,  15.00, 'No ham', 'DONE'),
(1,  8, 2, 6, 2, 0,   5.00, 'Extra espresso shot', 'DONE'),
(16, 9, 2, 3, 1, 0,   6.00, 'Extra cheese','DONE'),
(3,  9, 2, 2, 1, 0,   3.20, NULL,  'DONE'),
(10, 10, 2, 4, 1, 0,  8.50, NULL,  'DONE'),
(7,  NULL, 2, 6,  1, 0,   2.50, NULL,   'RECEIVED'),
(10, NULL, 2, 7,  2, 10, 15.30, 'No salt', 'IN_PROGRESS'),
(16, NULL, 2, 8,  1, 0,   6.00, 'Extra cheese', 'RECEIVED'),
(18, NULL, 3, 9,  1, 0,   8.00, NULL,   'IN_PROGRESS'),
(19, NULL, 3, 10, 2, 0,   6.00, NULL,   'RECEIVED'),
(13, NULL, 2, 11, 1, 0,   4.50, NULL,   'DONE'),
(11, NULL, 3, 12, 2, 5,  15.00, 'No pepper',    'IN_PROGRESS'),
(4,  NULL, 3, 13, 2, 0,   9.00, NULL,   'RECEIVED'),
(17, NULL, 3, 14, 1, 0,   7.50, 'No ham','RECEIVED'),
(20, NULL, 3, 15, 1, 0,   3.20, NULL,   'IN_PROGRESS');