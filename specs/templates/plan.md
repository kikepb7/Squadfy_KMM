# NNN · Plan técnico

- **Spec:** ./spec.md (tiene que estar en estado `Approved`)
- **Rama:** `feature/NNN-slug` (cliente) · `feature/NNN-slug` (backend, si aplica)

## Cumplimiento de la constitución
| Principio | Cumple | Nota |
|---|---|---|
| I. Servidor autoritativo | ✅/⚠️ | |
| II. Capas y MVI | | |
| III. Calidad (tests, migraciones, strings) | | |
| IV. Seguridad | | |
| V. Contrato | | |

## Diseño por capa (cliente)
### domain
<Modelos y enums, interfaces de repositorio, casos de uso con su lógica, errores tipados.>
### data / database
<DTOs, mappers, endpoints, caché en Room, migración `N → N+1`.>
### presentation
<State, Action y Event, ViewModel, pantallas y componentes, navegación, strings.>

## Backend (Squadfy_Backend)
<Entidades, migración Flyway, servicio, controlador, autorización, jobs y tests.>

## Cambios de contrato
<Diff sobre `contracts/api-v1.md`, más la entrada nueva en su historial.>

## Estrategia de test
| AC | Nivel | Test |
|---|---|---|
| AC-NNN-01 | domain unit | `XxxUseCaseTest.AC-NNN-01 …` |

## Riesgos y mitigación
- …
