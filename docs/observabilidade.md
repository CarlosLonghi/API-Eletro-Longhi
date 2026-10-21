# Observabilidade: métricas, logs e alertas

Mesmo padrão do desafio BCB (Prometheus + Loki + Alloy + Grafana, tudo provisionado por arquivo), sem as partes de RabbitMQ. A stack roda no profile `observability` do compose e coleta a API rodando **em container** (profile `app`). Sem esses profiles, `docker compose up` sobe só o banco, como antes.

```bash
docker compose --profile app --profile observability up -d --build
```

| O quê | Onde |
|---|---|
| API | http://localhost:8080 |
| Grafana | http://localhost:3001 (`admin`/`admin` ou `GRAFANA_ADMIN_*` do `.env`) |
| Prometheus | http://localhost:9090 |
| Loki | só na rede interna; consulte pelo Grafana (Explore → Loki) |

Com a API rodando fora do Docker (`./mvnw spring-boot:run`), o Prometheus não a alcança (o alvo `app` fica fora e o alerta "Alvo fora" dispara) e os logs não chegam ao Loki: o Alloy lê o stdout dos containers. O `/actuator/prometheus` continua disponível em http://localhost:8081.

## Arquitetura

```
 API (container app) ──/actuator/prometheus (porta de gerência 8081)──┐
 postgres-exporter ───────────────────────────────────────────────────┴──► Prometheus ──┐
                                                                                        ├──► Grafana (dashboards + alertas ──► Discord, opcional)
 stdout dos containers do projeto ──► Alloy (socket do Docker) ──► Loki ───────────────┘
```

- **Métricas**: o Prometheus coleta a cada 15s e guarda 15 dias (volume `prometheus_data`).
- **Logs**: no container a API escreve JSON (formato ECS, `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`) no stdout. O Alloy lê o stdout dos containers do mesmo projeto do compose e manda ao Loki com os labels `service` e `level`; o Loki guarda 15 dias (volume `loki_data`). A aplicação não sabe que o Loki existe.
- **Traces**: só o `traceId`/`spanId` nos logs (Micrometer Tracing com Brave, sem exportador). Serve para juntar todas as linhas de uma requisição; no Grafana o `traceId` de um log é um link para os outros logs do mesmo trace.
- **Grafana**: sem volume. Datasources, dashboards e alertas vêm dos arquivos e são recriados a cada subida. O repositório é a fonte da verdade, e o que for mudado pela tela se perde (exporte o JSON e atualize o arquivo).

## Arquivos

```
Dockerfile                               imagem da API (healthcheck no /actuator/health da 8081)
docker-compose.yml                       db; app (profile app); prometheus, postgres-exporter, loki, alloy, grafana (profile observability)
docker-compose.discord.yaml              override que liga os alertas no Discord
observability/
  prometheus.yml                         alvos de coleta (job = nome do serviço no compose)
  loki.yml                               Loki monolítico, filesystem, retenção 15 dias
  alloy.alloy                            containers do projeto → labels service/level → Loki
  grafana/
    provisioning/
      datasources/prometheus.yml         uid fixo "prometheus"
      datasources/loki.yml               uid fixo "loki"; traceId vira link para os logs do trace
      dashboards/dashboards.yml          uma pasta do Grafana por subpasta de dashboards/
      alerting/generic.yml               alertas de infraestrutura (pasta Infra)
      alerting/eletrolonghi.yml          alertas do domínio (pasta Eletro Longhi)
    dashboards/
      infra/spring-boot.json             HTTP, JVM, Hikari, chamadas HTTP de saída, logs
      infra/postgres.json                postgres-exporter
      eletrolonghi/eletrolonghi.json     ordens, pagamentos, Mercado Pago, login, logs (home do Grafana)
    discord/discord.yml                  contact point + política (só com o override)
```

## Métricas da aplicação

Expostas em `/actuator/prometheus` na porta de gerência (`management.server.port`, padrão 8081, não publicada pelo compose). Classes em `src/main/java/.../metrics/`.

| Métrica | Tags | Onde é registrada |
|---|---|---|
| `eletrolonghi_repair_orders_opened_total` | — | `RepairOrderService.save` |
| `eletrolonghi_repair_orders_status_changes_total` | `status` (destino) | `RepairOrderService.updateStatus` |
| `eletrolonghi_repair_orders_by_status` (gauge) | `status` | contado no banco a cada coleta (`countByStatus`) |
| `eletrolonghi_payments_registered_total` | `method` | `PaymentService.save` |
| `eletrolonghi_payments_status_changes_total` | `status` (destino) | `PaymentService.updateStatus` / `syncWithGateway` |
| `eletrolonghi_payments_approved_amount_brl_{sum,count,max}` | `method` | quando o pagamento vira `APPROVED` |
| `eletrolonghi_payments_checkout_links_total` | — | `PaymentService.createCheckoutLink` |
| `eletrolonghi_auth_logins_total` | `result` = `success`, `failure`, `blocked` | `LoginAttemptService` |
| `http_client_requests_seconds` | `client_name="api.mercadopago.com"`, `outcome`... | automático (`RestClient.Builder` do Spring no `MercadoPagoClient`) |
| `http_server_requests_seconds` (com histograma) | `uri`, `status`... | automático |

