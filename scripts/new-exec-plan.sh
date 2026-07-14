#!/bin/bash

# Script para criar novo exec-plan com template
# Uso: ./scripts/new-exec-plan.sh "Nome da Feature"

if [ -z "$1" ]; then
    echo "Uso: ./scripts/new-exec-plan.sh 'Nome da Feature'"
    echo ""
    echo "Exemplo:"
    echo "  ./scripts/new-exec-plan.sh 'Adicionar paginação de devices'"
    exit 1
fi

FEATURE_NAME="$1"
DATE=$(date +%Y-%m-%d)
SAFE_NAME=$(echo "$FEATURE_NAME" | tr ' ' '-' | tr '[:upper:]' '[:lower:]')
FILENAME="docs/exec-plans/active/${DATE}-${SAFE_NAME}.md"

# Cria arquivo com template
cat > "$FILENAME" <<EOF
# Exec Plan: $FEATURE_NAME

**Data de início:** $DATE
**Status:** 🟢 Active
**Owner:** [seu nome]

---

## Contexto

[Descreva o problema/oportunidade]

---

## Objetivos

- [ ] Objetivo 1
- [ ] Objetivo 2
- [ ] Objetivo 3

---

## Escopo

### In Scope
- [Feature/Tarefa 1]
- [Feature/Tarefa 2]

### Out of Scope
- [O que NÃO faz parte]

---

## Plano de Execução

### Fase 1: Design
- [ ] Diagramar fluxo de dados
- [ ] Definir DTOs/requests/responses
- [ ] Revisar com time (se houver)

### Fase 2: Implementação
- [ ] Criar migrations (se necessário)
- [ ] Implementar repository
- [ ] Implementar service
- [ ] Criar/atualizar mappers
- [ ] Criar controller
- [ ] Adicionar validações

### Fase 3: QA & Deploy
- [ ] Testes unitários
- [ ] Testes de integração
- [ ] Documentação atualizada
- [ ] Deploy

---

## Recursos & Referências

- AGENTS.md (convenções do projeto)
- ARCHITECTURE.md (visão geral)
- docs/AGENT_PROMPTS.md (templates de prompts para agentes)

---

## Notas & Learnings

[Registre decisões, blockers, descobertas enquanto avança]

---

## Checklist Final

- [ ] Código revisado
- [ ] Testes passando
- [ ] Documentação atualizada (README, specs, etc)
- [ ] Migrations aplicadas
- [ ] Feature funciona end-to-end

---

**Próximas ações:**
[Listar próximos passos ou bloqueadores]

EOF

echo "✅ Novo exec-plan criado: $FILENAME"
echo ""
echo "Próximos passos:"
echo "1. Abra o arquivo: $FILENAME"
echo "2. Preencha os detalhes"
echo "3. Ao finalizar, mova para docs/exec-plans/completed/"
echo ""
echo "Dica: Use docs/AGENT_PROMPTS.md para pedir ajuda a agentes/modelos de IA"

