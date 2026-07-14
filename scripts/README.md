# Scripts Auxiliares

Utilitários para acelerar desenvolvimento e documentação.

---

## `new-exec-plan.sh`

Script para criar novo exec-plan com template automático.

### Uso

```bash
./scripts/new-exec-plan.sh "Nome da Feature"
```

### Exemplo

```bash
./scripts/new-exec-plan.sh "Adicionar paginação de devices"
```

Isto vai gerar um arquivo em `docs/exec-plans/active/YYYY-MM-DD-adicionar-paginacao-de-devices.md` com template pronto para preencher.

### O que o script faz

- Cria arquivo com data e nome sanitizado
- Preenche template com seções padrão (Contexto, Objetivos, Fases, Checklist)
- Imprime instruções de próximos passos

### Próximas adições

- Script para gerar migrations (`new-migration.sh`)
- Script para gerar DTOs/Mappers boilerplate


