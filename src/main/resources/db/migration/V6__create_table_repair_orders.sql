CREATE TABLE repair_orders (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    customer_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_services_customer FOREIGN KEY(customer_id) REFERENCES customers(id),
    CONSTRAINT fk_services_device FOREIGN KEY(device_id) REFERENCES devices(id)
)