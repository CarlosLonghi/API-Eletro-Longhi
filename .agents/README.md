# 🤖 Agent Documentation

Welcome to the **Eletro Longhi Agent Documentation**. This directory contains specialized guides designed for AI coding agents to understand, navigate, and extend the codebase autonomously.

## 📚 Quick Navigation

### 🌍 **Start Here: [GLOSSARY.md](GLOSSARY.md)**
**What**: Domain terminology and architectural concepts.
**When to read**: First thing — understand the language (Brand, Device, RepairOrder, JWT, MapStruct, etc.).
**Time**: 10–15 minutes.
**Example**: What's a RepairOrder? How does it relate to Device and Customer?

### 🏗️ **Then: [ARCHITECTURE.md](ARCHITECTURE.md)**
**What**: System design, module locations, and architectural invariants.
**When to read**: Before making structural changes or adding new features.
**Time**: 15–20 minutes.
**Example**: Where does the `DeviceService` go? What does `@Mapper` mean? What are the non-negotiable rules?

### 📋 **For Complex Work: [PLANS.md](PLANS.md)**
**What**: ExecPlan specification for multi-step feature development.
**When to read**: Before tackling a significant feature or refactor.
**Time**: 10 minutes (template) + 1–2 hours (actual feature work).
**Example**: How do I plan adding "Device Color" support? What format should I use to document my work?

---

## 🎯 Workflow: How to Use These Docs (Optimized)

### **Scenario 1: I need to add a new endpoint** (Medium complexity)
```
Tokens saved: Skip STRUCTURE.md and SETUP.md
────────────────────────────────────────────
1. Read GLOSSARY.md (quick lookup: what entity?)
2. Read ARCHITECTURE.md → "Adding a New Feature" section
3. Look at existing code pattern (e.g., DeviceController.java)
4. Code: Controller → Service → Repository → Entity → Mapper
```

### **Scenario 2: I need to understand a complex flow** (Quick)
```
Tokens saved: Skip everything except GLOSSARY + ARCHITECTURE
────────────────────────────────────────────────────────────
1. GLOSSARY.md (learn the terms)
2. ARCHITECTURE.md → "Data Flow Diagram"
3. Trace: controller → service → repository → SQL
```

### **Scenario 3: I'm building a multi-step feature** (Complex)
```
Tokens saved: Skip STRUCTURE.md, SETUP.md, EXAMPLE files unless needed
─────────────────────────────────────────────────────────────────────
1. Read GLOSSARY.md (domain understanding)
2. Read ARCHITECTURE.md (code structure)
3. Read PLANS.md (feature planning format)
4. Copy execplans/TEMPLATE.md → your feature file
5. Follow your ExecPlan, updating as you discover things
```

### **Scenario 4: I found a bug or inconsistency** (Quick)
```
Tokens saved: Go straight to specific sections
─────────────────────────────────────────────
1. ARCHITECTURE.md → "Architectural Invariants" (did I violate one?)
2. GLOSSARY.md → (did I misunderstand a term?)
3. .agents/execplans/ → (was this already discovered?)
```

---

## 📖 Documentation Structure

| File | Purpose | Status | Read Time | Tokens |
|------|---------|--------|-----------|--------|
| **[GLOSSARY.md](GLOSSARY.md)** | Domain terms, entity relationships | **ESSENTIAL** | 15 min | ~1,800 |
| **[ARCHITECTURE.md](ARCHITECTURE.md)** | Module map, invariants, patterns | **ESSENTIAL** | 15 min | ~2,200 |
| **[PLANS.md](PLANS.md)** | ExecPlan template & best practices | ⚠️ Optional (for complex features) | 10 min | ~1,700 |
| **SETUP.md** | Getting started guide | ⚠️ Skip unless new | 5 min | ~1,200 |
| **STRUCTURE.md** | Visual overview | ⚠️ Skip (info already here) | 5 min | ~1,000 |
| **.agents/execplans/TEMPLATE.md** | Blank ExecPlan template | ⚠️ Copy when needed | — | ~600 |
| **.agents/execplans/EXAMPLE_*.md** | Worked example | ⚠️ Reference only | 15 min | ~2,200 |

---

## 🚀 Key Principles

### **1. Harness Engineering**
These docs are written for AI agents. They're:
- **Searchable** — Use keywords like "JWT", "Mapper", "RepairOrder".
- **Linked** — References point to exact files and line numbers.
- **Concrete** — Code examples and concrete steps, not vague advice.

