CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(12,2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    installments INTEGER NOT NULL DEFAULT 1,
    description VARCHAR(255),
    payer_name VARCHAR(100),
    payer_document VARCHAR(20),
    external_reference VARCHAR(100),
    gateway_payment_id VARCHAR(100),
    repair_order_id BIGINT NOT NULL UNIQUE,
    paid_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_payments_repair_order FOREIGN KEY (repair_order_id) REFERENCES repair_orders(id)
);
