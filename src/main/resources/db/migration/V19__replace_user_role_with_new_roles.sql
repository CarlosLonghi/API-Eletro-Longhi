-- Authorization roles reworked: ADMIN/USER -> ADMIN/GERENTE/ATENDENTE/TECNICO/PENDENTE.
-- 'USER' was an authenticated-can-do-almost-everything role; it no longer exists.
-- Existing 'USER' accounts (and every new self-registration) become 'PENDENTE',
-- a role with no permissions, until an ADMIN assigns a real one via PATCH /user/{id}/role.
UPDATE users SET role = 'PENDENTE' WHERE role = 'USER';
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'PENDENTE';
