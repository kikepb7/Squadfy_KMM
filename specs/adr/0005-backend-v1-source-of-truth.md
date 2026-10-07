# ADR-0005 · El backend v1 es la fuente de verdad del dominio y del contrato

- **Estado:** Aceptado (2026-10-07). Reemplaza a ADR-0001 y ADR-0004.
- **Fecha:** 2026-10-07

## Contexto
Entre el 5 y el 7 de octubre de 2026, `Squadfy_Backend` completó su propio SDD (specs BE-001…BE-007, todas en estado «Hecha»):
- API versionada en `/api/v1`;
- autorización por club;
- Flyway, ~97 tests con Testcontainers;
- convocatoria con lista de espera;
- sorteo automático equilibrado con un rating Elo;
- resultado basado en eventos, estadísticas derivadas y push.

Está documentado en `../Squadfy_Backend/docs/BACKEND.md` y su contrato ejecutable es OpenAPI (`/v3/api-docs`, perfil `dev`).

La app sigue usando las rutas antiguas (`/api/club/...`), que **ya no existen**, e implementa funcionalidades que el backend decidió no ofrecer:
- invitados;
- excepciones de calendario;
- `drawTime` configurable;
- valoración 1–99 asignada por un admin;
- resultado por marcador;
- foto por club.

## Decisión
1. **El backend define las reglas de negocio y el contrato.** La app las consume y no las redefine:
   - `specs/product/business-rules.md` deja de ser normativo para el dominio y pasa a ser un **espejo con referencias** a las reglas del backend (`BE-002 RN-5`…);
   - en la app solo hay reglas propias de **UX y cliente** (`APP-RN-xx`).
2. **La API se sigue documentando en el backend.** `specs/contracts/api-v1.md` es el **mapa de consumo** de la app: qué endpoint usa cada repositorio y cada pantalla, y qué DTOs cambian. Si hay discrepancia, gana `BACKEND.md` o el OpenAPI.
3. **Si la app necesita algo nuevo del backend**, se abre una spec en `Squadfy_Backend/specs/` siguiendo su SDD, y la spec de la app la referencia como dependencia. La app no implementa lógica de dominio para suplir un endpoint que falta.
4. **Las funciones de la app sin soporte en el backend** se mantienen o se retiran según la decisión D-1 (resuelta el 2026-10-07). Invitados, excepciones, `drawTime` y marcador manual **se mantienen**: el backend las implementa y la app las oculta tras feature flags (ADR-0007) hasta que estén disponibles. Las demás se retiran.
5. **El sorteo** se hace solo en el servidor. El algoritmo propio del cliente (ADR-0004) se descarta. El cliente muestra el equilibrio que calcula el backend (`team-balance`).

## Consecuencias
- ➕ Una única fuente de verdad, ya implementada y probada. La app tiene que hacer una migración, no un rediseño.
- ➕ La lógica de cliente sin respaldo en el servidor ya no se expone en producción: se oculta tras un flag (invitados, excepciones, `drawTime`, marcador manual) o se elimina (índice de rendimiento, foto por club).
- ➖ Hay que migrar casi todo el data layer de la app (spec 002) y adaptar los modelos (las estadísticas salen del miembro; el email desaparece).
- Los cambios de contrato del backend se detectan con fixtures JSON copiados de `BACKEND.md` y del OpenAPI en los tests de mappers de la app (spec 002).
