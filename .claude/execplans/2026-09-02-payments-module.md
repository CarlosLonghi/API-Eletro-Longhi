# ExecPlan: Módulo de Pagamentos + Recibo PDF

**Date**: 2026-09-02
**Status**: Complete

## Goal

Registrar o pagamento de cada ordem de reparo (dinheiro, cartão à vista/parcelado, PIX ou
boleto) num grupo de rotas próprio `/payment`, e emitir um recibo (comprovante não-fiscal)
em PDF por pagamento. Um pagamento aprovado avança a ordem vinculada para `PAYMENT_RECEIVED`.

## Big Picture & Context

**Current state**: nenhuma entidade guarda valor monetário; `RepairOrderStatus.PAYMENT_RECEIVED`
é só um rótulo sem transação. Não há PDF nem client HTTP no projeto.
**Desired state**: entidade `Payment` (1:1 com `RepairOrder`), CRUD + listagem paginada/filtrável,
endpoint de recibo PDF, auto-avanço de status ao aprovar, e um esqueleto de client Mercado Pago
para a integração futura (maquininha = API Point; app ainda não hospedada, sem webhook).
**Why now**: a loja precisa controlar recebimentos e entregar comprovante ao cliente.
**Affected modules**: `entity`, `entity/enums`, `controller` (+ `api/spec`, `request`, `response`),
`service`, `repository` (+ `specification`), `mapper`, `config`, `client` (novo pacote),
`exception`, `db/migration`, `application.properties`, `.env.example`, testes.

## Progress Checklist

- [x] `pom.xml` — dependência OpenPDF (`com.github.librepdf:openpdf:2.0.3`)
- [x] `V16__create_table_payments.sql`
- [x] enums `PaymentStatus`, `PaymentMethod`
- [x] entidade `Payment`
- [x] DTOs `PaymentRequest`, `PaymentStatusUpdateRequest`, `PaymentResponse`
- [x] `PaymentMapper`
- [x] `PaymentRepository` + `PaymentSpecification`
- [x] `ShopProperties`, `MercadoPagoProperties`, `@ConfigurationPropertiesScan`, properties + `.env.example`
- [x] `client/MercadoPagoClient` + `client/dto/GatewayPaymentSnapshot`
- [x] `RepairOrderService.markPaymentReceived`
- [x] `PaymentService`
- [x] `PaymentReceiptService` (OpenPDF)
- [x] `PaymentApi` (contrato OpenAPI)
- [x] `PaymentController`
- [x] `SecurityConfig` — `DELETE /payment/{id}` ADMIN
- [x] `PaymentAlreadyExistsForRepairOrderException` + handler no `ApplicationControllerAdvice`
- [x] Testes (service, controller, receipt, client, repository IT, authorization IT, `TestFixtures`)
- [x] Docs (`CLAUDE.md`, `.claude/GLOSSARY.md`, `.claude/ARCHITECTURE.md`, `README.md`)
- [x] `./mvnw verify` verde — 176 testes, cobertura de linha 0.99

## Surprises & Discoveries

**2026-09-02**: `Accessory` NÃO tem campo `price` apesar de `CLAUDE.md`/`GLOSSARY.md` dizerem
que sim. Fora de escopo aqui (o valor fica no `Payment`); nota deixada como está.

**2026-09-02**: Spring Boot 4.1 não expõe um bean `RestClient.Builder` por injeção neste
projeto (contexto falhou com `NoSuchBeanDefinitionException`). `MercadoPagoClient` passou a
construir o `RestClient` via `RestClient.builder()` estático; ctor público anotado
`@Autowired` porque há um segundo ctor package-private só para teste (`MockRestServiceServer`).

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-09-02 | Valor no `Payment`, não no `RepairOrder` | Menor blast radius; ordem não precisa saber de dinheiro |
| 2026-09-02 | `Payment` 1:1 com `RepairOrder` (`repair_order_id UNIQUE NOT NULL`) | Regra de negócio: uma ordem = um pagamento |
| 2026-09-02 | Sem chamada real à API Mercado Pago na v1 | App não hospedada, sem URL de webhook; maquininha (API Point) é passo futuro |
| 2026-09-02 | Recibo não-fiscal via OpenPDF; dados da loja em properties | Nota fiscal real (SEFAZ) é escopo muito maior |
| 2026-09-02 | Auto-avanço `REPAIR_COMPLETED → PAYMENT_RECEIVED` ao aprovar; no-op caso contrário | Reduz passo manual sem quebrar o workflow de ±1 |
| 2026-09-02 | `installments` só > 1 para `CARD`; service normaliza p/ 1 nos demais | Evita constraint cross-field no request |

