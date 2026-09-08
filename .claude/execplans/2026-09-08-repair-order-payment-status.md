# ExecPlan: Remover PAYMENT_RECEIVED e expor o status do pagamento na ordem

**Date**: 2026-09-08
**Status**: Complete

## Goal

Remover o passo `PAYMENT_RECEIVED` do workflow da ordem de reparo (estado duplicado
agora que `Payment` é uma entidade real) e, no lugar dele, expor o status do pagamento
vinculado na listagem de reparos — com filtro por `paymentStatus` e uma trava que impede
marcar a ordem como `DEVICE_COLLECTED` sem pagamento aprovado.

## Big Picture & Context

**Current state**: `RepairOrderStatus` tem `PAYMENT_RECEIVED` entre `REPAIR_COMPLETED` e
`DEVICE_COLLECTED`. Aprovar um `Payment` chama `RepairOrderService.markPaymentReceived`,
que avança a ordem `REPAIR_COMPLETED → PAYMENT_RECEIVED`. A informação "foi pago?" vive
em dois lugares (status da ordem + `Payment.status`) e pode divergir.
**Desired state**: `Payment` é a única fonte da verdade sobre pagamento. A ordem para em
`REPAIR_COMPLETED` até ser coletada. `RepairOrderResponse` carrega `paymentStatus` +
`paymentId`. `GET /repair-order?paymentStatus=…` filtra. `→ DEVICE_COLLECTED` exige
pagamento `APPROVED`.
**Why now**: o módulo de pagamentos (`2026-09-02-payments-module.md`,
`2026-09-03-mercadopago-checkout-pro.md`) já entrou; o passo do workflow ficou redundante.
**Affected modules**: `entity/enums`, `entity`, `repository` (+ specification),
`controller` (response + api spec), `mapper`, `service`, `exception`, `config`
(`ApplicationControllerAdvice`), testes, docs.

## Progress Checklist

- [x] Remover `PAYMENT_RECEIVED` de `RepairOrderStatus`
- [x] Remover `RepairOrderService.markPaymentReceived` + chamada em `PaymentService.applyApproved`
- [x] `Payment.repairOrder` → `@OneToOne`; `RepairOrder.payment` inverso; `@EntityGraph` no `findAll`
- [x] `RepairOrderResponse` (`paymentStatus` + `paymentId`) + `RepairOrderMapper`
- [x] `RepairOrderNotPaidException` + handler 422 + guarda em `RepairOrderService`
- [x] Filtro `paymentStatus` (specification + service + controller + api spec)
- [x] Prosa Swagger em `PaymentApi`
- [x] Testes (unit + integração) e cobertura ≥ 0.80 — `./mvnw verify`, 198 testes, 0 falhas
- [x] Docs: `CLAUDE.md`, `GLOSSARY.md`, `ARCHITECTURE.md`, `README.md`
- [ ] Verificação manual (pendente — rodar a app localmente)

## Surprises & Discoveries

