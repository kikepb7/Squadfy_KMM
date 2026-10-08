# 012 · Tareas

Leyenda: `[ ]` pendiente · `[~]` en curso · `[x]` hecha · `(B)` backend · `(M)` manual del owner en una consola

## Lado app (preparación)
- [x] **T-001** Plan del lado app y riesgos (`plan.md`). Verificación: revisado contra la spec y la 011.
- [x] **T-002** Inventario de datos para Data Safety y las etiquetas de privacidad (`privacy-data.md`), sacado del árbol de dependencias de la release y de los permisos. Verificación: `:composeApp:dependencies` muestra solo FCM y Crashlytics de Firebase, sin analítica.
- [x] **T-003** Fichas ES/EN, categoría, clasificación y recursos (`store-listing.md`).
- [x] **T-004** Checklist de secretos, Firebase, cuenta de demo y smoke test (`release-checklist.md`).
- [x] **T-005** Activar `ACCOUNT_DELETION` en PRO (`defaultInPro = true`). Hecho el 2026-10-08: la spec 010 del backend está en `master` y el borrado pasó el E2E (spec 015 T-011). El flag se mantiene como interruptor de emergencia.
- [ ] **T-006** Decisión D-13 (denunciar y bloquear en el chat, Apple 1.2) y, si procede, su spec en la app y en el backend.

## Backend (su propio SDD)
- [ ] **T-007 (B)** AC-012-01…06: hosting con HTTPS, servicios gestionados, fusionar la spec 010 del backend, backups y runbook.

## Consolas y lanzamiento
- [ ] **T-008 (M)** Secretos de GitHub y clave de subida (`release-checklist.md` §1), y primera ejecución del workflow `Release` → Internal testing.
- [ ] **T-009 (M)** Firebase de producción (§2).
- [ ] **T-010 (M)** Publicar la política de privacidad y rellenar Data Safety, las etiquetas de privacidad y la clasificación.
- [ ] **T-011 (M)** Cuenta de demo (§3) y fichas con capturas.
- [ ] **T-012 (M)** Smoke test en producción con dos dispositivos (§4, AC-012-11).
- [ ] **T-013 (M)** Closed testing en Play (12 testers, 14 días), TestFlight externo y beta con un club real durante 4 semanas (AC-012-12).
