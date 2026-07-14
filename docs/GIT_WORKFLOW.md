# GIT_WORKFLOW

Convenções e guia de trabalho com Git para este projeto.

---

## Estrutura de Branches

```
main                  # Produção (estável, code-reviewed)
  └── develop        # Branch de desenvolvimento integração
      ├── feature/*  # Features em desenvolvimento
      ├── bugfix/*   # Correções rápidas
      └── docs/*     # Melhorias de documentação
```

---

## Naming Conventions

### Feature Branch

```
feature/JIRA-123-descricao-curta
feature/add-device-pagination
feature/implement-jwt-refresh-token
```

### Bugfix Branch

```
bugfix/fix-null-pointer-device-mapper
bugfix/JIRA-456-403-on-list-endpoint
```

### Docs Branch

```
docs/update-architecture
docs/add-troubleshooting-guide
```

### Hotfix (apenas main → produção)

```
hotfix/critical-security-patch
```

---

## Fluxo de Commits

### Mensagem de Commit

Use o formato:

```
[TYPE] Short description (up to 50 chars)

Longer explanation if needed (wrap at 72 chars).

- Bullet points explaining what/why
- Use present tense ("Add feature" not "Added feature")

Closes #ISSUE_ID (if applicable)
```

### Tipos de Commit

```
[FEAT]     - Nova feature
[FIX]      - Bugfix
[REFACTOR] - Refatoração (sem mudar funcionalidade)
[TEST]     - Adicionar/atualizar testes
[DOCS]     - Mudanças em documentação
[CHORE]    - Build, deps, etc (não afeta código produção)
[PERF]     - Melhoria de performance
```

### Exemplos

```
[FEAT] Add pagination to device listing

- Implement page/size query parameters in DeviceController
- Update DeviceService to use Spring Data Page interface
- Modify DeviceResponse DTOs to include pagination metadata
- Add unit tests for pagination logic

Closes #123
```

```
[FIX] Fix null pointer exception in DeviceMapper

When accessory list is empty, mapper now returns empty list instead of null.

Closes #456
```

```
[DOCS] Update TROUBLESHOOTING guide with MapStruct resolution

Added section on "Cannot find symbol: DeviceMapper" error.
```

---

## Fluxo de Work

### 1. Começar uma Feature

```bash
# Atualize main/develop
git checkout develop
git pull origin develop

# Crie feature branch
git checkout -b feature/add-repair-status-filter

# Faça seu trabalho
# ...commits locais com [FEAT], [FIX], [DOCS]...

# Rebase antes de PR
git fetch origin
git rebase origin/develop

# Push
git push origin feature/add-repair-status-filter
```

### 2. Criar Pull Request

**Título:** Siga o mesmo padrão do commit
```
[FEAT] Add repair status filter to listing endpoints
```

**Descrição:** Use template:
```markdown
## Descrição
Breve explicação do que foi implementado.

## Tipo de Mudança
- [ ] Nova feature
- [x] Bugfix
- [ ] Refatoração
- [ ] Documentação

## Checklist
- [x] Código segue convenções do projeto (AGENTS.md)
- [x] MapStruct recompilado (`./mvnw compile`)
- [x] Testes passam (`./mvnw test`)
- [x] Sem conflitos com `develop`
- [ ] Documentação atualizada (se necessário)

## Testes Realizados
- Testei localmente via `./mvnw spring-boot:run`
- Endpoint responde corretamente com status = "PENDING"
- 403 continua sendo retornado para usuários não autenticados

## Screenshots (se aplicável)
[Cole screenshot do Swagger UI ou curl response]

Closes #123
```

### 3. Code Review

**Revisor:** Verifique:
- [ ] Segue AGENTS.md e convenções
- [ ] DTOs são records
- [ ] Mappers usam MapStruct com `componentModel = "spring"`
- [ ] Service retorna `Optional` para single, `List` para coleções
- [ ] Validações (`@Valid`, `@NotNull`) onde necessário
- [ ] Sem secrets ou credenciais no código
- [ ] Tests coverage adequada

### 4. Merge & Clean up