**2026-09-08**: Nenhuma migração de **schema** necessária — `repair_orders.status` é
`VARCHAR(50)` sem check constraint, e a FK `payments.repair_order_id` já é `NOT NULL UNIQUE`.
**2026-09-08 (pós-merge local)**: era necessária migração de **dados**. Não havia seed
nos scripts, mas o banco de dev tinha a ordem #9 em `PAYMENT_RECEIVED` (populada à mão) —
`@Enumerated(EnumType.STRING)` estoura `IllegalArgumentException` ao ler a linha → `GET
/repair-order` 500 em qualquer página que a inclua. Corrigido com
`V17__migrate_payment_received_repair_order_status.sql`
(`UPDATE ... SET status = 'REPAIR_COMPLETED' WHERE status = 'PAYMENT_RECEIVED'`).
**Lição**: `ls db/migration` não conta dados fora dos scripts — checar o banco em uso
antes de afirmar "sem migração".
**2026-09-08**: `PaymentService` mantém a dependência de `RepairOrderService` — ainda é
usada em `resolveRepairOrder` (não só no auto-avanço removido).
**2026-09-08**: `@OneToOne(mappedBy)` opcional não é lazy-proxiável sem bytecode
enhancement; sem `@EntityGraph` a listagem dispararia um SELECT extra por linha.

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-09-08 | Remover `PAYMENT_RECEIVED`; **sem** auto-avanço ao aprovar | Reverte a decisão de 2026-09-02 (`payments-module`, Decision Log): `Payment` virou entidade e o passo do workflow passou a ser estado duplicado |
| 2026-09-08 | `RepairOrderResponse`: `paymentStatus` + `paymentId` planos (null = sem pagamento) | Espelha `PaymentResponse.repairOrderId`; front consegue linkar ao pagamento |
| 2026-09-08 | `→ DEVICE_COLLECTED` exige `Payment` `APPROVED` → 422 (`RepairOrderNotPaidException`) | Preserva a garantia "não entrega aparelho não pago" que o workflow dava implicitamente, agora com fonte no `Payment` |
| 2026-09-08 | Novo filtro `paymentStatus` em `GET /repair-order` | "listar reparos não pagos / pendentes" |
| 2026-09-08 | `Payment.repairOrder` → `@OneToOne` + `@EntityGraph` na listagem | Mapeamento convencional; evita N+1 (e corrige o de `customer`/`device`) |
| 2026-09-08 | `V17` faz `UPDATE ... SET status = 'REPAIR_COMPLETED' WHERE status = 'PAYMENT_RECEIVED'` | Ordens já pagas mas não coletadas: o "pago" fica no `Payment`; `REPAIR_COMPLETED` é o estado seguro (e `→ DEVICE_COLLECTED` agora exige pagamento aprovado) |

## Context & Orientation

**Assumptions**: cobertura JaCoCo 0.80 mantida; commits semânticos pequenos, sem trailers de IA (`CLAUDE.md`).
**Key files**: `entity/enums/RepairOrderStatus.java`, `entity/{RepairOrder,Payment}.java`,
`repository/RepairOrderRepository.java`, `repository/specification/RepairOrderSpecification.java`,
`controller/response/RepairOrderResponse.java`, `controller/RepairOrderController.java`,
`controller/api/spec/{RepairOrderApi,PaymentApi}.java`, `mapper/RepairOrderMapper.java`,
`service/{RepairOrderService,PaymentService}.java`, `exception/RepairOrderNotPaidException.java`,
`config/ApplicationControllerAdvice.java`, `support/TestFixtures.java`.
**Related docs**: `.claude/GLOSSARY.md` (RepairOrder/Payment), `.claude/ARCHITECTURE.md`
(I8/I10), `.claude/execplans/2026-09-02-payments-module.md` (decisão revertida),
`.claude/execplans/2026-09-03-mercadopago-checkout-pro.md`.

## Plan of Work

### Step 1: Remover `PAYMENT_RECEIVED` e o auto-avanço
**Files**: `entity/enums/RepairOrderStatus.java`, `service/RepairOrderService.java`,
`service/PaymentService.java`, `controller/api/spec/PaymentApi.java`
- [x] apagar a constante; `validateStatusTransition` (checagem relativa `abs(ordinal)==1`) segue válida
- [x] remover `markPaymentReceived` + logger órfão; `applyApproved` só carimba `paidAt`
- [x] atualizar prosa `@Operation` de `updatePaymentStatus` / `syncPayment`

### Step 2: Associação inversa + listagem sem N+1
**Files**: `entity/Payment.java`, `entity/RepairOrder.java`, `repository/RepairOrderRepository.java`
- [x] `Payment.repairOrder` → `@OneToOne(optional = false)` (mesmo `@JoinColumn`)
- [x] `RepairOrder.payment` → `@OneToOne(mappedBy = "repairOrder")`
- [x] override `findAll(Specification, Pageable)` com `@EntityGraph(attributePaths = {"customer","device","payment"})`

### Step 3: Expor na resposta
**Files**: `controller/response/RepairOrderResponse.java`, `mapper/RepairOrderMapper.java`
- [x] componentes `PaymentStatus paymentStatus`, `Long paymentId`
- [x] `@Mapping(source = "payment.status")` / `@Mapping(source = "payment.id")`; nav. null-safe gerada

### Step 4: Trava de coleta
**Files**: `exception/RepairOrderNotPaidException.java`, `config/ApplicationControllerAdvice.java`,
`service/RepairOrderService.java`
- [x] exceção (ctor com `repairOrderId`, mensagem PT) + handler 422
- [x] `guardDeviceCollected(order, next)` chamado em `update` e `updateStatus`; ambos `@Transactional`

