ALTER TABLE repair_orders
    ADD COLUMN estimated_cost NUMERIC(12,2),
    ADD COLUMN estimated_completion_date DATE;
