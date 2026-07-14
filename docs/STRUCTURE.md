# STRUCTURE

Mapa detalhado de diretórios e responsabilidades de cada pasta.

---

## Estrutura Raiz

```
API-Eletro-Longhi/
├── AGENTS.md                          # Onboarding para agentes de IA
├── ARCHITECTURE.md                    # Resumo rápido de arquitetura
├── README.md                          # Documentação principal do projeto
├── pom.xml                            # Configuração Maven
├── mvnw / mvnw.cmd                    # Maven wrapper (use ./mvnw)
├── docker-compose.yml                 # Orquestra PostgreSQL (apenas DB)
├── docs/                              # 📚 Documentação estruturada
├── scripts/                           # 🔧 Utilitários auxiliares
├── src/
│   ├── main/
│   │   ├── java/br/com/carloslonghi/eletrolonghi/  # Código-fonte Java
│   │   └── resources/                 # Configurações e migrations
│   └── test/
│       └── java/                      # Testes automatizados
└── target/                            # Build output (gerado)
```

---

## `/docs` — Documentação Estruturada

```
docs/
├── QUICK_START_FOR_AGENTS.md          # ⭐ LEIA PRIMEIRO (para agentes)
├── AGENT_PROMPTS.md                   # Templates de prompts para LLMs
├── TROUBLESHOOTING.md                 # Guia de resolução de problemas
├── STRUCTURE.md                       # Este arquivo
├── DESIGN.md                          # Decisões de design importantes
├── FRONTEND.md                        # Notas sobre integração front-end
├── PLANS.md                           # Roadmap e planejamento
├── PRODUCT_SENSE.md                   # Contexto de produto
├── QUALITY_SCORE.md                   # Critérios de qualidade
├── RELIABILITY.md                     # Operações e disponibilidade
├── SECURITY.md                        # Política de segurança
├── design-docs/
│   ├── index.md                       # Índice de design docs
│   └── core-beliefs.md                # Princípios e crenças arquiteturais
├── exec-plans/
│   ├── README.md                      # Explicação de exec-plans
│   ├── active/                        # Planos em andamento
│   └── completed/                     # Planos finalizados
├── generated/
│   └── db-schema.md                   # Schema do BD (documentação)
├── product-specs/
│   ├── index.md                       # Índice de specs
│   └── new-user-onboarding.md         # Spec: onboarding de usuário
└── references/
    └── design-system-reference-llms.txt  # Referências para prompts/LLMs
```

---

## `/scripts` — Utilitários & Automação

```
scripts/
├── README.md                          # Documentação de scripts
├── new-exec-plan.sh                   # Cria novo exec-plan com template
└── [futuros]
    ├── new-migration.sh               # Gera migração BD (planejado)
    └── generate-crud.sh               # Scaffolding de CRUD (planejado)
```

**Uso rápido:**
```bash
./scripts/new-exec-plan.sh "Nome da Feature"
```

---

## `src/main/java/br/com/carloslonghi/eletrolonghi/` — Código-fonte

### Estrutura por Camadas

```
eletrolonghi/
├── EletrolonghiApplication.java       # Entry point (main)
├── config/                            # 🔧 Configuração global
│   ├── ApplicationControllerAdvice.java
│   ├── JWTUserData.java
│   ├── SecurityConfig.java
│   ├── SecurityFilter.java
│   ├── SwaggerConfig.java
│   └── TokenService.java
├── controller/                        # 🌐 REST Endpoints
│   ├── BrandController.java
│   ├── CustomerController.java
│   ├── DeviceController.java
│   ├── AccessoryController.java
│   ├── RepairOrderController.java
│   ├── UserController.java
│   ├── request/                       # Request DTOs (records)
│   │   ├── BrandRequest.java
│   │   ├── CustomerRequest.java
│   │   ├── DeviceRequest.java
│   │   ├── AccessoryRequest.java
│   │   ├── RepairOrderRequest.java
│   │   └── UserRequest.java
│   └── response/                      # Response DTOs (records)
│       ├── BrandResponse.java
│       ├── CustomerResponse.java
│       ├── DeviceResponse.java
│       ├── AccessoryResponse.java
│       ├── RepairOrderResponse.java
│       └── UserResponse.java
├── entity/                            # 🗄️ Entidades JPA
│   ├── BrandEntity.java
│   ├── CustomerEntity.java
│   ├── DeviceEntity.java
│   ├── AccessoryEntity.java
│   ├── RepairOrderEntity.java
│   ├── UserEntity.java
│   └── enums/
│       ├── RepairStatusEnum.java
│       └── [outros enums]
├── repository/                        # 📊 Spring Data Repositories
│   ├── BrandRepository.java
│   ├── CustomerRepository.java
│   ├── DeviceRepository.java
│   ├── AccessoryRepository.java
│   ├── RepairOrderRepository.java
│   └── UserRepository.java
├── service/                           # 🧠 Lógica de negócio
│   ├── BrandService.java
│   ├── CustomerService.java
│   ├── DeviceService.java
│   ├── AccessoryService.java
│   ├── RepairOrderService.java
│   └── UserService.java
├── mapper/                            # 🔄 MapStruct (DTO ↔ Entity)
│   ├── BrandMapper.java
│   ├── CustomerMapper.java
│   ├── DeviceMapper.java
│   ├── AccessoryMapper.java
│   ├── RepairOrderMapper.java
│   └── UserMapper.java
└── exception/                         # ⚠️ Exceções personalizadas
    ├── BusinessException.java
    ├── ResourceNotFoundException.java
    └── [outras exceções]
```

