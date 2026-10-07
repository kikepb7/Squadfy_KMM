# ADR-0001 · Fuente de verdad del contrato de API

- **Estado:** Reemplazado por ADR-0005 (2026-10-07): el backend completó su API v1 y pasa a ser la fuente de verdad.
- **Fecha:** 2026-10-05

## Contexto
El cliente y el backend divergieron (ver `contracts/gap-analysis.md`):
- El cliente implementa un contrato con inscripciones, invitados, resultado agregado y calendario anidado en el club.
- El backend (`match` WIP, sin commitear) implementa `/matches`, `/matchAnnouncements` y `/match-schedules`, con eventos sueltos.

Ninguno de los dos cubre el MVP completo, y el backend no tiene autorización en el módulo match.

## Opciones
1. **El cliente se adapta al backend actual.** El cliente pierde invitados, excepciones y el resultado agregado, que tendrían que añadirse igualmente al backend. Además obliga a reescribir el data layer y la UI.
2. **El backend implementa el contrato del cliente tal cual.** Es rápido, pero hereda carencias: no hay `signupState`, ni lista de espera, ni zona horaria, ni `myRole`.
3. **Contrato canónico nuevo (`contracts/api-v1.md`).** Usa las **rutas y la forma del cliente**, porque la UI ya está construida sobre ellas, y añade lo que pide el MVP. El backend **reutiliza la lógica del módulo `match`** y expone estas rutas.

## Decisión (propuesta)
**Opción 3.** `specs/contracts/api-v1.md` es la fuente de verdad, y ambos repositorios se ajustan a él:
- **Backend:** los controladores `MatchController`, `MatchAnnouncementController` y `ClubMatchScheduleController` se sustituyen por fachadas en las rutas `/club/...` del contrato. Las entidades `match_service` se mantienen y amplían (tz, `drawTime`, invitados, lista de espera, excepciones, resultado). Se añade autorización por club en todas las rutas.
- **Cliente:** los cambios de DTO son pequeños: `MatchDto` gana `signupState`, `mySignup`, contadores y `draw`, y `ClubDto` gana `myRole` y `schedule`. Los modelos de dominio pasan a usar enums.

## Consecuencias
- ➕ La UI existente se conserva. Hay un único documento que versionar, y la verificación puede ser contract-first (ver la skill `squadfy-api-contract`).
- ➖ El trabajo pesado está en el backend, que además tiene que commitear y estabilizar antes su working tree (HEAD no compila).
- Cada cambio de contrato se registra en el «Historial de cambios» de `api-v1.md`.
