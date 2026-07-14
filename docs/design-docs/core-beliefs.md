# Core Beliefs

Princípios arquiteturais e crenças que orientam as decisões do projeto.

---

## Princípios Fundamentais

### 1. Simplicity Over Cleverness

- Código simples e testável é melhor que código "inteligente"
- Padrão controller → service → repository é preferido
- Evitar abstrações prematuras

### 2. Separation of Concerns

- DTOs (records) separam camada HTTP de lógica
- Services contêm lógica de negócio
- Repositories falam com BD
- Mappers convertem entre camadas

### 3. Immutability in Data Transfer

- DTOs são records (imutáveis)
- Segurança e previsibilidade
- Facilita testes e concorrência

### 4. Spring Data First

- Usar Spring Data convenções para queries
- Evitar SQL raw quando possível
- Naming: `findDevicesByBrandId`, não `getDevicesByBrand`

### 5. Security by Default

- JWT obrigatório para endpoints (exceto `/auth/*`)
- Validações globais (`ApplicationControllerAdvice`)
- Constraints de BD como segunda linha de defesa

### 6. Database as Source of Truth

- Flyway migrations versionadas
- Schema mudanças rastreadas em VCS
- Sem dados em code

---

## O Que Evitar

❌ Static utility classes (use Spring beans)  
❌ Manual JDBC (use Spring Data)  
❌ Custom validation logic (use anotações + Advice)  
❌ Mixing concerns (service com HTTP logic)  
❌ Secrets em application.properties (use env vars)  

---

## Trade-offs Aceitos

| Aspecto | Escolha | Por quê |
|--------|---------|--------|
| Mappers | MapStruct | Type-safe, performance, generated code |
| DTOs | Records | Imutabilidade, menos boilerplate |
| Erros | Global Advice | Consistência, centralizado |
| Auth | JWT | Stateless, escalável |
| BD | SQL + Flyway | Controle total, versionamento |


