# QUICK_START_FOR_AGENTS

**Você é um agente de IA?** Comece aqui. Este é o arquivo mais importante para onboarding rápido.

---

## 📋 5 minutos: Essencial

1. **Linguagem & Framework:** Java 21 + Spring Boot 3 (PostgreSQL, JWT, Flyway)
2. **Estrutura:** `br.com.carloslonghi.eletrolonghi` com pacotes: config, controller, service, repository, mapper, entity
3. **Padrão:** HTTP Controller → Service (lógica) → Repository (BD) → Entity
4. **DTOs:** Records imutáveis (`controller/request/*`, `controller/response/*`)
5. **Mappers:** MapStruct (`mapper/*`) com `@Mapper(componentModel = "spring")` — injetar em controllers/services
6. **Security:** JWT em `config/TokenService`, aplicado via `SecurityFilter`, configurado em `SecurityConfig`
7. **BD:** Migrations Flyway em `src/main/resources/db/migration` (V1..V7)
8. **Docs:** OpenAPI/Swagger em `/swagger-ui/index.html`

---

## 🚀 Primeiros 15 minutos

### Leia nesta ordem

1. `AGENTS.md` — Checklist completo e convenções do projeto
2. `ARCHITECTURE.md` — Mapa de arquitetura e decisões
3. `docs/AGENT_PROMPTS.md` — Templates de prompts para tarefas comuns

### Estrutura de pastas

```
src/main/java/br/com/carloslonghi/eletrolonghi/
├── config/          # Security, Token, Swagger, Error Handling
├── controller/      # REST endpoints (accept DTOs records)
│   ├── request/     # Request DTOs
│   └── response/    # Response DTOs
├── service/         # Lógica de negócio
├── repository/      # Spring Data JPA
├── mapper/          # MapStruct (DTOs ↔ Entities)
└── entity/          # Entidades JPA + Enums
```

---

## 🎯 Tarefas Comuns & Templates

### ✅ Adicionar novo endpoint

**Passos:**
1. Criar Request record em `controller/request/`
2. Criar Response record em `controller/response/`
3. Atualizar mapper em `mapper/` (MapStruct)
4. Implementar service em `service/`
5. Implementar/atualizar repository em `repository/`
6. Criar/atualizar controller em `controller/`

**Template de prompt:** Veja `docs/AGENT_PROMPTS.md` → "Template 3: Adicionar novo endpoint"

### ✅ Gerar migração BD

**Arquivo:** `src/main/resources/db/migration/`  
**Numeração:** V8__, V9__, etc. (sequencial)  
**SQL:** PostgreSQL, sem DROP (use ALTER para compatibilidade)

**Template de prompt:** Veja `docs/AGENT_PROMPTS.md` → "Template 2: Gerar migração BD"

### ✅ Revisar código

**Verifique:**
- Conformidade com AGENTS.md (records, MapStruct, repositories)
- Validações (@Valid, @NotNull, @NotBlank)
- Tratamento de erros via ApplicationControllerAdvice
- Endpoints seguem padrão /api/recurso GET|POST|PUT|DELETE

**Template de prompt:** Veja `docs/AGENT_PROMPTS.md` → "Template 1: Revisar e corrigir código"

---

## 🔑 Convenções Obrigatórias

### DTOs & Validação

```java
// ✅ CERTO: Record imutável com validações
public record DeviceRequest(
    @NotBlank(message = "Name is required")
    String name,
    @NotNull(message = "Brand ID is required")
    Long brandId
) {}
```

### Mappers

```java
// ✅ CERTO: MapStruct com componentModel = "spring"
@Mapper(componentModel = "spring")
public interface DeviceMapper {
    DeviceEntity toEntity(DeviceRequest request);
    DeviceResponse toResponse(DeviceEntity entity);
}

// Em controller/service:
@Autowired
private DeviceMapper deviceMapper;

// Uso:
DeviceEntity entity = deviceMapper.toEntity(request);
DeviceResponse response = deviceMapper.toResponse(entity);
```

### Service & Repository

```java
// ✅ CERTO: Services retornam Optional para single, List para coleções
public Optional<DeviceEntity> findById(Long id);
public List<DeviceEntity> findAll();
public List<DeviceEntity> findDevicesByBrandId(Long brandId);

// Repository: use Spring Data naming conventions
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
    List<DeviceEntity> findDevicesByBrandId(Long brandId);
}
```

### Controller

```java
// ✅ CERTO: Injetar mapper, usar @Valid, retornar ResponseEntity
@RestController
@RequestMapping("/api/device")
public class DeviceController {
    
    @Autowired
    private DeviceService service;
    
    @Autowired
    private DeviceMapper mapper;
    
    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceRequest request) {
        DeviceEntity entity = mapper.toEntity(request);
        DeviceEntity saved = service.save(entity);
        return ResponseEntity.status(201).body(mapper.toResponse(saved));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getById(@PathVariable Long id) {
        return service.findById(id)
            .map(e -> mapper.toResponse(e))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

---

## 🛠️ Ambiente & Build

### Setup Local

```bash
# 1. Crie BD vazio
# CREATE DATABASE eletro_longhi;

