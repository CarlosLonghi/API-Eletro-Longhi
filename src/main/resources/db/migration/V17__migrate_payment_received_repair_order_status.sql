-- Backfill for the removal of RepairOrderStatus.PAYMENT_RECEIVED.
-- The payment of a repair order is now tracked solely by the Payment entity
-- (1:1, its own PaymentStatus). Rows still stored as 'PAYMENT_RECEIVED' would
-- fail to map (@Enumerated(EnumType.STRING)) and break any query that returns them.
-- Move them back to 'REPAIR_COMPLETED'; whether the order was actually paid is
-- carried by the linked Payment row, and 'REPAIR_COMPLETED -> DEVICE_COLLECTED'
-- now requires an APPROVED payment anyway.
UPDATE repair_orders SET status = 'REPAIR_COMPLETED' WHERE status = 'PAYMENT_RECEIVED';
