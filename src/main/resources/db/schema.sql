CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS locations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS payment_methods (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS inventory_ingredients (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantity NUMERIC DEFAULT 0,
    unit VARCHAR,
    cost_per_unit NUMERIC,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    role_id INT NOT NULL REFERENCES roles(id),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS tables (
    id SERIAL PRIMARY KEY,
    location_id INT NOT NULL REFERENCES locations(id),
    table_number INT NOT NULL UNIQUE,
    pos_x NUMERIC,
    pos_y NUMERIC,
    availability BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS menu_items (
    id SERIAL PRIMARY KEY,
    category_id INT REFERENCES categories(id),
    item_code INT NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    price NUMERIC NOT NULL,
    availability BOOLEAN DEFAULT TRUE,
    description TEXT,
    to_kitchen BOOLEAN,
    discount NUMERIC DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS user_sessions (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    login_time TIMESTAMP,
    logout_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS menu_item_ingredients (
    id SERIAL PRIMARY KEY,
    ingredient_id INT NOT NULL REFERENCES inventory_ingredients(id),
    menu_item_id INT NOT NULL REFERENCES menu_items(id),
    quantity_needed NUMERIC NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS payments (
    id SERIAL PRIMARY KEY,
    waiter_id INT NOT NULL REFERENCES users(id),
    method_id INT NOT NULL REFERENCES payment_methods(id),
    amount NUMERIC NOT NULL,
    refunded BOOLEAN DEFAULT FALSE,
    tip NUMERIC DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS order_items (
    id SERIAL PRIMARY KEY,
    menu_item_id INT NOT NULL REFERENCES menu_items(id),
    payment_id INT REFERENCES payments(id),
    waiter_id INT NOT NULL REFERENCES users(id),
    table_id INT NOT NULL REFERENCES tables(id),
    quantity INT NOT NULL,
    discount NUMERIC DEFAULT 0,
    price NUMERIC NOT NULL,
    note TEXT,
    status VARCHAR(50) DEFAULT 'WAITING',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
    );