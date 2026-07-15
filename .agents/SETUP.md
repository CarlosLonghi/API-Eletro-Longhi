# 🎯 SETUP.md — Getting Started with Agent Documentation

Welcome! This document helps you understand and use the **Eletro Longhi Agent Documentation** structure.

---

## 📁 What Was Created

```
.agents/
├── README.md                                    # ← Start here! Navigation & overview
├── GLOSSARY.md                                  # Domain terms & concepts
├── ARCHITECTURE.md                              # System design & module map
├── PLANS.md                                     # ExecPlan specification
├── SETUP.md                                     # This file
└── execplans/
    ├── README.md                                # ExecPlans guide
    ├── TEMPLATE.md                              # Copy this to create new ExecPlans
    └── EXAMPLE_2025-07-14-repair-order-status-filter.md  # Working example
```

---

## 🚀 Quick Start (5 minutes)

### For AI Agents

1. **Read this file** (you're reading it now).
2. **Go to [.agents/README.md](README.md)** — High-level overview + navigation.
3. **Read [GLOSSARY.md](GLOSSARY.md)** (10 min) — Learn domain terms.
4. **Skim [ARCHITECTURE.md](ARCHITECTURE.md)** (5 min) — Understand where code lives.
5. **Start coding** — Follow patterns you've learned.

### For Complex Features

1. Go through steps 1–4 above.
2. **Read [PLANS.md](PLANS.md)** (10 min) — Learn ExecPlan format.
3. **Copy [execplans/TEMPLATE.md](execplans/TEMPLATE.md)** → `execplans/YYYY-MM-DD-your-feature.md`.
4. **Fill it out as you work** — Update it live.
5. **Commit it with your feature** — Future agents can see your decisions.

---

## 📚 Documentation Roles

| Document | Best For | Time |
|----------|----------|------|
| **README.md** | Overview, navigation, quick reference | 5 min |
| **GLOSSARY.md** | Understanding domain (Brand, Device, RepairOrder, JWT, MapStruct, etc.) | 15 min |
| **ARCHITECTURE.md** | Code structure, naming conventions, invariants, adding features | 15 min |
| **PLANS.md** | Writing executable feature specs (ExecPlans) | 10 min (template) |
| **execplans/*** | Tracking actual features & decisions | 1–2 hours per feature |

---

## 🎯 Use Cases

### "I need to add a new endpoint"
1. Read GLOSSARY.md (domain understanding)
2. Read ARCHITECTURE.md → "Adding a New Feature" section
3. Look at `controller/DeviceController.java` as a pattern
4. Code it: Controller → Service → Repository → Entity → Mapper

### "I need to understand a complex flow"
1. Read GLOSSARY.md (learn the terms)
2. Read ARCHITECTURE.md → "Data Flow Diagram" section
3. Trace the code: start in controller → follow to service → repository → entity

### "I'm building a big feature"
1. Read PLANS.md (learn ExecPlan format)
2. Copy `execplans/TEMPLATE.md` to a new file
3. Fill out the "Goal", "Big Picture", "Context" sections first
4. Start coding; update Progress/Discoveries/Decisions as you go
5. Finalize with Outcomes & Retrospective

### "I found a bug or inconsistency"
1. Check ARCHITECTURE.md → "Architectural Invariants" — am I violating one?
2. Search GLOSSARY.md — did I misunderstand a concept?
3. Look for related ExecPlans in `execplans/` — was this already discovered?
4. Document your finding

---

## 🧭 Navigation Map

```
Want to understand...?          Read this...
────────────────────────────────────────────────────────
What a "Brand" or "Device" is   → GLOSSARY.md
What JWT tokens do              → GLOSSARY.md + ARCHITECTURE.md (config/ section)
Where the DeviceService lives   → ARCHITECTURE.md (service/ section)
How MapStruct works             → GLOSSARY.md (Mapper section) + ARCHITECTURE.md (mapper/ section)
Database migrations             → GLOSSARY.md (Flyway section) + ARCHITECTURE.md (I7)
How to add a new endpoint       → ARCHITECTURE.md ("Adding a New Feature" example)
How to plan a complex feature   → PLANS.md + execplans/TEMPLATE.md
What decisions were made        → execplans/EXAMPLE_*.md (Decision Log section)
```

---

## 💡 Key Principles

### **1. This Documentation is for Agents**
- Written to be machine-readable and navigable.
- Uses keywords, links, and concrete code examples.
- Optimized for search and cross-reference.

### **2. It's a Living Document**
- Update as you discover things.
- Add ExecPlans for every major feature.
- Keep decisions documented.

### **3. It Prevents Mistakes**
- Architectural Invariants are non-negotiable rules.
- Decision Logs help future agents understand trade-offs.
- Examples show patterns to follow.

---

## ✅ Checklist: Have You Set Up Agent Docs?

- [ ] Read this file (SETUP.md)
- [ ] Opened [README.md](README.md)
- [ ] Skimmed [GLOSSARY.md](GLOSSARY.md)
- [ ] Reviewed [ARCHITECTURE.md](ARCHITECTURE.md) → module map
- [ ] Understand the 8 architectural invariants
- [ ] Ready to code! Pick a task and follow the patterns.

---

## 🔗 Entry Points

### For Everyone
- **Start**: [.agents/README.md](README.md)

### For Understanding the Domain
- **Go To**: [GLOSSARY.md](GLOSSARY.md)

### For Adding Code
- **Go To**: [ARCHITECTURE.md](ARCHITECTURE.md)

### For Complex Features
- **Go To**: [PLANS.md](PLANS.md), then [execplans/TEMPLATE.md](execplans/TEMPLATE.md)

### For Project History & Decisions
- **Go To**: [execplans/](execplans/)

---

## 🚨 Common Mistakes to Avoid

1. **Skipping GLOSSARY.md** — You'll misunderstand domain terms; read it first.
2. **Not following naming conventions** — Services should return `Optional` for single lookups.
3. **Ignoring architectural invariants** — They exist for a reason; respect them.
4. **Not updating ExecPlans** — They're working documents; keep them current.
5. **Exposing entities directly** — Always map via DTOs; that's what mappers are for.

---

## 🎓 Learning Journey

### Day 1 (30 min)
- [ ] Read SETUP.md (this file) — 5 min
- [ ] Read README.md — 5 min
- [ ] Skim GLOSSARY.md — 10 min
- [ ] Skim ARCHITECTURE.md — 10 min

### Day 2 (Start Coding)
- [ ] Deep-dive GLOSSARY.md for your domain — 15 min
- [ ] Read ARCHITECTURE.md section relevant to your task — 10 min
- [ ] Look at existing code patterns — 15 min
- [ ] Code your feature following patterns — 2+ hours

### Ongoing
- [ ] Write ExecPlans for complex features — 30 min per feature
- [ ] Update documentation as you learn — 5 min per discovery
- [ ] Review decisions in ExecPlan decision logs — Ongoing

---

## 📞 FAQ

### Q: Do I need to read all these docs?
**A**: No. Skim GLOSSARY and ARCHITECTURE first; deep-dive as needed for your task.

### Q: When should I write an ExecPlan?
**A**: For any work that touches 3+ modules or involves a database migration. Skip for tiny bug fixes.

### Q: Can I update the docs?
**A**: Yes! Update GLOSSARY, ARCHITECTURE, PLANS as you learn. Add ExecPlans for every feature.

### Q: What if I find a typo or unclear section?
**A**: Fix it! These docs are for all agents; keep them accurate and clear.

### Q: How do I know if my code follows the architecture?
**A**: Check ARCHITECTURE.md → "Architectural Invariants". If you're breaking one, reconsider.

---

## 🏆 Success Criteria

You've successfully adopted agent documentation when:
- [ ] You can read GLOSSARY and explain the domain.
- [ ] You can look at ARCHITECTURE and know where code belongs.
- [ ] You can follow PLANS format to document work.
- [ ] You can reference invariants and avoid architectural debt.
- [ ] New agents on the project ramp up 30% faster (rough estimate).

---

## 🤝 Contributing to the Docs

### If You Discover Something New
1. Check if it's already documented.
2. If not, add it to the relevant doc (GLOSSARY, ARCHITECTURE, PLANS).
3. Commit with a message like: "Docs: Add JWT claim section to GLOSSARY".

### If You Write an ExecPlan
1. Copy `execplans/TEMPLATE.md`.
2. Name it `YYYY-MM-DD-feature-name.md`.
3. Fill it out as you work.
4. Commit with message: "ExecPlan: [Feature name]".

### If You Improve the Docs
1. Update the document.
2. Commit with message: "Docs: [Section] [what changed]".

---

## 📊 Quick Stats

- **Total Files**: 8 (including this setup)
- **Docs**: GLOSSARY, ARCHITECTURE, PLANS, 3x README
- **Examples**: 1 full ExecPlan + 1 template
- **Time to Read All**: ~60 minutes
- **Time per Feature to Document**: 30–40 min
- **Return on Investment**: Future agents ramp up faster; decisions are preserved

---

## 🎉 You're Ready!

You've completed setup. Here's what to do next:

1. **Pick a task** from your backlog.
2. **Go to [README.md](README.md)** for navigation.
3. **Follow the patterns** in GLOSSARY, ARCHITECTURE.
4. **Write an ExecPlan** if it's complex.
5. **Code confidently** knowing where everything goes.

**Happy shipping! 🚀**

---

## 📚 Files at a Glance

```
.agents/
├── README.md                    Main entry point; navigation & overview
├── GLOSSARY.md                  Domain terms: Brand, Device, JWT, MapStruct, Flyway, etc.
├── ARCHITECTURE.md              Module map, layer responsibilities, invariants, data flow
├── PLANS.md                     Full ExecPlan specification + best practices
├── SETUP.md                     This file; getting started guide
└── execplans/
    ├── README.md                ExecPlans guide; naming, best practices
    ├── TEMPLATE.md              Copy this to create new features
    └── EXAMPLE_*.md             Full worked example
```

---

**Last updated**: 2026-07-14  
**Version**: 1.0  
**Purpose**: Onboarding guide for the harness-engineering documentation structure

