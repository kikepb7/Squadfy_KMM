# Squadfy · Spec-Driven Development (SDD)

En este repositorio, **ningún cambio funcional se implementa sin una spec aprobada**. La spec es la fuente de verdad. El código, los tests y el contrato de API derivan de ella y se verifican contra ella.

```
specs/
├── constitution.md          # Principios no negociables (arquitectura, calidad, seguridad)
├── roadmap.md               # Estado del MVP: qué feature está en qué fase
├── product/
│   ├── vision.md            # Qué es Squadfy y qué es el MVP
│   ├── glossary.md          # Lenguaje ubicuo (convocatoria, lista de espera, rating, gestor…)
│   └── business-rules.md    # Espejo de las reglas del backend (BE-NNN RN-x) + reglas de cliente (APP-RN-xx)
├── architecture/overview.md # Mapa de módulos cliente + backend y flujo de datos
├── contracts/
│   ├── api-v1.md            # Mapa de consumo de la API v1 del backend (rutas antiguas → nuevas, por spec)
│   └── gap-analysis.md      # Qué falta en la app y qué falta en el backend para producción
├── adr/                     # Decisiones de arquitectura (NNNN-titulo.md)
├── features/NNN-slug/       # Una carpeta por feature
│   ├── spec.md              # QUÉ y POR QUÉ: historias, criterios de aceptación (Given/When/Then)
│   ├── plan.md              # CÓMO: diseño técnico por capa/módulo, contrato, migraciones
│   └── tasks.md             # Tareas atómicas, ordenadas y verificables
└── templates/               # Plantillas para spec/plan/tasks/adr
```

## Flujo

| Fase | Entrada | Salida | Puerta (gate) |
|---|---|---|---|
| 1. **Specify** | Necesidad de producto | `features/NNN/spec.md` (estado `Draft`) | Criterios de aceptación testables, referencias a `BE-NNN RN-x` / `APP-RN-xx`, preguntas abiertas resueltas → `Approved` |
| 2. **Plan** | Spec `Approved` | `plan.md` + actualización de `contracts/api-v1.md` + ADR si hay decisión relevante | Cumple `constitution.md`; cada endpoint usado existe en `BACKEND.md` o en el OpenAPI, o hay una spec del backend abierta |
| 3. **Tasks** | Plan | `tasks.md` (T-001…), cada una ≤ ½ día, con su verificación | Cada criterio de aceptación queda cubierto por al menos una tarea con test |
| 4. **Implement** | Una tarea | Código + tests en la rama `feature/NNN-slug` | `./gradlew testDebugUnitTest` en verde y tarea marcada `[x]` |
| 5. **Verify** | Feature completa | Checklist de aceptación marcada en `spec.md`; el roadmap pasa a `Done` | Revisión (`/code-review`) y demo manual en emulador |

Reglas:
- **Primero se cambia la spec y luego el código.** Si durante la implementación aparece algo que la spec no cubre, se para, se actualiza la spec (o se abre una pregunta en ella) y se continúa.
- **Trazabilidad.** Los tests nombran el criterio que cubren (`AC-004-03`), y los commits referencian la feature: `CLUB | 004 · T-007 Enroll button respects signup window`.
- **Cambios entre repositorios.** El backend (`../Squadfy_Backend`) tiene su propio SDD (`specs/BE-NNN`) y **es la fuente de verdad del dominio y del contrato** (ADR-0005). Si una feature de la app necesita algo nuevo del backend, se abre una spec allí y la de la app la referencia como dependencia. `contracts/api-v1.md` es el mapa de consumo de la app.

Las skills de Claude Code que automatizan el flujo están en `.claude/skills/`:
- `sdd-workflow`: guía cada fase.
- `squadfy-domain`: reglas de negocio.
- `squadfy-api-contract`: cambios de contrato entre repositorios.
- `kmp-release-readiness`: preparación de la release.