## Context & Orientation

**Assumptions**: projeto *layered* (não feature-based); `ddl-auto=validate` exige entidade ==
migração; JaCoCo conta `service`/`controller`/`config`/`client` novos.
**Key files**: espelhar o slice de `RepairOrder` (`controller/RepairOrderController`,
`service/RepairOrderService`, `repository/RepairOrderRepository`,
`repository/specification/RepairOrderSpecification`, `mapper/RepairOrderMapper`,
`controller/api/spec/RepairOrderApi`, DTOs correspondentes).
**Related docs**: `.claude/GLOSSARY.md`, `.claude/ARCHITECTURE.md`,
`.claude/execplans/2026-08-24-admin-user-activation.md`, plano em
`~/.claude/plans/quero-implementar-um-gateway-cosmic-whisper.md`.

## Plan of Work

Ver o plano aprovado (link acima) — cada item do Progress Checklist corresponde a uma seção lá.

## Concrete Steps

```bash
git checkout -b feat/payments-module      # feito
ls src/main/resources/db/migration        # confirma V15 → cria V16
./mvnw -q -DskipTests package             # compila + gera MapStruct
./mvnw verify                             # suíte + gate JaCoCo (precisa Docker)
```

## Validation & Acceptance

**Acceptance criteria**:
- [ ] `POST /payment` cria pagamento `PENDING` vinculado a uma ordem; 2º POST p/ a mesma ordem → 422
- [ ] `PATCH /payment/{id}/status` `APPROVED` seta `paidAt` e avança a ordem p/ `PAYMENT_RECEIVED`
- [ ] `GET /payment/{id}/receipt` devolve `application/pdf` com dados de loja + pagamento + ordem
- [ ] `GET /payment` pagina e filtra por `status`/`method`/`repairOrderId`/datas
- [ ] `DELETE /payment/{id}` → 403 p/ USER, 204 p/ ADMIN
- [ ] `./mvnw verify` verde

**Test scenarios**:
1. Pagamento aprovado numa ordem em `REPAIR_COMPLETED` → ordem vira `PAYMENT_RECEIVED`.
2. Pagamento aprovado numa ordem em `IN_REPAIR` → log + ordem inalterada.
3. `POST /payment` com `repairOrder` inexistente → 404.

## Outcomes & Retrospective

**What shipped**: grupo de rotas `/payment` completo (CRUD + `PATCH /{id}/status` + `GET /{id}/receipt`),
entidade `Payment` 1:1 com `RepairOrder`, migração `V16`, recibo PDF via OpenPDF com dados de
`ShopProperties`, avanço automático `REPAIR_COMPLETED → PAYMENT_RECEIVED` ao aprovar, e
`MercadoPagoClient` como esqueleto (só `getPayment`/polling). `DELETE /payment/{id}` ADMIN-only.
Docs (`CLAUDE.md`, `GLOSSARY.md`, `ARCHITECTURE.md` invariante I10, `README.md`) atualizadas.

**What we learned**: ver "Surprises" — `RestClient.Builder` não é injetável aqui.

**What's left** (fase futura, quando a app for hospedada): implementar
`createPointPaymentIntent`/`createCheckoutPreference` no `MercadoPagoClient`, endpoint público
`/payment/webhook/mercado-pago` (assinatura HMAC via `mercadopago.webhook-secret`), e o job de
polling que concilia `Payment.status` com `GatewayPaymentSnapshot`.
