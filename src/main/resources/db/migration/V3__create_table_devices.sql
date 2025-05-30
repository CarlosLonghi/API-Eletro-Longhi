CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    model VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    brand_id BIGINT NOT NULL,
    CONSTRAINT fk_devices_brand FOREIGN KEY(brand_id) REFERENCES brands(id)
);
