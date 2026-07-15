# 📋 PLANS — ExecPlan Specification for Feature Development

An **ExecPlan** is a self-contained, executable specification for building a feature or significant refactor. It guides development from intent to implementation, capturing decisions and discoveries along the way.

Use this template when tackling complex features, architectural changes, or multi-step refactors.

---

## ExecPlan Template

Every ExecPlan should include these sections:

### 1. **Title & Purpose**
```
# ExecPlan: [Feature Name]

**Goal**: A 1-2 sentence summary of what users can do after this work is complete.

Example:
- Goal: Users can search repair orders by customer name and filter by status.
- Goal: Add pagination to device listing endpoints for better performance.
```

### 2. **Big Picture & Context**
Explain the current state and what's missing:
```
## Big Picture

**Current State**:
- Repair orders can only be retrieved by ID or listed all.
- No filtering or pagination; large result sets slow down UI.
- Customer names are embedded but not queryable.

**Desired State**:
- GET /repair-order?customerName=João&status=OPEN returns filtered results.
- Paginated response with metadata (page, size, total).

**Why Now**:
- Performance issue affecting technicians in large repair backlogs.
- Quick win: add repository query + controller param.

**Affected Modules**:
- controller/RepairOrderController.java
- service/RepairOrderService.java
- repository/RepairOrderRepository.java
```

### 3. **Progress Checklist**
Track work as you go:
```
## Progress

- [ ] Understand current RepairOrder query capabilities
- [ ] Add repository method: findRepairOrdersByCustomerNameAndStatus
- [ ] Add service method: findByCustomerNameAndStatus
- [ ] Add controller endpoint: GET /repair-order?customerName=&status=
- [ ] Write manual test request
- [ ] Validate response format + status codes
- [ ] Update GLOSSARY.md if new terms introduced
```

### 4. **Surprises & Discoveries**
Document unexpected findings:
```
## Surprises & Discoveries

**2025-07-14 13:30**: 
- Found that `RepairOrder.customer` is embedded (not a foreign key).
- This means customer name is stored in the `repair_orders` table directly.
- Query will be simpler than expected: filter on embedded `customer_name` column.

**2025-07-14 14:00**:
- ApplicationControllerAdvice doesn't handle pagination params; we'll return a simple filtered list for MVP.
- TODO: Implement page/size handling in future iteration.
```

### 5. **Decision Log**
Record every design choice and rationale:
```
## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2025-07-14 | Use derived query in repository | Simpler than @Query; follows Spring Data conventions. |
| 2025-07-14 | Add `@RequestParam` for customerName | URL filtering standard; avoids request body complexity. |
| 2025-07-14 | Allow null customerName (optional filter) | If not provided, return all by status; more flexible. |
| 2025-07-14 | Return List<RepairOrderResponse> | MVP; pagination deferred to next phase. |
```

### 6. **Context & Orientation**
State assumptions and reference key files:
```
## Context & Orientation

**Assumptions**:
- RepairOrder.customer is embedded (not separate entity).
- PostgreSQL supports ILIKE for case-insensitive search.
- Calling code can handle empty lists (not null).

**Key Files**:
- entity/RepairOrder.java — embedded Customer; current schema
- repository/RepairOrderRepository.java — where new query goes
- service/RepairOrderService.java — delegates to repository
- controller/RepairOrderController.java — exposes endpoint
- config/ApplicationControllerAdvice.java — error handling

**Related ExecPlans**:
- N/A (first iteration)

**External Docs**:
- Spring Data derived queries: https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
```

### 7. **Plan of Work**
Concrete sequence of edits and additions:

```
## Plan of Work

### Step 1: Add Repository Query
**File**: repository/RepairOrderRepository.java
- Add method: `List<RepairOrder> findByCustomerNameContainingIgnoreCaseAndStatus(String customerName, RepairOrderStatus status);`
- Or, if customerName optional: Add two methods or use `@Query`.

### Step 2: Add Service Method
**File**: service/RepairOrderService.java
- Add: `public List<RepairOrder> findByCustomerNameAndStatus(String customerName, RepairOrderStatus status)`
- Delegates to repository; returns empty list if no matches.

### Step 3: Update Controller
**File**: controller/RepairOrderController.java
- Add `@GetMapping` with `@RequestParam(required = false) String customerName` and `@RequestParam(required = false) RepairOrderStatus status`.
- If both null, return all repair orders (existing behavior).
- Otherwise, call service; map to response DTOs; return list.

### Step 4: Manual Testing
- Start app: `./mvnw spring-boot:run`
- Test request: `GET /repair-order?customerName=João&status=OPEN`
- Verify response: 200 + filtered list or empty array.
- Test no params: `GET /repair-order` → all orders.

### Step 5: Update Documentation
- Add entry to GLOSSARY.md if "RepairOrderSearch" is a new term.
- Update ARCHITECTURE.md query examples if relevant.
```

### 8. **Concrete Steps**
Exact commands with expected output:

```
## Concrete Steps

### Command 1: Compile & Generate MapStruct
```bash
./mvnw clean compile
```
**Expected Output**:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 12.34 s
```

### Command 2: Run App
```bash
./mvnw spring-boot:run
```
**Expected Output**:
```
2025-07-14 14:30:00.000  INFO 12345 --- [main] ... Started EletrolonghiApplication ...
2025-07-14 14:30:05.000  INFO 12345 --- [main] ... Tomcat started on port(s): 8080
```

