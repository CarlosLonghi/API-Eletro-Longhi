# New User Onboarding — Spec

**Owner:** [Seu nome]  
**Status:** A preencher  
**Data:** [Quando escrito]

---

## Descrição

Jornada do novo usuário ao se registrar e fazer primeiro login na API.

---

## Fluxo Atual

```
1. Usuário acessa POST /auth/register
   ├── Fornece email + password
   └── Recebe token JWT ou user data

2. Usuário usa token em Bearer header
   └── Acessa endpoints protegidos (ex: /api/brand, /api/device)

3. Endpoints retornam dados via Swagger UI ou REST client
```

---

## Requisitos Funcionais

- [ ] Endpoint `/auth/register` aceita email + password
- [ ] Endpoint `/auth/login` gera JWT token
- [ ] JWT válido por [X] horas
- [ ] Endpoints protegidos retornam 403 sem token válido

---

## Requisitos Técnicos

### Endpoints

```
POST /auth/register
  Request:  { email, password }
  Response: { token } ou { userId, email }
  Status:   201 ou 409 (email existe)

POST /auth/login
  Request:  { email, password }
  Response: { token }
  Status:   200 ou 401 (credenciais inválidas)

GET /api/brand
  Headers:  Authorization: Bearer <token>
  Response: [ { id, name, created_at, updated_at } ]
  Status:   200 ou 403 (sem token)
```

### DTOs

**UserRequest (register/login):**
```json
{
  "email": "user@example.com",
  "password": "senha123"
}
```

**UserResponse (após login/register):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "user@example.com"
}
```

### Validações

- `email`: não vazio, formato email válido, único no BD
- `password`: não vazio, mínimo [X] caracteres
- Passwords hasheadas antes de persistir (nunca em plain text)

---

## Cenários de Teste (QA)

### Happy Path
- [x] Usuário registra com email + password válidos → 201, recebe token
- [x] Usuário faz login com credenciais corretas → 200, recebe token
- [x] Usuário usa token em Bearer header → acessa endpoints protegidos

### Error Cases
- [ ] Register com email já existente → 409 Conflict
- [ ] Register com email inválido → 400 Bad Request
- [ ] Register com password vazio → 400 Bad Request
- [ ] Login com email não registrado → 401 Unauthorized
- [ ] Login com password errada → 401 Unauthorized
- [ ] Endpoint protegido sem token → 403 Forbidden
- [ ] Endpoint protegido com token expirado → 403 Forbidden
- [ ] Endpoint protegido com token inválido → 403 Forbidden

---

## Métricas de Sucesso

- [ ] 100% dos novos usuários conseguem se registrar e logar
- [ ] Tempo de requisição de login < 100ms
- [ ] Erros de validação são claros e acionáveis

---

## Notas para Operação

- JWT secret em `spring.security.secret` em `application.properties`
- Mude secret antes de produção
- Monitorar tentativas falhadas de login (possível brute force)

---

## Próximas Fases (Roadmap)

- [ ] Refresh token (estender sessão)
- [ ] Social login (Google/GitHub)
- [ ] Two-factor authentication
- [ ] Email verification before registration


