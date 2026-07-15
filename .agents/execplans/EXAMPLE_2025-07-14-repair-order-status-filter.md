# ExecPlan: Add Repair Order Status Filter

**Date**: 2025-07-14  
**Author**: AI Agent  
**Status**: Example (Not implemented)

---

## 🎯 Goal

Technicians can filter repair orders by status (`OPEN`, `IN_PROGRESS`, `COMPLETED`, `CANCELED`) to focus on active work, improving workflow efficiency.

---

## 📖 Big Picture & Context

### Current State
- `GET /repair-order` returns all repair orders without filtering.
- Technicians must manually search through potentially hundreds of orders.
- No performance optimization for common queries.

### Desired State
- `GET /repair-order?status=IN_PROGRESS` returns only in-progress repairs.
- Optional filtering; if no status param, returns all orders.
- Uses Spring Data derived query for efficiency.

### Why Now
- Quick win; improves technician workflow.
- Demonstrates agent-driven feature development via ExecPlan.

### Affected Modules
- `repository/RepairOrderRepository.java` — new derived query
- `service/RepairOrderService.java` — new service method
- `controller/RepairOrderController.java` — new endpoint param

---

## ✅ Progress Checklist

- [ ] Understand RepairOrderStatus enum and database schema
- [ ] Add repository method: `List<RepairOrder> findByStatus(RepairOrderStatus status)`
- [ ] Add service method: `findByStatus(RepairOrderStatus status)`
- [ ] Update controller: add `@RequestParam(required = false) RepairOrderStatus status`
- [ ] Manual test: curl with status param
- [ ] Verify response format and status codes (200, 400, 403)
- [ ] Update docs if needed

---

## 🔍 Surprises & Discoveries

*(To be filled in as agent executes)*

---

## 📝 Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2025-07-14 | Use derived query (not @Query) | Simpler; follows Spring Data conventions |
| 2025-07-14 | Make status param optional | If not provided, return all (backward compatible) |
| 2025-07-14 | Return List not Page | MVP; pagination future work |

---

## 🧭 Context & Orientation

### Key Assumptions
- RepairOrderStatus enum is defined and used in entity/RepairOrder.java.
- Spring Data supports derived queries like `findByStatus(Enum status)`.
- Client can handle empty lists (not null).

### Key Files
- `entity/RepairOrder.java` — status field definition
- `repository/RepairOrderRepository.java` — where new query goes
- `service/RepairOrderService.java` — delegates to repo
- `controller/RepairOrderController.java` — exposes endpoint

### Related Docs
- `.agents/GLOSSARY.md` — RepairOrderStatus enum definition
- `.agents/ARCHITECTURE.md` — repository naming conventions

---

## 📋 Plan of Work

### Step 1: Update Controller
**File**: `controller/RepairOrderController.java`

Add `@RequestParam(required = false)` for status:
```java
@GetMapping
public ResponseEntity<List<RepairOrderResponse>> list(
    @RequestParam(required = false) RepairOrderStatus status,
    @RequestHeader("Authorization") String token
) {
    List<RepairOrder> orders;
    if (status != null) {
        orders = repairOrderService.findByStatus(status);
    } else {
        orders = repairOrderService.findAll();
    }
    return ResponseEntity.ok(orders.stream()
        .map(repairOrderMapper::toResponse)
        .toList());
}
```

### Step 2: Add Service Method
**File**: `service/RepairOrderService.java`

```java
public List<RepairOrder> findByStatus(RepairOrderStatus status) {
    return repairOrderRepository.findByStatus(status);
}
```

### Step 3: Add Repository Query
**File**: `repository/RepairOrderRepository.java`

```java
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {
    List<RepairOrder> findByStatus(RepairOrderStatus status);
}
```

### Step 4: Manual Testing
- Start app: `./mvnw spring-boot:run`
- Test 1: `GET /repair-order?status=IN_PROGRESS` → returns matching orders
- Test 2: `GET /repair-order?status=COMPLETED` → returns completed orders
- Test 3: `GET /repair-order` → returns all orders
- Test 4: `GET /repair-order?status=INVALID` → returns 400