# 2. Configure application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/eletro_longhi
spring.security.secret=seu-segredo-jwt-aqui

# 3. Rode
./mvnw spring-boot:run

# API estará em http://localhost:8080
# Swagger em http://localhost:8080/swagger-ui/index.html
```

### Comandos Maven

```bash
./mvnw compile              # Compila (gera MapStruct implementations)
./mvnw test                 # Roda testes
./mvnw spring-boot:run      # Inicia app
./mvnw -q -DskipTests package # Build JAR
```

### Docker (apenas BD)

```bash
docker compose up -d        # Inicia PostgreSQL (sem app containerizado)
```

---

## 🔐 Security Cheat Sheet

**JWT:** Gerado em `config/TokenService.generateToken(userId, userName)`  
**Verificação:** `config/TokenService.verifyToken(token)`  
**Aplicação:** `config/SecurityFilter` extrai token e inicia contexto  
**Config:** `config/SecurityConfig` define regras (ex: `/auth/register` e `/auth/login` são públicos)  

**Claims do JWT:** `userId`, `userName`  
**Header esperado:** `Authorization: Bearer <token>`

---

## 📚 Onde Procurar

| Precisa de... | Vá para... |
|---|---|
| Convenções do projeto | `AGENTS.md` |
| Visão geral arquitetura | `ARCHITECTURE.md` |
| Templates de prompts | `docs/AGENT_PROMPTS.md` |
| Decisões de design | `docs/DESIGN.md` |
| Política de segurança | `docs/SECURITY.md` |
| Operações/reliability | `docs/RELIABILITY.md` |
| Specs de features | `docs/product-specs/` |
| Exec plans em andamento | `docs/exec-plans/active/` |
| DB Migrations | `src/main/resources/db/migration/` |
| Main configs | `src/main/.../config/*.java` |
| Exemplo: Device CRUD | `controller/DeviceController.java` + `service/DeviceService.java` + `mapper/DeviceMapper.java` |

---

## ⚡ Pro Tips

1. **Sempre rode `./mvnw compile` após editar mappers** (MapStruct gera implementations)
2. **Use prompts de `docs/AGENT_PROMPTS.md`** quando pedir ajuda a agentes
3. **Validações globais:** Anotações `@Valid`, `@NotNull`, etc. são traduzidas em `ApplicationControllerAdvice`
4. **Conflitos de constraints:** `DataIntegrityViolationException` → 409 (handled globally)
5. **Criar novo exec-plan:** `./scripts/new-exec-plan.sh "Nome da Feature"`

---

## 🎓 Exemplo: Implementar CRUD de Device

Siga este fluxo:

1. **Request/Response DTOs** → `controller/request/DeviceRequest.java`, `controller/response/DeviceResponse.java`
2. **Entity** → `entity/DeviceEntity.java` (JPA anotada)
3. **Mapper** → `mapper/DeviceMapper.java` (MapStruct interface)
4. **Repository** → `repository/DeviceRepository.java` (Spring Data)
5. **Service** → `service/DeviceService.java` (lógica, usa repository)
6. **Controller** → `controller/DeviceController.java` (endpoints, usa service + mapper)
7. **Migration** (se houver) → `src/main/resources/db/migration/V8__*.sql`

Código de exemplo: Ver `controller/DeviceController.java` e `service/DeviceService.java`

---

## 🚨 Erros Comuns

| Erro | Solução |
|---|---|
| "Cannot find symbol: DeviceMapper" | Rode `./mvnw compile` (MapStruct gera implementation) |
| Mapper injected but null | Certifique-se de `@Mapper(componentModel = "spring")` no interface |
| 409 Conflict no endpoint | Validação BD falhou (check constraints/uniques) — ver `ApplicationControllerAdvice` |
| 403 Forbidden | JWT inválido ou faltando — verify `SecurityFilter` e header Bearer |
| 404 ao encontrar recurso | Service retorna `Optional.empty()` — controller retorna `ResponseEntity.notFound()` |

---

## 📞 Próximos Passos

- [ ] Ler `AGENTS.md` (10 min)
- [ ] Ler `ARCHITECTURE.md` (5 min)
- [ ] Familiarizar com `docs/AGENT_PROMPTS.md` (5 min)
- [ ] Explorar exemplo em `controller/DeviceController.java` (5 min)
- [ ] Pronto para começar! 🚀

---

**Quer adicionar uma feature?** Use um template de `docs/AGENT_PROMPTS.md` ou crie um exec-plan com `./scripts/new-exec-plan.sh`.

