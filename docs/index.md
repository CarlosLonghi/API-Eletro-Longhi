# Documentação — Índice Central

Bem-vindo! Este é o índice de toda a documentação do projeto. Escolha seu ponto de partida.

---

## 🎯 Comece por aqui

### ✨ Novo no projeto?

1. **5 min:** [`../ARCHITECTURE.md`](../ARCHITECTURE.md) — Resumo de arquitetura
2. **15 min:** [`QUICK_START_FOR_AGENTS.md`](QUICK_START_FOR_AGENTS.md) — Essencial para agentes/LLMs
3. **20 min:** [`FIRST_TIME_SETUP.md`](FIRST_TIME_SETUP.md) — Setup local com Docker

### 🚀 Pronto para codar?

1. [`STRUCTURE.md`](STRUCTURE.md) — Entenda a organização de pastas
2. [`GIT_WORKFLOW.md`](GIT_WORKFLOW.md) — Convenções de commits e branches
3. [`../AGENTS.md`](../AGENTS.md) — Checklist de convenções

### 🆘 Preso?

→ Consulte [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md) (soluções para problemas comuns)

---

## 📚 Documentação Completa

### Core Docs

| Arquivo | Propósito | Leitura |
|---------|-----------|---------|
| [`../AGENTS.md`](../AGENTS.md) | **Obrigatório:** Onboarding completo para agentes/IA | 20 min |
| [`../ARCHITECTURE.md`](../ARCHITECTURE.md) | Resumo de arquitetura (rápido reference) | 5 min |
| [`QUICK_START_FOR_AGENTS.md`](QUICK_START_FOR_AGENTS.md) | Folha de referência super compacta | 15 min |

### Setup & Troubleshooting

| Arquivo | Propósito | Leitura |
|---------|-----------|---------|
| [`FIRST_TIME_SETUP.md`](FIRST_TIME_SETUP.md) | Setup passo-a-passo (Docker ou local) | 20 min |
| [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md) | Resolução de problemas comuns | Reference |
| [`GIT_WORKFLOW.md`](GIT_WORKFLOW.md) | Convenções de commits, branches, PRs | 10 min |

### Development Guides

| Arquivo | Propósito | Leitura |
|---------|-----------|---------|
| [`STRUCTURE.md`](STRUCTURE.md) | Mapa detalhado de diretórios e responsabilidades | 15 min |
| [`AGENT_PROMPTS.md`](AGENT_PROMPTS.md) | Templates de prompts para LLMs (code review, migrations, etc) | Reference |
| [`DESIGN.md`](DESIGN.md) | Decisões de design importantes | Reference |
| [`SECURITY.md`](SECURITY.md) | Política de segurança (JWT, auth, etc) | 10 min |
| [`RELIABILITY.md`](RELIABILITY.md) | Operações, backup, disaster recovery | Reference |

### Organization & Planning

| Arquivo | Propósito | Conteúdo |
|---------|-----------|---------|
| [`PLANS.md`](PLANS.md) | Roadmap e planos de alto nível | A preencher |
| [`PRODUCT_SENSE.md`](PRODUCT_SENSE.md) | Notas de produto e prioridades | A preencher |
| [`QUALITY_SCORE.md`](QUALITY_SCORE.md) | Critérios de qualidade e checkpoints | A preencher |
| [`FRONTEND.md`](FRONTEND.md) | Notas para integração front-end | A preencher |

### Subdirectories

| Pasta | Propósito | Conteúdo |
|-------|-----------|---------|
| [`design-docs/`](design-docs/) | Documentação de design detalhada | index.md, core-beliefs.md |
| [`exec-plans/`](exec-plans/) | Planos de execução (in-progress e concluídos) | active/, completed/, README.md |
| [`generated/`](generated/) | Documentação auto-gerada (ex: DB schema) | db-schema.md |
| [`product-specs/`](product-specs/) | Especificações de features e onboarding | index.md, specs por feature |
| [`references/`](references/) | Referências e templates para LLMs | design-system-reference-llms.txt, etc |

---

## 🤖 Para Agentes/LLMs

**Se você é um agente de IA/modelo de linguagem:**

1. Leia [`../AGENTS.md`](../AGENTS.md) **primeiro** (entender convenções)
2. Use [`QUICK_START_FOR_AGENTS.md`](QUICK_START_FOR_AGENTS.md) como folha de referência
3. Quando codificar, consulte [`STRUCTURE.md`](STRUCTURE.md) para orientação de arquivos
4. Para tarefas específicas, use templates em [`AGENT_PROMPTS.md`](AGENT_PROMPTS.md)
5. Se tiver erro, vá para [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md)

**Dica:** Você pode usar qualquer template de `AGENT_PROMPTS.md` como system prompt ao conversar comigo — eles foram projetados para manter o agente alinhado com convenções do projeto.

---

## 👨‍💻 Para Desenvolvedores

**Setup inicial:**

```bash
# 1. Faça setup
cat docs/FIRST_TIME_SETUP.md

# 2. Entenda a estrutura
cat docs/STRUCTURE.md

# 3. Configure seu editor (vs code / IntelliJ)
# dica: adicione `target/generated-sources/annotations` como source folder

# 4. Crie sua primeiro feature branch
git checkout -b feature/seu-nome
# (veja GIT_WORKFLOW.md para convenções)
```