### Command 3: Test Endpoint (in new terminal)
```bash
curl -X GET "http://localhost:8080/repair-order?customerName=Jo%C3%A3o&status=OPEN" \
  -H "Authorization: Bearer $(curl -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"userName":"carlos","password":"123456"}' | jq -r '.token')" \
  -H "Content-Type: application/json"
```
**Expected Output**:
```json
[
  {
    "id": 1,
    "device": {...},
    "customer": {
      "name": "João",
      "email": "joao@email.com",
      "phone": "11999999999",
      "cpf": "123.456.789-00"
    },
    "status": "OPEN",
    "createdAt": "2025-07-14T10:00:00Z"
  }
]
```
```

### 9. **Validation & Acceptance**
How to verify the feature works:

```
## Validation & Acceptance

**Acceptance Criteria**:
- [ ] Endpoint `GET /repair-order?customerName=&status=` returns 200 + filtered RepairOrderResponse[].
- [ ] If no customerName provided, status filter still works.
- [ ] If no status provided, customerName filter still works.
- [ ] If neither provided, returns all repair orders.
- [ ] Empty result returns 200 + [].
- [ ] Invalid status enum returns 400 + error message.
- [ ] Unauthenticated request returns 403.

**Test Scenarios**:
1. `GET /repair-order?customerName=João&status=OPEN` → returns João's open orders.
2. `GET /repair-order?status=COMPLETED` → returns all completed orders.
3. `GET /repair-order?customerName=NotExist&status=OPEN` → returns [].
4. `GET /repair-order` → returns all.
5. `GET /repair-order?status=INVALID` → returns 400.
```

### 10. **Outcomes & Retrospective**
Summarize what was learned:

```
## Outcomes & Retrospective

**What Shipped**:
- New endpoint: GET /repair-order?customerName=&status=
- Service layer handles filtering logic.
- Repository method uses derived query (no custom SQL).

**What We Learned**:
- Embedded Customer entity simplified query (no joins).
- Null-safe filtering in controller reduced edge cases.
- Test with real JWT tokens (previous attempt failed auth).

**What's Left**:
- Pagination (future: add Spring Data Page<T>).
- Advanced search (customer email, phone, CPF).
- Performance: consider indexing customer_name column.

**Time Spent**:
- Planning & research: 30 min
- Implementation: 45 min
- Testing & fixes: 30 min
- **Total**: ~2 hours

**Lessons for Next Time**:
- Always test auth headers early (wasted 20 min on 403 errors).
- Embedded vs FK decisions impact query complexity; clarify upfront.
```

---

## When to Write an ExecPlan

**Use ExecPlans for**:
- New entities or endpoints
- Cross-module changes (affects controller + service + repository)
- Refactoring existing features
- Performance or security improvements
- Database schema migrations

**Skip ExecPlans for**:
- Trivial bug fixes
- Documentation-only changes
- Single-file tweaks (e.g., typo in comment)

---

## ExecPlan Naming Convention

Store ExecPlans in a `.agents/execplans/` directory (create if needed):

```
.agents/execplans/
├── 2025-07-14-repair-order-search.md
├── 2025-07-15-device-pagination.md
└── 2025-07-20-jwt-token-refresh.md
```

**Format**: `YYYY-MM-DD-feature-name.md`

---

## Tips for Writing Effective ExecPlans

### 1. **Start Broad, Then Narrow**
- Begin with "Big Picture" to align on goals.
- Move to "Plan of Work" for concrete steps.
- Enables agents to validate assumptions before implementing.

### 2. **Use Checklists**
- Progress section lets agent track what's done.
- Acceptance criteria define "done".

### 3. **Capture Decisions**
- Decision log prevents second-guessing later.
- Rationale helps future agents understand trade-offs.

### 4. **Update as You Go**
- ExecPlan is not final; evolve it.
- "Surprises" section catches unexpected complexity.
- Keeps team aware of progress.

### 5. **Link to Codebase**
- Reference exact files and methods.
- Reduces context-switching for agents.

### 6. **Make Commands Copy-Paste Ready**
- "Concrete Steps" should be runnable verbatim.
- Include expected output.

---

## Example: Minimal ExecPlan

For quick features, a minimal ExecPlan is sufficient:

```markdown
# ExecPlan: Add Device Color Field

## Goal
Technicians can record device color (e.g., "Black", "Silver") for repair descriptions.

## Context
- Current: Device has model, serialNumber, brand, accessories.
- New: Add color field to Device entity, request/response DTOs, and controller.

## Plan
1. Add `color: String` to entity/Device.java
2. Create V8__add_device_color.sql migration
3. Update controller/request/DeviceRequest.java + response/DeviceResponse.java
4. MapStruct auto-maps; no mapper changes needed
5. Test: Create device with color, verify response

## Done
- [ ] Entity updated
- [ ] Migration created
- [ ] DTOs updated
- [ ] Manual test passed
```

---

## Integration with Agent Workflow

Agents should:

1. **Read** the ExecPlan fully before starting.
2. **Check Progress** — Mark items done as they go.
3. **Log Decisions** — Update decision table if new choices arise.
4. **Record Surprises** — Note any unexpected complexity or bugs.
5. **Update Acceptance** — Ensure all criteria pass before marking feature done.
6. **Finalize** — Run final validation; update "Outcomes & Retrospective".

---

**Last updated**: 2026-07-14  
**Version**: 1.0  
**Example ExecPlans**: See `.agents/execplans/` directory.