```bash
# Após aprovação, faça merge em develop
git checkout develop
git pull origin develop
git merge --no-ff feature/add-repair-status-filter

# Delete local branch
git branch -d feature/add-repair-status-filter

# Delete remote branch
git push origin --delete feature/add-repair-status-filter

# Push merged develop
git push origin develop
```

### 5. Integração para Produção (main)

```bash
# Apenas quando pronto para deploy
git checkout main
git pull origin main
git merge --no-ff develop

# Tag de release (opcional)
git tag -a v1.2.0 -m "Release 1.2.0"

# Push
git push origin main
git push origin --tags
```

---

## Boas Práticas

### ✅ DO

- Commit frequentemente (com mensagens claras)
- Rebase antes de PR (mantenha histórico limpo)
- Descrever **por quê** na mensagem de commit, não só **o quê**
- Executar `./mvnw compile && ./mvnw test` antes de fazer push
- Revisar seu próprio código antes de pedir review (diff check)
- Atualizar documentação (`docs/`) se mudou comportamento

### ❌ DON'T

- Fazer commits muito grandes (divida em pedaços lógicos)
- Forçar push (`git push -f`) em branches compartilhadas
- Deixar TODO comments sem abrir issue
- Commitar `target/` ou `.idea/`
- Mudar branch sem fazer commit (use `git stash` se necessário)
- Fazer merge direto sem PR (sempre use PR para rastreabilidade)

---

## Gitignore

Certifique-se de que `.gitignore` contém:

```
# Maven
target/
.mvn/

# IDE
.idea/
*.iml
*.swp
*.swo
.vscode/

# OS
.DS_Store
Thumbs.db

# Env
.env
.env.local
application-local.properties
```

---

## Troubleshooting Git

### Problema: "Branch ahead of 'origin/develop'"

**Solução:** Você tem commits locais não pusheados. Verifique e faça push:

```bash
git log origin/develop..HEAD  # Veja o que vai ser pusheado
git push origin feature/seu-branch
```

### Problema: "Your branch has diverged"

**Solução:** Rebase ao invés de merge:

```bash
git fetch origin
git rebase origin/develop
git push origin feature/seu-branch -f  # força push (ok em feature branch)
```

### Problema: "Cannot delete branch (not fully merged)"

**Solução:** Força delete (se tiver certeza):

```bash
git branch -D feature/seu-branch
git push origin --delete feature/seu-branch
```

### Problema: "Accidentally committed to main"

**Solução:** Mova commits para nova branch:

```bash
git branch feature/novo-branch
git reset --hard origin/main
git checkout feature/novo-branch
```

---

## CI/CD Integration (Futura)

Quando tiver CI/CD (ex: GitHub Actions), adicione checks:

```yaml
# .github/workflows/test.yml
on: [pull_request, push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Compile
        run: ./mvnw -q compile
      - name: Run tests
        run: ./mvnw test
      - name: Check formatting
        run: ./mvnw fmt:check
```

---

## Labels para Issues & PRs

Recomendado usar:

```
🟢 ready          # Pronto para merge
🟡 in-progress    # Trabalho em andamento
🔴 blocked        # Bloqueado/esperando review
📝 documentation  # Mudanças apenas em docs
🐛 bug            # Correção de bug
✨ enhancement    # Nova feature
```

---

## Changelog (Opcional)

Mantenha um `CHANGELOG.md` (exemplo):

```markdown
# Changelog

## [1.2.0] - 2026-03-15
### Added
- Device pagination support
- Repair status filter

### Fixed
- Null pointer in DeviceMapper when accessories empty
- 403 error message formatting

## [1.1.0] - 2026-02-01
...
```

---

## Quickstart para Agentes/Novos Devs

```bash
# Clone
git clone <repo-url>
cd API-Eletro-Longhi

# Setup
git checkout develop

# Feature nova
git checkout -b feature/seu-nome

# Após trabalho
git commit -m "[FEAT] Sua descrição aqui"
git push origin feature/seu-nome

# Abra PR no GitHub
# Aguarde review + merge
```