Todas as séries de negócio são registradas zeradas no startup: o painel mostra 0 em vez de "No data" e o `increase()` conta o primeiro evento. Evite nomes terminados em `_created`: o registry do Prometheus remove esse sufixo (reservado no OpenMetrics).

## Convenções

1. **`job` do Prometheus = nome do serviço no compose** (`app`, `postgres`). Os logs têm o label `service` com o mesmo nome, então o painel de logs do dashboard Spring Boot usa `{service="$job"}`.
2. **Datasources com uid fixo** (`prometheus`, `loki`): dashboards e alertas referenciam os uids, não os nomes.
3. **Toda consulta filtra por `job`.**
4. **Labels de log só de baixa cardinalidade:** `service` e `level`. IDs (traceId, id de ordem) ficam no corpo e são filtrados na consulta (`|= "<id>"` ou `| json`).
5. **Alertas agrupam `by (job)`:** um alerta por serviço afetado, e o resumo usa `{{ $labels.job }}`.
6. **Dashboards genéricos com a tag `generic`** e os do projeto com a tag `app`: os menus "Infra" e "Aplicação" no topo de cada dashboard são montados por essas tags.

Cuidado com o `$` nos arquivos de provisioning do Grafana: `${VAR}` vira variável de ambiente. Para um `$` literal, use `$$` (ver `datasources/loki.yml`). `{{ $labels.x }}` nos alertas não é afetado.

## Consultas úteis

| Pergunta | Consulta |
|---|---|
| Tudo o que aconteceu numa requisição | Loki: `{service="app"} \|= "<traceId>"` (ou clique no traceId de um log) |
| Erros da API | Loki: `{service="app", level="ERROR"}` |
| Logs de um logger | Loki: `{service="app"} \| json \| log_logger=~".*PaymentService"` |
| Alvos fora | Prometheus: `up == 0` |
| p95 HTTP por rota | Prometheus: `histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_seconds_bucket{job="app"}[5m])))` |
| Ordens paradas por etapa | Prometheus: `eletrolonghi_repair_orders_by_status{job="app"}` |
| Faturado hoje | Prometheus: `sum(increase(eletrolonghi_payments_approved_amount_brl_sum{job="app"}[1d]))` |

## Alertas

| Regra | Arquivo | Dispara quando |
|---|---|---|
| Alvo fora | `generic.yml` | algum job do Prometheus para de responder por 1 min |
| Postgres fora | `generic.yml` | o exporter responde, mas não conecta no banco |
| Erros 5xx na API | `generic.yml` | mais de 3 respostas 5xx por minuto, por 5 min |
| Pool do banco saturado | `generic.yml` | requisições esperando conexão do Hikari por 2 min |
| Falhas no Mercado Pago | `eletrolonghi.yml` | alguma chamada ao gateway sem `SUCCESS` nos últimos 10 min |
| Muitas tentativas de login inválidas | `eletrolonghi.yml` | mais de 20 logins com falha ou bloqueados em 10 min |
| Pagamentos recusados | `eletrolonghi.yml` | mais de 2 pagamentos em `REJECTED` na última hora |

Sem o override do Discord, os alertas aparecem só em **Alerting** e no painel "Alertas disparados" do dashboard Eletro Longhi. Para receber no Discord, defina `GRAFANA_DISCORD_WEBHOOK_URL` no `.env` e suba com:

```bash
docker compose -f docker-compose.yml -f docker-compose.discord.yaml --profile app --profile observability up -d
```

## CI

O job `observability` do [`ci.yml`](../.github/workflows/ci.yml) valida o compose (com e sem Discord), as configs do Prometheus/Loki/Alloy, o provisionamento do Grafana (regras, dashboards, datasource, contact point) e faz uma prova ponta a ponta: um login do admin precisa aparecer no Loki e em `eletrolonghi_auth_logins_total` no Prometheus.

## O que ficou de fora

- **Traces distribuídos (Tempo):** o `traceId` nos logs já junta as linhas de uma requisição. Exportar spans (OTLP → Tempo) mostraria a duração de cada etapa (inclusive a chamada ao Mercado Pago) e ligaria log ↔ trace ↔ métrica.
- **Header `X-Trace-Id` na resposta:** o BCB devolve o traceId ao cliente para ligar um erro do frontend aos logs; aqui não foi trazido.
- **Métricas de containers (cAdvisor).**
- **Alta disponibilidade:** Prometheus, Loki e Grafana rodam com uma instância cada, com dados em volume local. Serve para desenvolvimento e demonstração, não para produção.