### Step 5: Filtro `paymentStatus`
**Files**: `repository/specification/RepairOrderSpecification.java`,
`service/RepairOrderService.java`, `controller/RepairOrderController.java`,
`controller/api/spec/RepairOrderApi.java`
- [x] param novo em `withFilters` / `findAll` / `getAllRepairOrders` / contrato OpenAPI

### Step 6: Testes
- [x] `TestFixtures.repairOrderWithPayment(id, status)` (liga os dois lados)
- [x] `RepairOrderServiceTest`: removidos os 3 testes de `markPaymentReceived`; +3 da guarda de coleta
- [x] `PaymentServiceTest`: `verify(...markPaymentReceived...)` trocado por asserção de `paidAt`
- [x] `RepairOrderControllerTest`: repasse do `paymentStatus`; `findAll` com novo arg
- [x] `RepairOrderRepositoryIntegrationTest`: filtro `paymentStatus` + `@EntityGraph` (flush/clear)
- [x] `./mvnw verify` — BUILD SUCCESS, 198 testes

### Step 7: Docs
- [x] `CLAUDE.md`, `GLOSSARY.md`, `ARCHITECTURE.md` (I8/I10), `README.md`

## Concrete Steps

```bash
./mvnw -q -DskipTests package
./mvnw verify
```
**Expected output**:
```
BUILD SUCCESS  (testes 0 failures; JaCoCo check >= 0.80)
```

## Validation & Acceptance

**Acceptance criteria**:
- [ ] `RepairOrderStatus` não tem mais `PAYMENT_RECEIVED`
- [ ] aprovar um pagamento não altera o status da ordem
- [ ] `GET /repair-order` traz `paymentStatus`/`paymentId` e aceita `?paymentStatus=`
- [ ] `PATCH /repair-order/{id}/status` para `DEVICE_COLLECTED` → 422 sem pagamento `APPROVED`, 200 com
- [ ] cobertura ≥ 0.80

**Test scenarios**:
1. Ordem em `REPAIR_COMPLETED` sem pagamento → `DEVICE_COLLECTED` → **422**
2. Pagamento `PENDING` na ordem → `DEVICE_COLLECTED` → **422**; listagem mostra `paymentStatus=PENDING`
3. `PATCH /payment/{id}/status APPROVED` → 200, `paidAt` setado, ordem **continua** `REPAIR_COMPLETED`
4. `GET /repair-order?paymentStatus=APPROVED` → só ordens pagas
5. `DEVICE_COLLECTED` após aprovação → **200**

## Outcomes & Retrospective

**What shipped**: `RepairOrderStatus` sem `PAYMENT_RECEIVED`; `Payment` é `@OneToOne` com
`RepairOrder` (lado inverso em `RepairOrder.payment`); `RepairOrderResponse` traz
`paymentStatus` + `paymentId`; `GET /repair-order?paymentStatus=` filtra; a transição
`→ DEVICE_COLLECTED` exige pagamento `APPROVED` (`RepairOrderNotPaidException` → 422);
aprovar um pagamento não altera mais o status da ordem. Um commit
(`refactor: replace the PAYMENT_RECEIVED workflow step with a payment check`).
**What we learned**: (1) migração de schema desnecessária, mas o banco de dev tinha a
ordem #9 em `PAYMENT_RECEIVED` (fora dos scripts de seed) → 500 no `GET /repair-order`;
corrigido com `V17` (`UPDATE`), depois do merge local. Checar o banco em uso, não só `ls db/migration`; (2) `@OneToOne(mappedBy)` não é lazy-proxiável → `@EntityGraph` no
`findAll` paginado resolve (e ainda corrige o N+1 pré-existente de `customer`/`device`);
(3) testar o `@EntityGraph` num teste `@Transactional` exige `flush()`+`clear()` — senão
a instância em cache volta com `payment == null`; (4) a mudança não se fatia em commits
menores sem quebrar a compilação (remover a constante do enum obriga a remover
`markPaymentReceived`, que puxa a associação, que puxa mapper/guarda/filtro/fixtures).
**What's left**: verificação manual rodando a app; front-end (prompt já enviado à IA do front).
