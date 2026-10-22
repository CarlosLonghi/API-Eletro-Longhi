# Observabilidade: métricas, logs e alertas

Mesmo padrão do desafio BCB (Prometheus + Loki + Alloy + Grafana, tudo provisionado por arquivo), sem as partes de RabbitMQ. A stack roda no profile `observability` do compose e coleta a API de dois jeitos:

- **Pela IDE** (desenvolvimento, padrão): basta dar Run (ou `./mvnw spring-boot:run`). Ver [Desenvolvimento pela IDE](#desenvolvimento-pela-ide).
- **Em container** (profile `app`), tudo no Docker:

```bash
docker compose --profile app --profile observability up -d --build
```

| O quê | Onde |
|---|---|
| API | http://localhost:8080 |
| Grafana | http://localhost:3001 (`admin`/`admin` ou `GRAFANA_ADMIN_*` do `.env`) |
| Prometheus | http://localhost:9090 |
| Loki | só na rede interna; consulte pelo Grafana (Explore → Loki) |

## Desenvolvimento pela IDE

Sem perfil ativo, a API sobe com o perfil `dev` (`spring.profiles.default=dev`, ver `application-dev.properties`):

- O `spring-boot-docker-compose` sobe o `db` **e** o profile `observability` (`spring.docker.compose.profiles.active`). O container `app` não sobe, então não há disputa pela 8080/8081.
- `lifecycle-management=start-and-stop` (com `stop.command=stop`): parar a API na IDE (Stop ou Ctrl+C) para a stack junto e libera as portas para outros projetos. Containers e volumes são mantidos, então o banco e o histórico de métricas/logs continuam na próxima subida. Um `kill -9` não dispara o shutdown hook; nesse caso: `docker compose --profile observability stop`. Para olhar o Grafana com a API parada: `docker compose --profile observability up -d`.
- O console continua em texto (legível na IDE) e uma cópia em JSON (ECS) vai para `logs/eletrolonghi.log`. O Alloy lê esse arquivo (montado em `/var/log/eletrolonghi`) com o mesmo label `service="app"` do container.
- O Prometheus coleta o job `app` em dois endereços: `app:8081` (container) e `host.docker.internal:8081` (IDE, via `extra_hosts: host-gateway`). Um deles está sempre fora; por isso o alerta "Alvo fora" e os painéis de "no ar" usam `max(up)`.

Para trocar de modo:

```bash
docker compose --profile app stop app        # container → IDE (libera a 8080/8081)
docker compose --profile app up -d --build   # IDE → container (pare a API na IDE antes)
```

O diretório `logs/` é versionado (só o `.gitkeep`). Se ele não existisse, o bind mount do Alloy o criaria como root e a API não conseguiria gravar o arquivo. Com firewall no host (ufw etc.), libere a 8081 vinda da rede do Docker, senão o alvo `host.docker.internal:8081` fica fora.

## Arquitetura

```
 API (IDE ou container app) ──/actuator/prometheus (porta de gerência 8081)──┐
 postgres-exporter ───────────────────────────────────────────────────┴──► Prometheus ──┐
                                                                                        ├──► Grafana (dashboards + alertas ──► Discord, opcional)
 stdout dos containers + logs/eletrolonghi.log (IDE) ──► Alloy ──► Loki ───────────────┘
```

- **Métricas**: o Prometheus coleta a cada 15s e guarda 15 dias (volume `prometheus_data`).
- **Logs**: no container a API escreve JSON (formato ECS, `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`) no stdout. O Alloy lê o stdout dos containers do mesmo projeto do compose e manda ao Loki com os labels `service` e `level`; o Loki guarda 15 dias (volume `loki_data`). A aplicação não sabe que o Loki existe.
- **Traces**: só o `traceId`/`spanId` nos logs (Micrometer Tracing com Brave, sem exportador). Serve para juntar todas as linhas de uma requisição; no Grafana o `traceId` de um log é um link para os outros logs do mesmo trace.
- **Grafana**: sem volume. Datasources, dashboards e alertas vêm dos arquivos e são recriados a cada subida. O repositório é a fonte da verdade, e o que for mudado pela tela se perde (exporte o JSON e atualize o arquivo).

## Arquivos

```
Dockerfile                               imagem da API (healthcheck no /actuator/health da 8081)
docker-compose.yml                       db; app (profile app); prometheus, postgres-exporter, loki, alloy, grafana (profile observability)
observability/
  prometheus.yml                         alvos de coleta (job = nome do serviço no compose)
  loki.yml                               Loki monolítico, filesystem, retenção 15 dias
  alloy.alloy                            containers do projeto + logs/*.log da IDE → labels service/level → Loki
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
    discord/discord.yml                  contact point + política (copiado pelo entrypoint do Grafana quando há webhook)
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
| Alvo fora | `generic.yml` | nenhuma instância de um job do Prometheus responde por 1 min |
| Postgres fora | `generic.yml` | o exporter responde, mas não conecta no banco |
| Erros 5xx na API | `generic.yml` | mais de 3 respostas 5xx por minuto, por 5 min |
| Pool do banco saturado | `generic.yml` | requisições esperando conexão do Hikari por 2 min |
| Falhas no Mercado Pago | `eletrolonghi.yml` | alguma chamada ao gateway sem `SUCCESS` nos últimos 10 min |
| Muitas tentativas de login inválidas | `eletrolonghi.yml` | mais de 20 logins com falha ou bloqueados em 10 min |
| Pagamentos recusados | `eletrolonghi.yml` | mais de 2 pagamentos em `REJECTED` na última hora |

Os alertas aparecem em **Alerting** e no painel "Alertas disparados" do dashboard Eletro Longhi, em qualquer ambiente. As regras são as mesmas em todos; o que muda é para onde a notificação vai.

### Discord (produção/staging)

A notificação no **Discord** é só para produção e staging, cada ambiente com o webhook do próprio canal (ex.: `#alertas-prod`, `#alertas-staging`). Basta preencher `GRAFANA_DISCORD_WEBHOOK_URL` no `.env` do servidor: o entrypoint do Grafana copia `discord/discord.yml` para o provisioning quando a variável existe. Sem a variável, o Discord fica desligado.

Em desenvolvimento a variável fica vazia (ou comentada): parar a API na IDE ou desligar o banco dispara "Alvo fora"/"Postgres fora", o que é rotina e não incidente, e mandar isso para um canal só gera ruído. Para testar uma regra nova ou a integração, preencha temporariamente com o webhook de um canal de teste.

Depois de preencher ou trocar o webhook, o Grafana precisa ser recriado para ler a variável: `docker compose --profile observability up -d grafana`.

Tempo até a mensagem chegar: a regra é avaliada a cada 1 min e precisa ficar no estado ruim pelo `for` dela (1 min no "Postgres fora" e no "Alvo fora"), e a política espera 30s (`group_wait`) para agrupar. Na prática, de 2 a 3 minutos depois de o problema começar. Quando o problema se resolve, chega uma mensagem de "Resolved".

## CI

O job `observability` do [`ci.yml`](../.github/workflows/ci.yml) valida o compose (com e sem Discord), as configs do Prometheus/Loki/Alloy, o provisionamento do Grafana (regras, dashboards, datasource, contact point) e faz uma prova ponta a ponta: um login do admin precisa aparecer no Loki e em `eletrolonghi_auth_logins_total` no Prometheus.

## O que ficou de fora

- **Traces distribuídos (Tempo):** o `traceId` nos logs já junta as linhas de uma requisição. Exportar spans (OTLP → Tempo) mostraria a duração de cada etapa (inclusive a chamada ao Mercado Pago) e ligaria log ↔ trace ↔ métrica.
- **Header `X-Trace-Id` na resposta:** o BCB devolve o traceId ao cliente para ligar um erro do frontend aos logs; aqui não foi trazido.
- **Métricas de containers (cAdvisor).**
- **Alta disponibilidade:** Prometheus, Loki e Grafana rodam com uma instância cada, com dados em volume local. Serve para desenvolvimento e demonstração, não para produção.
