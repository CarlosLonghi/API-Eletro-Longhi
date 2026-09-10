-- Revert the soft-delete column for payments (added in V18).
-- Payment is the inverse side of a @OneToOne eagerly loaded with RepairOrder;
-- with @SoftDelete the join predicate makes Hibernate raise FetchNotFoundException
-- for every repair order that has no payment, and the UNIQUE repair_order_id would
-- let a logically-deleted row block a new payment for the same order.
-- DELETE /payment/{id} is a physical delete.
ALTER TABLE payments DROP COLUMN deleted_at;
