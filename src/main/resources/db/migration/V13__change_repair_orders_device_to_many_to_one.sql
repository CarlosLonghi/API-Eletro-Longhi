-- Change relationship between repair_orders and devices from OneToOne to ManyToMany.
-- A device can now have multiple repair orders over time, as long as the previous
-- order has status DEVICE_COLLECTED before a new one is created (enforced at application level).
ALTER TABLE repair_orders DROP CONSTRAINT IF EXISTS repair_orders_device_id_key;

