# Exec Plans

Estrutura de rastreamento de execução de features, bugfixes e planejamentos.

---

## O que é um Exec Plan?

Um documento que descreve:
- **O quê:** Feature ou iniciativa sendo implementada
- **Por quê:** Contexto e justificativa
- **Como:** Fases e checklist de execução
- **Quando:** Timeline e owner

---

## Pastas

### `active/`

Planos em andamento. Convenção de nomeação:

```
YYYY-MM-DD-descricao-curta.md
```

Exemplos:
- `2026-07-14-add-pagination-devices.md`
- `2026-07-10-fix-device-mapper-null.md`
- `2026-07-08-implement-repair-status-filter.md`

### `completed/`

Planos finalizados (para referência histórica). Quando uma exec-plan fica pronta:

```bash
# Mova de active para completed
mv docs/exec-plans/active/2026-07-14-add-pagination-devices.md docs/exec-plans/completed/

# Adicione data de conclusão ao arquivo
echo "**Concluído:** 2026-07-20" >> docs/exec-plans/completed/2026-07-14-add-pagination-devices.md
```

---

## Criando Novo Exec Plan

### Rápido (manual)

```bash
cat > docs/exec-plans/active/$(date +%Y-%m-%d)-nome-feature.md <<EOF
# Exec Plan: Minha Feature

**Data:** $(date +%Y-%m-%d)
**Status:** 🟢 Active
**Owner:** [Seu Nome]

## Contexto
[Descrição do problema]

## Objetivos
- [ ] Obj 1
- [ ] Obj 2

## Fases
### Fase 1: Design
- [ ] Passo 1
- [ ] Passo 2

### Fase 2: Implementação
- [ ] Passo 1
- [ ] Passo 2

## Checklist Final
- [ ] Code reviewed
- [ ] Tests passing
- [ ] Docs updated

EOF
```

### Automático (script)

```bash
./scripts/new-exec-plan.sh "Nome da Feature"
```

---

## Template de Exec Plan

```markdown
# Exec Plan: [Título da Feature]

**Data de início:** YYYY-MM-DD  
**Status:** 🟢 Active | 🟡 On-Hold | 🟢 Completed  
**Owner:** [Seu Nome]  
**Reviewer:** [Se houver]

---

## Contexto

[Descrever problema/oportunidade que levou a esta iniciativa]

---

## Objetivos

- [ ] Objetivo 1
- [ ] Objetivo 2
- [ ] Objetivo 3

---

## Escopo

### In Scope
- [Feature/tarefa 1]
- [Feature/tarefa 2]

### Out of Scope
- [O que explicitamente NÃO será feito]

---

## Plano de Execução

### Fase 1: Design & Planning
- [ ] Diagramar arquitetura/fluxo
- [ ] Definir DTOs
- [ ] Revisar com time (se aplicável)
- [ ] Criar issues/sub-tasks

### Fase 2: Implementação
- [ ] Criar migrations (se BD)
- [ ] Implementar repository
- [ ] Implementar service
- [ ] Criar/atualizar mappers
- [ ] Criar controller
- [ ] Adicionar validações

### Fase 3: QA & Testing
- [ ] Testes unitários
- [ ] Testes de integração
- [ ] Testar via Swagger UI
- [ ] Testar casos edge

### Fase 4: Documentation & Deploy
- [ ] Documentar endpoint em QUICK_START_FOR_AGENTS.md (se relevante)
- [ ] Atualizar docs/product-specs/ (se spec mudar)
- [ ] Code review
- [ ] Merge para develop
- [ ] Deploy

---

## Recursos & Referências

- [`../../AGENTS.md`](../../AGENTS.md) — Convenções do projeto
- [`../STRUCTURE.md`](../STRUCTURE.md) — Estrutura de pastas
- [`../AGENT_PROMPTS.md`](../AGENT_PROMPTS.md) — Templates de prompts

---

## Notas & Learnings

[Registre decisões, blockers, descobertas enquanto avança]

**Exemplo:**
- Descobri que DeviceMapper tinha bug com null accessories → fixado em V2
- Adicionar paginação demanda mudança no Service (agora retorna Page<>)

---

## Blockers

[Listar qualquer coisa bloqueando progresso]

- [ ] Blocker 1: descrição
- [ ] Blocker 2: descrição

---

## Verificação Final

- [ ] Todos os objetivos atingidos
- [ ] Testes passam (`./mvnw test`)
- [ ] Código compila sem warnings (`./mvnw clean compile`)
- [ ] Documentação atualizada
- [ ] Migrations aplicadas
- [ ] Code review aprovado
- [ ] Feature funciona end-to-end
- [ ] Nenhum regressão identificada

---

## Próximas Ações

[Listar próximos passos após conclusão desta exec-plan]

---

**Concluído em:** YYYY-MM-DD (preencher ao finalizar)
```

---

## Boas Práticas

✅ **DO:**
- Commitar frequentemente enquanto trabalha
- Atualizar exec-plan com notas de progresso
- Mover para `completed/` quando finalizar

❌ **DON'T:**
- Deixar plano em `active/` por meses (arquivar em `completed/` ou deletar)
- Copiar-colar templates sem adaptar ao contexto real
- Ignorar blockers — escrever explicitamente

---

## Exemplo de Fluxo Real

```
2026-07-14: Crio novo exec-plan
2026-07-14: Design approved
2026-07-15: Implemento service + repository
2026-07-16: Crio controller + mapper
2026-07-17: Testes + bugfixes
2026-07-18: Code review + mais correções
2026-07-19: Final checks, movo para completed/
```

---

## Integração com Agentes/LLMs

Use exec-plans para rastrear trabalho com agentes:

1. Crie exec-plan com contexto claro
2. Compartilhe com agente
3. Agente refencia exec-plan durante implementação
4. Atualiza exec-plan com progresso
5. Ao finalizar, registre learnings

**Exemplo de prompt para agente:**
```
Veja exec-plan em docs/exec-plans/active/2026-07-14-add-pagination.md
Implemente os objetivos conforme Fase 2: Implementação.
Use referências em ../AGENTS.md para convenções.
Registre progresso e blockers no exec-plan ao terminar.
```