**Durante desenvolvimento:**

- Commit messages: veja [`GIT_WORKFLOW.md`](GIT_WORKFLOW.md)
- Adicionar feature? Veja [`STRUCTURE.md`](STRUCTURE.md) (qual camada mexer)
- Erro? Veja [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md)
- Segurança? Veja [`SECURITY.md`](SECURITY.md)

---

## 🔄 Fluxo Recomendado por Tarefa

### Implementar nova feature

```
STRUCTURE.md (entender arquitetura)
    ↓
AGENT_PROMPTS.md (Template 3: Adicionar novo endpoint)
    ↓
../AGENTS.md (recordar convenções de mappers, DTOs)
    ↓
Implementar (controller → mapper → service → repository)
    ↓
GIT_WORKFLOW.md (commitar e fazer PR)
```

### Corrigir bug

```
TROUBLESHOOTING.md (diagnosticar)
    ↓
STRUCTURE.md (localizar arquivo relevante)
    ↓
Debugar + corrigir
    ↓
GIT_WORKFLOW.md (commit [FIX])
```

### Gerar migração BD

```
AGENT_PROMPTS.md (Template 2: Gerar migração BD)
    ↓
../AGENTS.md (verificar convenção Flyway)
    ↓
Criar arquivo em src/main/resources/db/migration/
    ↓
Testar com ./mvnw spring-boot:run
```

### Revisar código

```
AGENT_PROMPTS.md (Template 1: Revisar e corrigir código)
    ↓
../AGENTS.md (checklist de convenções)
    ↓
Revisar & comentar
```

---

## 📞 Perguntas Frequentes (FAQ)

**P: Por onde começar?**  
R: Se é novo no projeto, leia `QUICK_START_FOR_AGENTS.md` (15 min). Se é dev fazendo setup, leia `FIRST_TIME_SETUP.md`.

**P: Qual arquivo documenta a estrutura de pastas?**  
R: `STRUCTURE.md` — contém mapa completo com responsabilidades de cada camada.

**P: Como adiciono um novo endpoint?**  
R: Veja `AGENT_PROMPTS.md` → "Template 3: Adicionar novo endpoint".

**P: Como comito meu trabalho?**  
R: Veja `GIT_WORKFLOW.md` para convenções de branches e mensagens de commit.

**P: O projeto está com erro/problema. Onde procuro?**  
R: Veja `TROUBLESHOOTING.md` — lista problemas comuns e soluções rápidas.

**P: Como rodo o projeto pela primeira vez?**  
R: Veja `FIRST_TIME_SETUP.md` — passo-a-passo com Docker ou local.

**P: Estou preso com MapStruct/Mapper?**  
R: Veja `TROUBLESHOOTING.md` → "Build & Compile" → "Cannot find symbol: DeviceMapper".

---

## 🏗️ Estrutura Rápida

```
docs/
├── index.md                   ← Você está aqui
├── QUICK_START_FOR_AGENTS.md  ← Leia isto (15 min)
├── FIRST_TIME_SETUP.md        ← Setup passo-a-passo
├── STRUCTURE.md               ← Mapa de diretórios
├── TROUBLESHOOTING.md         ← Problemas & soluções
├── GIT_WORKFLOW.md            ← Commits & branches
├── AGENT_PROMPTS.md           ← Templates de prompts
├── DESIGN.md, SECURITY.md, RELIABILITY.md, etc. ← Reference
├── design-docs/               ← Design detalhado
├── exec-plans/                ← Planos em execução
├── generated/                 ← Documentação auto-gerada
├── product-specs/             ← Specs de features
└── references/                ← Referências para LLMs
```

---

## ⚡ Quick Links

- **Setup?** → [`FIRST_TIME_SETUP.md`](FIRST_TIME_SETUP.md)
- **Novo no projeto?** → [`QUICK_START_FOR_AGENTS.md`](QUICK_START_FOR_AGENTS.md)
- **Entender código?** → [`STRUCTURE.md`](STRUCTURE.md)
- **Algo quebrou?** → [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md)
- **Commitar código?** → [`GIT_WORKFLOW.md`](GIT_WORKFLOW.md)
- **Usar com LLM?** → [`AGENT_PROMPTS.md`](AGENT_PROMPTS.md)
- **Segurança?** → [`SECURITY.md`](SECURITY.md)
- **Convenções?** → [`../AGENTS.md`](../AGENTS.md)

---

## 🎓 Últimas dicas

1. **Bookmark este índice** (`docs/index.md`) — é seu ponto de referência central.
2. **Ler em ordem:** ARCHITECTURE → QUICK_START → STRUCTURE (primeiro dia).
3. **Manter reference aberta:** GIT_WORKFLOW, TROUBLESHOOTING durante codificação.
4. **Usar prompts:** `AGENT_PROMPTS.md` tem templates prontos para tarefas comuns.
5. **Atualizar docs:** Quando descobrir algo novo, atualize um arquivo apropriado (ex: TROUBLESHOOTING).

---

**Última atualização:** 2026-07-14

Dúvidas? Consulte o arquivo relevante acima ou crie uma issue no GitHub. Bom desenvolvimento! 🚀