---

### Descrição de Cada Camada

#### `config/`

**Responsabilidade:** Configuração central da aplicação.

| Arquivo | Função |
|---------|--------|
| `SecurityConfig.java` | Define regras de segurança (endpoints públicos, filtros) |
| `SecurityFilter.java` | Aplica JWT em requisições (extrai token, inicia contexto) |
| `TokenService.java` | Gera e verifica tokens JWT (claims: userId, userName) |
| `ApplicationControllerAdvice.java` | Tratamento global de exceções (409, 400, 404, etc.) |
| `SwaggerConfig.java` | Configura OpenAPI/Swagger UI |
| `JWTUserData.java` | POJO com dados extraídos do JWT (userId, userName) |

**Quando modificar:**
- Nova regra de segurança → `SecurityConfig.java`
- Novo claim do JWT → `TokenService.java` + `SecurityFilter.java`
- Novo tipo de erro global → `ApplicationControllerAdvice.java`

---

#### `controller/`

**Responsabilidade:** Aceitar requisições HTTP, validar DTOs, orquestrar service e retornar responses.

**Padrão de um Controller:**

```java
@RestController
@RequestMapping("/api/brand")
public class BrandController {
    @Autowired private BrandService service;
    @Autowired private BrandMapper mapper;
    
    @GetMapping
    public ResponseEntity<List<BrandResponse>> list() { ... }
    
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getById(@PathVariable Long id) { ... }
    
    @PostMapping
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request) { ... }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

**Request DTOs:** Records imutáveis com anotações de validação (`@NotNull`, `@NotBlank`).  
**Response DTOs:** Records imutáveis que refletem entity, sem internals expostos.

---

#### `entity/`

**Responsabilidade:** Representar tabelas do BD em Java (JPA).

**Padrão de uma Entity:**

```java
@Entity
@Table(name = "brands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Enums:** Em `entity/enums/` (ex: RepairStatusEnum.java).

---

#### `repository/`

**Responsabilidade:** Acesso ao BD via Spring Data JPA.

**Padrão de um Repository:**

```java
public interface BrandRepository extends JpaRepository<BrandEntity, Long> {
    // Spring Data gera queries automaticamente (padrão naming)
    Optional<BrandEntity> findByName(String name);
    List<BrandEntity> findByNameContainingIgnoreCase(String name);
}
```

**Convenção:** Use nomes descritivos (ex: `findDevicesByBrandId`, `findByStatusAndDateRange`).

---

#### `service/`

**Responsabilidade:** Implementar lógica de negócio, validações e orquestrar repositories.

**Padrão de um Service:**

```java
@Service
public class BrandService {
    @Autowired private BrandRepository repository;
    
    public List<BrandEntity> findAll() {
        return repository.findAll();
    }
    
    public Optional<BrandEntity> findById(Long id) {
        return repository.findById(id);
    }
    
    public BrandEntity save(BrandEntity entity) {
        // Validações de negócio aqui
        return repository.save(entity);
    }
    
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
```

**Retorno de valores:**
- Single entity: `Optional<Entity>`
- Coleções: `List<Entity>`

---

#### `mapper/`

**Responsabilidade:** Converter entre DTOs e Entities (MapStruct).

**Padrão de um Mapper:**

```java
@Mapper(componentModel = "spring")
public interface BrandMapper {
    
    BrandEntity toEntity(BrandRequest request);
    BrandResponse toResponse(BrandEntity entity);
    
    // Helper para id → entity
    default BrandEntity brandFromId(Long id) {
        if (id == null) return null;
        BrandEntity entity = new BrandEntity();
        entity.setId(id);
        return entity;
    }
}
```

**Características:**
- Interface (não classe abstrata)
- `@Mapper(componentModel = "spring")` para injetar como bean
- Métodos `toEntity()` e `toResponse()`
- Helper methods para conversões especiais (id → entity, etc.)

---

#### `exception/`

**Responsabilidade:** Exceções personalizadas que são capturadas e tratadas globalmente.

**Exemplo:**

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

Tratamento em `ApplicationControllerAdvice`:

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
}
```

---

## `src/main/resources/` — Configurações & Migrations

```
resources/
├── application.properties              # Configuração da app (DB, JWT, logging)
└── db/migration/                       # Flyway migrations
    ├── V1__create_table_brands.sql
    ├── V2__create_table_accessories.sql
    ├── V3__create_table_devices.sql
    ├── V4__create_table_devices_accessories.sql
    ├── V5__create_table_customers.sql
    ├── V6__create_table_repair_orders.sql
    └── V7__create_table_users.sql
