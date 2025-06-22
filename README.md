## API Eletro Longhi

API RESTful para gerenciamento de serviços de conserto de equipamentos em uma eletrônica.

---

### 📖 Visão Geral

* **Título:** API Eletro Longhi
* **Versão:** v1
* **Base URL (desenvolvimento):** `http://localhost:8080`
* **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
* **Swagger/OpenAPI:** `http://localhost:8080/api/api-docs`

---

### 🚀 Tecnologias

* Java 21
* Spring Boot 3
* Spring Web, Spring Security (JWT)
* Spring Data JPA
* Flyway (migrations)
* Hibernate Validator (Bean Validation)
* Lombok
* Swagger/OpenAPI (Springdoc)
* PostgreSQL
* Docker

---

### 📋 Pré-requisitos

* JDK 21 instalado
* Maven ou Gradle
* PostgreSQL em execução

    * Banco de dados vazio criado para a aplicação
* Git

---

### ⚙️ Configuração e Inicialização

1. **Clone o repositório**

   ```bash
   git clone https://github.com/CarlosLonghi/API-Eletro-Longhi
   cd API-Eletro-Longhi
   ```

2. **Configure as credenciais do banco**
   No arquivo `src/main/resources/application.properties`, ajuste:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/seu_banco
   spring.datasource.username=seu_usuario
   spring.datasource.password=sua_senha

   spring.flyway.baseline-on-migrate=true
   springdoc.override-with-generic-response=false
   ```

3. **Execute as migrations**
   Ao iniciar a aplicação, o Flyway criará as tabelas e enums automaticamente.

4. **Compile e rode**

   ```bash
   # Usando Maven
   mvn clean spring-boot:run

   # Ou usando Gradle
   ./gradlew bootRun
   ```

5. **Acesse a documentação interativa**

   Abra no navegador:

    ```
   http://localhost:8080/swagger-ui/index.html
   ```
    ou
   ```
   http://localhost:8080/api/api-docs
   ```

---

### 🔖 Tags e Endpoints Principais

| Tag              | Descrição                                      | Base Path       |
| ---------------- | ---------------------------------------------- | --------------- |
| **Autenticação** | Registro e login de usuários (JWT)             | `/auth`         |
| **Marca**        | CRUD de marcas de dispositivos                 | `/brand`        |
| **Cliente**      | CRUD de clientes                               | `/customer`     |
| **Aparelho**     | CRUD de aparelhos                              | `/device`       |
| **Acessório**    | CRUD de acessórios                             | `/accessory`    |
| **Reparo**       | Gerenciamento de ordens de serviço de conserto | `/repair-order` |

Alguns exemplos:

* **POST** `/auth/register` → registra um novo usuário.
* **POST** `/auth/login` → faz login e retorna JWT.
* **GET** `/brand` → lista todas as marcas.
* **POST** `/repair-order` → cria uma nova ordem de reparo.
* **PUT** `/device/{id}` → atualiza um aparelho existente.

---

### 📦 Payloads (exemplos)

* **LoginRequest**

  ```json
  {
    "email": "user@example.com",
    "password": "senha123"
  }
  ```

* **LoginResponse**

  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI..."
  }
  ```

* **RepairOrderRequest**

  ```json
  {
    "description": "Tela quebrada",
    "status": "AWAITING_EVALUATION",
    "customer": 1,
    "device": 1
  }
  ```

---

### 🛡️ Tratamento de Erros

A API retorna payload padronizado com:

```json
{
  "timestamp": "2025-06-22T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Já existe um acessório com esse nome.",
  "path": "/accessory"
}
```

* **400 Bad Request** — validação de payload
* **401 Unauthorized** — credenciais inválidas
* **403 Forbidden** — sem token ou sem permissão
* **404 Not Found** — recurso não encontrado
* **409 Conflict** — violação de integridade (duplicidade, FK)

---

### 📚 Mais informações

* **Código-fonte:** `https://github.com/CarlosLonghi/API-Eletro-Longhi`
* **Documentação completa:** `http://localhost:8080/api/api-docs`

—
Desenvolvido por **Carlos Longhi** ([carloslonghi.cl@gmail.com](mailto:carloslonghi.cl@gmail.com))