### **2. Single Source of Truth**
- **Business Rules** → `GLOSSARY.md`
- **Code Structure** → `ARCHITECTURE.md`
- **Feature Process** → `PLANS.md`
- **Project History** → `.agents/execplans/`

### **3. Keep It Live**
- Update docs as you discover things.
- Add ExecPlans to `.agents/execplans/` for every significant feature.
- Mark decisions in ExecPlan decision logs.

---

## 📋 Common Tasks

### **Understand listing behavior (pagination + filters)**
👉 See `ARCHITECTURE.md` (controller/service/repository sections) + `GLOSSARY.md` (Pagination, Specification).

Current split:
- `Brand` and `Accessory`: simple `List` endpoints (no pagination/filter).
- `Device`, `Customer`, `RepairOrder`: paginated `Page` endpoints with advanced filters.

### **Add a new REST endpoint**
👉 See `ARCHITECTURE.md` → "Adding a New Feature: Step-by-Step"

### **Understand JWT authentication**
👉 See `GLOSSARY.md` → "JWT (JSON Web Token)" + `ARCHITECTURE.md` → "config/"

### **Change database schema**
👉 See `GLOSSARY.md` → "Flyway Migration" + `ARCHITECTURE.md` → "I7: Database migrations are append-only"

### **Add validation to a request DTO**
👉 See `ARCHITECTURE.md` → "controller/" + `GLOSSARY.md` → "DTO (Data Transfer Object)"

### **Understand entity relationships (Brand → Device → RepairOrder)**
👉 See `GLOSSARY.md` → "Core Entities" + `ARCHITECTURE.md` → "Data Flow Diagram"

---

## 🎓 Learning Path for New Agents

1. **5 min**: Skim this file (`README.md`).
2. **15 min**: Read `GLOSSARY.md` (domain + concepts).
3. **15 min**: Scan `ARCHITECTURE.md` (where code lives).
4. **5 min**: Review `AGENTS.md` in root (quick checklists).
5. **10 min**: Run the app locally (`./mvnw spring-boot:run`).
6. **10 min**: Test an endpoint via Swagger UI or `curl`.
7. **Ready**: Pick a task and follow the patterns you've learned.

---

## 📞 When Docs Don't Help

If you find something unclear or missing:
1. **Search** the codebase using grep/semantic search.
2. **Check existing ExecPlans** (`.agents/execplans/`) — it may be documented.
3. **Read the code** — follow patterns in existing modules (e.g., `DeviceService` as a model).
4. **Document your discovery** — add a note to the relevant doc or create a new ExecPlan.

---

## 🔗 Related Files (Root Level)

- **[../README.md](../README.md)** — Project overview, tech stack, endpoints.
- **[../AGENTS.md](../AGENTS.md)** — Quick checklist + project conventions (entry point for agents).
- **[../pom.xml](../pom.xml)** — Maven dependencies (MapStruct, Springdoc, JWT, etc.).
- **[../docker-compose.yml](../docker-compose.yml)** — Database setup (PostgreSQL).

---

## 📊 Example ExecPlans

(None yet; future work will add examples to `.agents/execplans/`.)

When you write your first ExecPlan, store it as:
```
.agents/execplans/YYYY-MM-DD-feature-name.md
```

Example:
- `.agents/execplans/2025-07-14-repair-order-search.md` — Filter repair orders by customer + status.
- `.agents/execplans/2025-07-15-device-pagination.md` — Add pagination to device list.

---

## 💾 Version & Maintenance

- **Last Updated**: 2026-07-20
- **Version**: 1.1 (listing behavior updated)
- **Maintainer**: AI Coding Agents + Human Reviewers
- **Update Frequency**: As features are added or architectural decisions change.

---

## 🎯 Success Criteria

This documentation is successful if:
- [ ] An agent can read `GLOSSARY.md` and understand the domain.
- [ ] An agent can read `ARCHITECTURE.md` and know where code belongs.
- [ ] An agent can follow `PLANS.md` to plan and execute multi-step work.
- [ ] An agent can read root `AGENTS.md` and start working on a task.
- [ ] A human reviewer can track an agent's decisions via ExecPlan logs.

---

**Welcome to the Eletro Longhi agent workspace. Happy coding! 🚀**