### Step 5: Verify Response Format
Ensure response matches expected DTO:
```json
[
  {
    "id": 1,
    "device": {...},
    "customer": {...},
    "status": "IN_PROGRESS",
    "description": "LCD broken",
    "diagnosis": "Display module faulty",
    "cost": 150.00,
    "createdAt": "2025-07-14T10:00:00Z",
    "updatedAt": "2025-07-14T11:30:00Z"
  }
]
```

---

## 🔧 Concrete Steps

### Step 1: Compile
```bash
./mvnw clean compile
```
**Expected**:
```
[INFO] BUILD SUCCESS
```

### Step 2: Run App
```bash
./mvnw spring-boot:run
```
**Expected**:
```
... Started EletrolonghiApplication in 8.234 seconds ...
Tomcat started on port(s): 8080
```

### Step 3: Get JWT Token
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userName":"carlos","password":"123456"}'
```
**Expected**:
```json
{
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
}
```

### Step 4: Test With Status Filter
```bash
TOKEN="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
curl -X GET "http://localhost:8080/repair-order?status=IN_PROGRESS" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```
**Expected**:
```json
[
  {
    "id": 1,
    "device": {...},
    "status": "IN_PROGRESS",
    ...
  }
]
```

### Step 5: Test Without Filter
```bash
curl -X GET "http://localhost:8080/repair-order" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```
**Expected**:
```json
[
  {id: 1, status: "IN_PROGRESS", ...},
  {id: 2, status: "COMPLETED", ...},
  {id: 3, status: "OPEN", ...}
]
```

### Step 6: Test Invalid Status
```bash
curl -X GET "http://localhost:8080/repair-order?status=INVALID" \
  -H "Authorization: Bearer $TOKEN"
```
**Expected**:
```
400 Bad Request
{
  "error": "Failed to convert value of type 'java.lang.String' to required type 'br.com.carloslonghi.eletrolonghi.entity.RepairOrderStatus'"
}
```

---

## ✅ Validation & Acceptance

### Acceptance Criteria
- [ ] Endpoint `GET /repair-order?status=OPEN` returns 200 + RepairOrderResponse[] with status=OPEN only.
- [ ] Endpoint `GET /repair-order?status=IN_PROGRESS` returns 200 + matching orders.
- [ ] Endpoint `GET /repair-order?status=COMPLETED` returns 200 + matching orders.
- [ ] Endpoint `GET /repair-order?status=CANCELED` returns 200 + matching orders.
- [ ] Endpoint `GET /repair-order` (no param) returns 200 + all orders.
- [ ] Empty result returns 200 + [] (not 404).
- [ ] Invalid status returns 400 + error message.
- [ ] Unauthenticated request returns 403 (requires JWT).
- [ ] Swagger UI reflects new parameter.

### Test Scenarios
1. **Happy Path**: `GET /repair-order?status=IN_PROGRESS` → all matching
2. **No Filter**: `GET /repair-order` → all orders
3. **Empty Result**: `GET /repair-order?status=COMPLETED` when none exist → []
4. **Bad Enum**: `GET /repair-order?status=WRONG` → 400
5. **No Auth**: `GET /repair-order` without token → 403

---

## 📊 Outcomes & Retrospective

*(To be filled in after implementation)*

### What Shipped
- New query parameter: `?status=`
- Service and repository methods for status filtering
- Backward compatibility: existing `GET /repair-order` still works

### What We Learned
- *To be added*

### What's Left
- Pagination (future: `?page=1&size=10`)
- Combined filters (status + customer name)
- Performance indexing

### Time Estimate
- Planning: 15 min
- Coding: 20 min
- Testing: 15 min
- **Total**: ~50 min

---

## 📚 References

- `.agents/GLOSSARY.md` — RepairOrderStatus enum
- `.agents/ARCHITECTURE.md` — Repository naming, service patterns
- Spring Data Query Methods: https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
- Project `README.md` — Endpoints overview

---

**End of ExecPlan**

