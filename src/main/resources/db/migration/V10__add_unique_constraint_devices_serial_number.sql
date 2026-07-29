ALTER TABLE devices
    ALTER COLUMN serial_number SET NOT NULL;

ALTER TABLE devices
    ADD CONSTRAINT uk_devices_serial_number UNIQUE (serial_number);


