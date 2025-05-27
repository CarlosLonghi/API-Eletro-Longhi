CREATE TABLE devices_accessories (
    device_id BIGINT,
    accessory_id BIGINT,
    CONSTRAINT fk_devices_accessories_device FOREIGN KEY(device_id) REFERENCES devices(id),
    CONSTRAINT fk_devices_accessories_accessory FOREIGN KEY(accessory_id) REFERENCES accessories(id)
);