# NNN · <Título de la feature>

- **Estado:** Draft | Approved | In progress | Done
- **Owner:** <persona>
- **Reglas:** BR-xxx, BR-yyy
- **ADRs:** ADR-NNNN
- **Repos afectados:** Squadfy_App | Squadfy_Backend

## Problema / objetivo
<Por qué hace falta esta feature y qué resultado de usuario se busca. Sin hablar de la solución técnica.>

## Fuera de alcance
- …

## Historias de usuario
- **US-NNN-01** Como <rol> quiero <acción> para <beneficio>.

## Criterios de aceptación
Cada AC se puede verificar y tiene al menos un test que lo cita por su ID.

- **AC-NNN-01** *Given* … *When* … *Then* …
- **AC-NNN-02** …

## Casos límite y errores
| Situación | Comportamiento esperado | Código de error |
|---|---|---|

## UX
<Pantallas afectadas y estados (cargando / vacío / error / éxito), textos y permisos por rol.>

## Contrato de API
<Endpoints de `contracts/api-v1.md` que usa o modifica. Si se modifican, describir el diff.>

## Preguntas abiertas
- ❓ …

## Checklist de verificación
- [ ] Todos los AC tienen un test que los cita
- [ ] `./gradlew testDebugUnitTest` en verde
- [ ] Demo manual en Android (y en iOS si la feature tiene UI)
- [ ] `roadmap.md` actualizado
