# NFS-e — análise de necessidade

> Análise de outubro/2026 para a Eletro Longhi, enquadrada como **MEI**. Não substitui a orientação de um contador: confirme a regra vigente no município da loja.

## Resumo

**Hoje não há necessidade de integrar a emissão de NFS-e à API.** O comprovante gerado pelo sistema (`GET /payment/{id}/receipt`) é **não-fiscal** e continua sendo o documento entregue ao cliente. Quando a nota for exigida (casos abaixo), ela é emitida manualmente no **Emissor Nacional** (portal ou app gov.br), sem custo.

## Quando o MEI precisa emitir NFS-e

| Tomador do serviço | Obrigação |
|---|---|
| Pessoa jurídica (CNPJ) | **Obrigatória** |
| Pessoa física (consumidor final) | **Dispensada**, mas passa a ser **obrigatória se o cliente pedir** (CDC) |

- Desde 09/2023 o MEI emite NFS-e exclusivamente pelo **Emissor Nacional** (padrão nacional), não mais pelo sistema da prefeitura.
- A LC 214/2025 (reforma tributária) tornou o **padrão nacional de NFS-e** obrigatório para os municípios a partir de 2026; isso muda o *formato* da nota, não a dispensa do MEI para pessoa física.
- Peças vendidas separadamente são mercadoria (ICMS → NF-e/NFC-e), não serviço. Em conserto com troca de peça cobrado como serviço, vale confirmar com o contador como discriminar.

Como a clientela da assistência é quase toda pessoa física, o volume de notas obrigatórias é baixo: emissão manual sob demanda resolve.

## Quando reavaliar

- **Desenquadramento do MEI** (faturamento acima do teto → ME no Simples Nacional): a emissão passa a ser obrigatória em **todo** serviço, inclusive para pessoa física. Aí a integração se paga.
- Volume relevante de clientes PJ (empresas, condomínios, revendas) ou de pedidos de nota.

## Caminhos de integração (para quando for necessário)

| Opção | Como funciona | Custo | Esforço |
|---|---|---|---|
| **API do Sistema Nacional NFS-e** | A API monta a DPS (XML), assina com **certificado digital A1** e envia por mTLS; recebe a NFS-e e o DANFSe | Gratuito (só o certificado A1, ~R$ 150–250/ano) | Alto: assinatura XML, ambiente de homologação, tratamento de rejeições |
| **Provedor de notas** (Focus NFe, NFE.io, PlugNotas…) | API REST/JSON; o provedor cuida de XML, assinatura e particularidades | Mensalidade e/ou valor por nota | Baixo: um `client/` como o do Mercado Pago, com sandbox |

Para um volume pequeno, o **provedor** tende a ser a melhor relação custo/esforço; a API nacional direta só compensa com volume alto ou como exercício técnico.

### Esboço da integração (seguindo os padrões do projeto)

- `client/NfseClient` (via `RestClient.Builder` injetado, para manter as métricas `http_client_requests_seconds`), configurado por `nfse.*` / `.env`; sem credenciais, fica inativo (como o `MercadoPagoClient`).
- Nova migration com `nfse_number`, `nfse_access_key`, `nfse_status` e `nfse_issued_at` em `payments` (a nota é emitida sobre um pagamento aprovado).
- `POST /payment/{id}/nfse` (A G T), permitido só para pagamento `APPROVED`; falha no provedor → 502, como o `PaymentGatewayException`.
- O comprovante passa a mostrar o número/chave da NFS-e quando existir, e o envio por e-mail pode anexar o DANFSe.

## Referências

- [Trivium Contábil — Nota fiscal MEI em 2026](https://triviumcontabil.com.br/nota-fiscal-mei/)
- [Razonet — Nota fiscal MEI 2026: quando é obrigatória](https://razonet.com.br/contabilidade-digital/nota-fiscal-mei-em-2026-quando-e-obrigatoria-e-como-emitir)
- [Exemplo municipal: Decreto 12.656/2025 de Santa Cruz do Sul (dispensa para pessoa física, salvo pedido)](https://www.santacruz.rs.gov.br/decretos/docs/DECRETOS_2025/DECRETO_12656_regulamenta_emissao_NFS-e_padrao_nacional.pdf)
- Emissor Nacional: <https://www.nfse.gov.br/EmissorNacional>
