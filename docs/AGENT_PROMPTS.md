# AGENT_PROMPTS

Templates de prompts e instruções para trabalhar com agentes/modelos de IA.

---

## Template 1: Revisar e corrigir código

```
System prompt:
Você é um code reviewer experiente em Java 21 + Spring Boot 3.
Foque em: segurança, padrões do projeto, testes e documentação.

User prompt:
Revisar o seguinte código [ARQUIVO/MUDANÇAS]:

[COLE O CÓDIGO OU DIFF AQUI]

Verifique:
- Conformidade com AGENTS.md (DTOs records, MapStruct, repositories)
- Mappers injetados e usados corretamente
- Validações e tratamento de erros conforme ApplicationControllerAdvice
- Endpoints refletem convenção (ex: /api/recurso GET/POST/PUT/DELETE)
- Migrações BD (se houver) em src/main/resources/db/migration

Retorne um sumário: Issues encontradas | Sugestões | ✅ Approved
```

---

## Template 2: Gerar migração BD

```
System prompt:
Você é especialista em Flyway migrations e PostgreSQL.
O projeto usa numeração sequencial: V1.sql, V2.sql, etc.
Atual: V7__create_table_users.sql

User prompt:
Gere a próxima migração para [DESCRIÇÃO DA MUDANÇA].

Requisitos:
- Numeração: V8__[descricao].sql
- Sintaxe PostgreSQL
- Sem DROP (use ALTER/ADD para compatibilidade)
- Inclua constraints (FK, unique, NOT NULL) conforme necessário

Retorne SQL pronto para src/main/resources/db/migration/
```

---

## Template 3: Adicionar novo endpoint

```
System prompt:
Você é um desenvolvedor Spring Boot experiente.
Siga o padrão: Controller → Service → Repository.

User prompt:
Crie um novo endpoint para [DESCRIÇÃO]:

Requisitos:
- HTTP Method: [GET/POST/PUT/DELETE]
- Path: /api/[recurso]
- Request DTO: [se houver]
- Response DTO: [se houver]

Passos esperados:
1. Criar/atualizar request record em controller/request/
2. Criar/atualizar response record em controller/response/
3. Criar/atualizar mapper (MapStruct) em mapper/
4. Criar/atualizar service em service/
5. Criar/atualizar repository em repository/
6. Criar/atualizar controller em controller/

Forneça código para cada camada, seguindo convenções do projeto.
```

---

## Template 4: Gerar documentação de feature

```
System prompt:
Você é um técnico de documentação experiente em Java/Spring.

User prompt:
Resuma a seguinte feature em um markdown para docs/product-specs/:

Feature: [NOME]
Objetivo: [DESCRIÇÃO BREVE]
Endpoints:
- [GET /api/endpoint]
- [POST /api/endpoint]

Gere um arquivo markdown com:
- Descrição clara
- Fluxo de dados (request → service → response)
- Casos de uso
- Notas para QA e operações
```

---

## Template 5: Analizar e propor refatoração

```
System prompt:
Você é um arquiteto de software especialista em Spring Boot.

User prompt:
Analise a camada de service/repository para oportunidades de refatoração:

Contexto:
- Arquivo: [CAMINHO]
- Problema: [O QUE ESTÁ COMPLICADO?]

Proponha:
- Padrão design (Strategy, Factory, etc)
- Separação de responsabilidades
- Testes adicionais recomendados
- Código de exemplo refatorado

Use convenções do projeto (Records, MapStruct, Optional).
```

---

## Template 6: Escrever testes

```
System prompt:
Você é especialista em testes JUnit 5 + Mockito.

User prompt:
Escreva testes para: [CLASSE/MÉTODO]

Cobertura esperada:
- Caminho feliz (happy path)
- Casos de erro (null, exceções)
- Validações (anotações @NotNull, @NotBlank)
- Interação com repositório/mapeador

Use mocks e asserts claros. Retorne código pronto para src/test/java/
```

---

## Dicas práticas para usar com agentes

1. **Contexto:** Cole AGENTS.md ou ARCHITECTURE.md no prompt para manter agente sincronizado.
2. **Iteração:** Se a resposta ficar genérica, realimente com: "Aplique isto especificamente ao projeto [recurso]"
3. **Validação:** Sempre rode `./mvnw compile` e `./mvnw test` depois.
4. **Versão:** Atualize este arquivo ao adicionar novas convenções ou patterns ao projeto.

---

## Referências rápidas

- **Projeto:** Java 21, Spring Boot 3, PostgreSQL 16, Flyway, JWT, MapStruct
- **Camadas:** controller → service → repository
- **DTOs:** records (imutáveis, anotadas com `@Valid`)
- **Security:** JWT em config/TokenService, SecurityFilter, SecurityConfig
- **Docs:** AGENTS.md, ARCHITECTURE.md, docs/


