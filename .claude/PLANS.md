# ExecPlans — Specification for Complex Feature Work

An ExecPlan is a self-contained working document for a feature or refactor that's big enough to benefit from writing the plan down: it captures the goal, the plan, decisions made along the way, and what shipped. Store one per non-trivial feature in `.claude/execplans/YYYY-MM-DD-feature-name.md`, copied from `.claude/execplans/TEMPLATE.md`.

## When to write one

Write an ExecPlan for:
- New entities or endpoints
- Changes crossing 3+ modules (controller + service + repository, etc.)
- Database migrations
- Auth/security changes
- Significant refactors

Skip it for trivial bug fixes, doc-only changes, or single-file tweaks.

## Sections

1. **Goal** — 1-2 sentences: what can a user do after this ships?
2. **Big picture** — current state, desired state, why now, affected modules.
3. **Progress checklist** — track work items as you go.
4. **Surprises & discoveries** — timestamped notes on anything unexpected.
5. **Decision log** — table of `date | decision | rationale`. This is the part future agents actually reread — write it like you're explaining a code-review comment, not filling in a form.
6. **Context & orientation** — assumptions, key files, related ExecPlans/docs.
7. **Plan of work** — concrete file-by-file steps.
8. **Concrete steps** — copy-paste-ready commands with expected output.
9. **Validation & acceptance** — criteria and test scenarios.
10. **Outcomes & retrospective** — what shipped, what was learned, what's left.

## Working with an ExecPlan

- Update it live — Progress/Discoveries/Decisions as you go, not just at the end.
- Reference exact files and methods, not vague descriptions ("add `status VARCHAR(50)` via `V14__*.sql`", not "update the database").
- Cross-link `.claude/GLOSSARY.md` and `.claude/ARCHITECTURE.md` for domain/structure terms instead of restating them.
- If you find something that changes the shared docs (a new invariant, a corrected assumption), update `.claude/ARCHITECTURE.md` or `.claude/GLOSSARY.md` in the same change — don't leave it stranded only in the ExecPlan.
