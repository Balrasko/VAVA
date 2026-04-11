-- ENUMS
CREATE TYPE order_status AS ENUM (
    'RECEIVED',
    'IN_PROGRESS',
    'DONE'
);

CREATE TYPE cash_operation_type AS ENUM (
    'CASH_FLOAT',
    'WITHDRAWAL'
);


-----------------------------------------------------------------------------------------------------------------


-- TABLES
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
    );

CREATE TABLE IF NOT EXISTS locations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
    );

CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
    );

CREATE TABLE IF NOT EXISTS payment_methods (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
    );

CREATE TABLE IF NOT EXISTS inventory_ingredients (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantity NUMERIC DEFAULT 0,
    minimal_quantity NUMERIC DEFAULT 0,
    unit VARCHAR,
    cost_per_unit NUMERIC,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
    );

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    role_id INT NOT NULL REFERENCES roles(id),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
    );

CREATE TABLE IF NOT EXISTS tables (
    id SERIAL PRIMARY KEY,
    location_id INT NOT NULL REFERENCES locations(id),
    table_number INT NOT NULL,
    pos_x NUMERIC,
    pos_y NUMERIC,
    availability BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
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
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
    );

CREATE TABLE IF NOT EXISTS user_sessions (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    login_time TIMESTAMPTZ,
    logout_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
    );

CREATE TABLE IF NOT EXISTS menu_item_ingredients (
    id SERIAL PRIMARY KEY,
    ingredient_id INT NOT NULL REFERENCES inventory_ingredients(id),
    menu_item_id INT NOT NULL REFERENCES menu_items(id),
    quantity_needed NUMERIC NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
    );

CREATE TABLE IF NOT EXISTS payments (
    id SERIAL PRIMARY KEY,
    waiter_id INT NOT NULL REFERENCES users(id),
    method_id INT NOT NULL REFERENCES payment_methods(id),
    amount NUMERIC(10,2) NOT NULL,
    refunded BOOLEAN DEFAULT FALSE,
    tip NUMERIC(10,2) DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
    );

CREATE TABLE IF NOT EXISTS cash_movements (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    operation_type cash_operation_type NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    note TEXT,
    business_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
    );

CREATE TABLE IF NOT EXISTS daily_closings (
    id SERIAL PRIMARY KEY,
    closed_by_user_id INT NOT NULL REFERENCES users(id),
    business_date DATE NOT NULL UNIQUE,
    total_paid NUMERIC NOT NULL,
    total_tips NUMERIC NOT NULL,
    grand_total NUMERIC NOT NULL,
    cash_float NUMERIC NOT NULL,
    cash NUMERIC NOT NULL,
    card NUMERIC NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
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
    status order_status DEFAULT 'RECEIVED',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
    );
