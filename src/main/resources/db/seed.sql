-- Roles
INSERT INTO roles (name) VALUES ('ADMIN'), ('MANAGER'), ('WAITER'), ('CHEF') ON CONFLICT (name) DO NOTHING;

-- Users (Role IDs: 1=ADMIN, 2=MANAGER, 3=WAITER, 4=CHEF)
INSERT INTO users (role_id, name, email, password, status) VALUES 
(1, 'Admin User', 'admin@vava.com', 'admin123', true),
(2, 'Test Manager', 'manager@vava.com', 'manager123', true),
(3, 'Jožko Čašník', 'waiter@vava.com', 'waiter123', true),
(4, 'Fero Kuchár', 'chef@vava.com', 'chef123', true)
ON CONFLICT (email) DO NOTHING;

-- Locations
INSERT INTO locations (name) VALUES ('Terasa'), ('Hlavná sála'), ('Bar');

-- Categories (Menu)
INSERT INTO categories (name) VALUES ('Nealko nápoje'), ('Alkoholické nápoje'), ('Predjedlá'), ('Hlavné jedlá'), ('Dezerty');

-- Payment Methods
INSERT INTO payment_methods (name) VALUES ('Hotovosť'), ('Karta'), ('Stravná karta');

-- Tables (Location IDs: 1=Terasa, 2=Hlavná sála, 3=Bar)
INSERT INTO tables (location_id, table_number, pos_x, pos_y) VALUES 
(1, 10, 10.0, 10.0),
(1, 11, 20.0, 10.0),
(1, 12, 30.0, 10.0),
(2, 20, 10.0, 20.0),
(2, 21, 20.0, 20.0),
(3, 30, 10.0, 30.0)
ON CONFLICT (table_number) DO NOTHING;

-- Inventory Ingredients
INSERT INTO inventory_ingredients (name, quantity, unit, cost_per_unit) VALUES 
('Mlieko', 10, 'l', 1.2),
('Káva zrná', 5, 'kg', 15.0),
('Citrón', 20, 'ks', 0.5),
('Zemiaky', 50, 'kg', 1.0),
('Hovädzie mäso', 10, 'kg', 12.0),
('Múka', 20, 'kg', 0.8),
('Vajcia', 100, 'ks', 0.2);

-- Menu Items (Category IDs: 1=Nealko, 2=Alko, 3=Predjedla, 4=Hlavne, 5=Dezerty)
INSERT INTO menu_items (category_id, item_code, name, price, description, to_kitchen) VALUES 
(1, 100, 'Espresso', 2.50, 'Klasické talianske espresso', false),
(1, 101, 'Limonáda', 3.00, 'Domáca citrónová limonáda so sviežou mätou', false),
(4, 300, 'Hovädzí Guláš', 8.50, 'Tradičný hovädzí guláš s varenými zemiakmi', true),
(5, 400, 'Palacinky', 4.50, 'Sladké palacinky s džemom a šľahačkou', true)
ON CONFLICT (item_code) DO NOTHING;

-- Menu Item Ingredients
-- Espresso (1) needs Kava (2)
INSERT INTO menu_item_ingredients (ingredient_id, menu_item_id, quantity_needed) VALUES 
(2, 1, 0.015),
-- Limonada (2) needs Citron (3)
(3, 2, 0.5),
-- Gulas (3) needs Hovadzie maso (5) and Zemiaky (4)
(5, 3, 0.2),
(4, 3, 0.3),
-- Palacinky (4) needs Muka (6), Vajcia (7), Mlieko (1)
(6, 4, 0.1),
(7, 4, 2),
(1, 4, 0.2);