```

**`application.properties`:**
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/eletro_longhi
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT Secret
spring.security.secret=sua-chave-super-secreta-aqui

# Flyway
spring.flyway.baseline-on-migrate=true

# OpenAPI/Swagger
springdoc.api-docs.path=/api/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

**Migrations:** Numeradas sequencialmente (V1, V2, ... V8, ...). Arquivo atual: V7.

---

## `src/test/` — Testes

```
test/
└── java/br/com/carloslonghi/eletrolonghi/
    └── EletrolonghiApplicationTests.java  # Teste básico (placeholder)
```

**Status:** Testes ainda são uma melhoria futura. Para começar, crie testes em `src/test/java/`.

---

## `target/` — Build Output (Gerado)

```
target/
├── classes/                           # Classes compiladas
├── generated-sources/annotations/     # MapStruct implementations geradas
├── eletrolonghi-0.0.1-SNAPSHOT.jar   # JAR executável
└── [outros]
```

**Nota:** Não commite. Está em `.gitignore`.

---

## Fluxo de Dados

Exemplo: Criar nova marca

```
POST /api/brand
  ↓
BrandController.create(BrandRequest)
  ↓
BrandMapper.toEntity(request) → BrandEntity
  ↓
BrandService.save(entity)
  ↓
BrandRepository.save(entity) → BD (PostgreSQL)
  ↓
BrandMapper.toResponse(saved) → BrandResponse
  ↓
ResponseEntity<BrandResponse> (201 Created)
```

---

## Checklist: Achar um arquivo

| Preciso de... | Tipo | Caminho |
|---|---|---|
| Regra de rota HTTP | Controller | `controller/[Recurso]Controller.java` |
| Validação de entrada | Request DTO | `controller/request/[Recurso]Request.java` |
| Formato de saída | Response DTO | `controller/response/[Recurso]Response.java` |
| Lógica de negócio | Service | `service/[Recurso]Service.java` |
| Query ao BD | Repository | `repository/[Recurso]Repository.java` |
| Tabela do BD | Entity | `entity/[Recurso]Entity.java` |
| Conversão DTO ↔ Entity | Mapper | `mapper/[Recurso]Mapper.java` |
| Regra de segurança | Config | `config/SecurityConfig.java` ou `config/SecurityFilter.java` |
| Tratamento de erro | Advice | `config/ApplicationControllerAdvice.java` |
| DB schema | Migration | `src/main/resources/db/migration/V[N]__*.sql` |

---

## Boas Práticas de Navegação

1. **Comece pelo Controller** (onde entra a requisição)
2. **Siga para o Mapper** (DTO → Entity)
3. **Depois o Service** (onde está a lógica)
4. **Finalize no Repository** (query ao BD)
5. **Se erro:** verifique `ApplicationControllerAdvice.java`


