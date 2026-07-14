# Generated / DB Schema

Espaço reservado para documentação auto-gerada e schemas.

---

## DB Schema

Documentação do schema PostgreSQL atual.

### Tabelas

```
brands
├── id (PK, auto-increment)
├── name (UNIQUE, NOT NULL)
├── created_at
└── updated_at

accessories
├── id (PK)
├── name (UNIQUE, NOT NULL)
├── created_at
└── updated_at

devices
├── id (PK)
├── name (NOT NULL)
├── brand_id (FK → brands)
├── created_at
└── updated_at

devices_accessories (JOIN TABLE)
├── device_id (FK → devices)
├── accessory_id (FK → accessories)
└── PRIMARY KEY (device_id, accessory_id)

customers
├── id (PK)
├── email (UNIQUE, NOT NULL)
├── name (NOT NULL)
├── phone
├── created_at
└── updated_at

repair_orders
├── id (PK)
├── status (VARCHAR, default PENDING)
├── device_id (FK → devices)
├── customer_id (FK → customers)
├── description
├── created_at
└── updated_at

users
├── id (PK)
├── email (UNIQUE, NOT NULL)
├── password (hashed)
├── created_at
└── updated_at
```

---

## Notas

- Schema é definido e evoluído via Flyway migrations em `src/main/resources/db/migration/`
- Versão atual: V7
- Para mudanças, crie novo arquivo V8__, V9__, etc.

---

## Próximas Adições (Exemplo)

Quando adicionar nova tabela/coluna, documente aqui com:
- Nome da migration (ex: V8__)
- Mudanças realizadas
- Impacto em entidades Java


