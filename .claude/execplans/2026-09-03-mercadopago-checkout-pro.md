# ExecPlan: Mercado Pago Checkout Pro (link de pagamento) + polling

**Date**: 2026-09-03
**Status**: Complete

## Goal

Gerar um link de pagamento do Mercado Pago (Checkout Pro) para um `Payment` já
registrado e, sem webhook, conciliar a situação do pagamento por polling manual.

## Big Picture & Context

**Current state**: `MercadoPagoClient` só tem `getPayment` (esqueleto); pagamentos são
todos registrados manualmente. Não há forma de cobrar o cliente remotamente.
**Desired state**: `POST /payment/{id}/checkout` cria uma *preference* no MP e devolve o
`init_point` (link/QR); `POST /payment/{id}/sync` busca o pagamento no MP por
`external_reference`, atualiza o `Payment.status` local e dispara o avanço da ordem se aprovado.
**Why now**: fase seguinte do módulo de pagamentos (a maquininha/API Point continua adiada;
a app ainda não está hospedada, então polling em vez de webhook).
**Affected modules**: `client` (+ `client/dto`), `entity/enums/PaymentMethod`, `service/PaymentService`,
`controller/PaymentController` (+ `PaymentApi`, `response`), `exception` (+ `ApplicationControllerAdvice`),
docs, testes. **Sem migração** (reaproveita `external_reference` / `gateway_payment_id`).

### Decisões (com o usuário)

| Tema | Decisão |
|---|---|
| Forma do fluxo | Cria o `Payment` primeiro (`POST /payment`, method `MERCADO_PAGO_CHECKOUT`), depois `POST /payment/{id}/checkout` gera a preference |
| Confirmação | Endpoint manual `POST /payment/{id}/sync` (polling); **sem** job agendado nesta fase |
| Persistência | `external_reference = "payment-<id>"` gravado ao gerar o link; `gateway_payment_id` recebe o id do pagamento real do MP no sync. Preference id não é persistido (o search usa `external_reference`) |

## Progress Checklist

- [x] `PaymentMethod.MERCADO_PAGO_CHECKOUT`
- [x] `client/dto`: `PreferenceRequest`, `PreferenceItem`, `CheckoutPreference`, `PaymentSearchResponse`
- [x] `MercadoPagoClient.createCheckoutPreference(...)` + `findPaymentByExternalReference(...)` (+ javadoc)
- [x] `PaymentService.createCheckoutLink(id)` + `syncWithGateway(id)` (+ `applyStatus` extraído, dep `MercadoPagoClient`)
- [x] `exception/PaymentGatewayException` (502) + `exception/InvalidPaymentCheckoutException` (422) + handlers
- [x] `controller/response/CheckoutLinkResponse` (só `initPoint`)
- [x] `PaymentController`: `POST /{id}/checkout`, `POST /{id}/sync`
- [x] `PaymentApi`: documentar as duas operações
- [x] Testes (client, service, controller, advice)
- [x] Docs (`CLAUDE.md`, `GLOSSARY`, `ARCHITECTURE`, `README`)
- [x] `./mvnw verify` verde — 196 testes, cobertura de linha 0.97

## Plan of Work

### `PaymentMethod`
Novo valor `MERCADO_PAGO_CHECKOUT("Link de pagamento (Mercado Pago)")`.

### `MercadoPagoClient`
- `Optional<CheckoutPreference> createCheckoutPreference(String title, BigDecimal amount, String externalReference)`
  → `POST /checkout/preferences` com um item (`quantity 1`, `currency_id "BRL"`).
- `Optional<GatewayPaymentSnapshot> findPaymentByExternalReference(String externalReference)`
  → `GET /v1/payments/search?external_reference=…&sort=date_created&criteria=desc`, devolve `results[0]`.
- Ambos: `configured == false` ou `RestClientException` → `Optional.empty()` (padrão do `getPayment`).

### `PaymentService`
- dep nova `MercadoPagoClient`.
- `createCheckoutLink(Long id) → Optional<CheckoutLinkResponse>`: 404 se não achar; 422
  (`InvalidPaymentCheckoutException`) se `method != MERCADO_PAGO_CHECKOUT` ou `status != PENDING`;
  chama o client, `empty → PaymentGatewayException` (502); grava `externalReference`; devolve `{initPoint, externalReference}`.
- `syncWithGateway(Long id) → Optional<Payment>`: 404 se não achar; 422 se `externalReference == null`;
  busca no MP; se achou, grava `gatewayPaymentId`, mapeia status MP→`PaymentStatus` e reaplica via
  `applyStatus` (aprova → `paidAt` + `RepairOrderService.markPaymentReceived`); se não achou, devolve inalterado.
- `applyStatus(Payment, PaymentStatus)` extraído e reaproveitado por `updateStatus`.
- `mapStatus(String)`: approved/authorized→APPROVED; pending/in_process/in_mediation→PENDING;
  rejected→REJECTED; cancelled→CANCELLED; refunded/charged_back→REFUNDED; outro→null (sem mudança).

### Controller / API
- `POST /payment/{id}/checkout` → `CheckoutLinkResponse` (200) / 404 / 422 / 502.
- `POST /payment/{id}/sync` → `PaymentResponse` (200) / 404 / 422 / 502.
- Ambos abertos a qualquer usuário autenticado (sem mudança no `SecurityConfig`).

## Validation & Acceptance

**Acceptance**:
- [ ] `POST /payment` (`method:"MERCADO_PAGO_CHECKOUT"`) → 201 PENDING; `POST /payment/{id}/checkout` →
  200 com `initPoint`; `external_reference` gravado.
- [ ] `POST /payment/{id}/checkout` em pagamento não-checkout → 422; com o MP fora do ar / sem token → 502.
- [ ] `POST /payment/{id}/sync` após pagamento aprovado no MP → `status=APPROVED`, `paidAt` setado,
  ordem em `PAYMENT_RECEIVED`.
- [ ] `./mvnw verify` verde.

**Test scenarios** (unit, com `MockRestServiceServer` / Mockito):
1. preference criada → `init_point` retornado, `externalReference` persistido.
2. search retorna `approved` → `syncWithGateway` aprova o pagamento e avança a ordem.
3. search vazio → pagamento inalterado.

## Outcomes & Retrospective

**What shipped**: `POST /payment/{id}/checkout` (gera `init_point` do Checkout Pro, grava
`external_reference`) e `POST /payment/{id}/sync` (polling: busca o pagamento no MP e reaplica
o status, disparando o avanço da ordem). `MercadoPagoClient` ganhou `createCheckoutPreference`
e `findPaymentByExternalReference`. Novo `PaymentMethod.MERCADO_PAGO_CHECKOUT`. Sem migração.

**Decisões de implementação**:
- `CheckoutLinkResponse` só expõe `initPoint`; `external_reference` já vai no `PaymentResponse`.
- Preference id **não** é persistido (o polling usa `external_reference`); `gateway_payment_id`
  recebe o id do pagamento real do MP no primeiro sync bem-sucedido.
- `applyStatus` extraído em `PaymentService` e reaproveitado por `updateStatus` e `syncWithGateway`.
- `mapGatewayStatus`: approved/authorized→APPROVED, pending/in_process/in_mediation→PENDING,
  rejected→REJECTED, cancelled→CANCELLED, refunded/charged_back→REFUNDED, desconhecido→sem mudança.

**What's left** (fase futura): maquininha via **API Point** (`createPointPaymentIntent`),
endpoint de webhook público quando a app for hospedada (mantendo o polling como reconciliação),
e opcionalmente um job `@Scheduled` que reaproveita `syncWithGateway`.
