-- Soft delete: every DELETE through the API now flags the row instead of removing it.
-- Hibernate @SoftDelete(strategy = TIMESTAMP, columnName = "deleted_at") manages this
-- column implicitly (NULL = active, non-NULL = deleted) and appends "deleted_at IS NULL"
-- to every query. Permanent removal is a manual database operation, not exposed by the API.
ALTER TABLE brands        ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE accessories   ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE customers     ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE devices       ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE repair_orders ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE payments      ADD COLUMN deleted_at TIMESTAMP;

-- @SoftDelete on Device also governs its @ManyToMany join table: Hibernate expects the
-- soft-delete column on devices_accessories so join rows are flagged, not deleted.
ALTER TABLE devices_accessories ADD COLUMN deleted_at TIMESTAMP;
