# ExecPlans Directory

This directory stores **executable feature specifications** for the Eletro Longhi project. Each ExecPlan documents a significant feature, refactor, or architectural change from planning through completion.

## 📋 What's Here

| File | Status | Purpose |
|------|--------|---------|
| **TEMPLATE.md** | Template | Copy this when starting a new feature; follow the structure. |
| **EXAMPLE_2025-07-14-repair-order-status-filter.md** | Example | Demonstrates how to fill out an ExecPlan (not implemented). |

---

## 🚀 How to Use

### Create a New ExecPlan
1. Copy `TEMPLATE.md` to a new file: `YYYY-MM-DD-feature-name.md` (e.g., `2025-07-20-device-pagination.md`).
2. Fill in the sections as you work.
3. Update it live; it's a working document, not final.
4. Commit to git with your feature branch.

### Example Workflow
```bash
# Create new ExecPlan
cp TEMPLATE.md 2025-07-20-add-device-color.md

# Edit it, commit
git add .agents/execplans/2025-07-20-add-device-color.md
git commit -m "ExecPlan: Add device color field"

# As you work, update Progress, Discoveries, Decisions
# Commit updates frequently
git add .agents/execplans/2025-07-20-add-device-color.md
git commit -m "ExecPlan update: Repository query implemented"

# When done, finalize Outcomes & Retrospective
git add .agents/execplans/2025-07-20-add-device-color.md
git commit -m "ExecPlan complete: Device color feature shipped"
```

---

## 📚 Naming Convention

Use the format: `YYYY-MM-DD-feature-name.md`

Examples:
- `2025-07-14-repair-order-search.md` — Add search/filter to repair orders
- `2025-07-15-device-pagination.md` — Paginate device list endpoint
- `2025-07-20-jwt-token-refresh.md` — Implement token refresh mechanism
- `2025-08-01-customer-reporting.md` — Customer reporting dashboard

---

## ✅ Quick Checklist: When to Write an ExecPlan

Write an ExecPlan if your work involves:
- [ ] Adding a new entity or entity field
- [ ] Creating new endpoints or changing existing ones
- [ ] Cross-module changes (affects 3+ modules)
- [ ] Database migrations or schema changes
- [ ] Security or authentication changes
- [ ] Performance optimization
- [ ] Significant refactoring

**Skip ExecPlans for:**
- Bug fixes (1–2 line changes)
- Comment/documentation tweaks
- Single-file edits

---

## 📖 ExecPlan Sections (Quick Reference)

| Section | What to Fill | Effort |
|---------|---------|--------|
| **Title & Goal** | 1-2 sentence what users can do | 2 min |
| **Big Picture** | Current state → desired state | 5 min |
| **Progress** | Checklist of work items | Updated live |
| **Discoveries** | Surprises, unexpected findings | Updated live |
| **Decision Log** | Every design choice + rationale | Updated live |
| **Context** | Key files, assumptions | 5 min |
| **Plan of Work** | Step-by-step implementation | 10 min |
| **Concrete Steps** | Exact commands + expected output | 10 min |
| **Validation** | How to verify it works | 5 min |
| **Outcomes** | Summary, lessons, retrospective | Filled at end |

**Total effort**: 30–40 min upfront; updated during work; 10 min to finalize.

---

## 🎯 Best Practices

### 1. **Update Frequently**
- Don't wait until the end; update Progress/Discoveries as you go.
- Helps future agents understand your decisions.

### 2. **Be Specific**
- Don't write "Update database"; write "Add `status VARCHAR(50)` column to `devices` table via V8__*.sql".
- Include file paths, method names, field names.

### 3. **Capture Surprises**
- Found an edge case? Add it to Discoveries.
- Found a shortcut? Document it in Decisions.
- These help future agents avoid the same pitfalls.

### 4. **Link to Docs**
- Reference `GLOSSARY.md` for terms.
- Reference `ARCHITECTURE.md` for structure.
- Helps agents navigate quickly.

### 5. **Make Commands Copy-Paste Ready**
- "Concrete Steps" should be runnable verbatim.
- Include expected output so agent can verify.

---

## 💡 Tips & Tricks

### **Unsure If Your Feature Needs an ExecPlan?**
Ask yourself: "Would a non-technical observer understand what work was done?" If yes, you probably need an ExecPlan.

### **Stuck on a Decision?**
- Write it in the Decision Log with your reasoning.
- Future agents can learn from it.

### **Found a Bug While Working?**
- Add to "Surprises & Discoveries" section.
- Create a separate ExecPlan for the bug fix (or note it as future work).

### **Running Out of Time?**
- Update "Outcomes & Retrospective" with what's left.
- Next agent picks up from there.

---

## 🔗 Related Docs

- `.agents/README.md` — Main agent documentation index
- `.agents/PLANS.md` — Full ExecPlan specification
- `.agents/ARCHITECTURE.md` — Where code belongs
- `.agents/GLOSSARY.md` — Domain terminology

---

## 📊 Example: View Recent ExecPlans

```bash
# List all ExecPlans by date (newest first)
ls -ltr .agents/execplans/*.md | tail -10

# View latest ExecPlan
cat .agents/execplans/2025-07-14-*.md
```

---

## 🚀 Ready to Start?

1. Copy `TEMPLATE.md` to a new file with your feature name.
2. Fill in the top sections (Goal, Big Picture, Context).
3. Start coding; update Progress/Discoveries/Decisions as you go.
4. At the end, finalize Outcomes & Retrospective.
5. Commit and merge.

**Happy shipping! 🎉**

---

**Last updated**: 2026-07-14  
**Version**: 1.0

