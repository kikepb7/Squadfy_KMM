# ADR-0009 · Modelo de ramas y versiones

- **Estado:** Aceptado (lo pide el owner el 2026-10-08)
- **Fecha:** 2026-10-08

## Contexto
Hasta ahora todo se integraba en `main`, que además disparaba el CI. Con la app a punto de publicarse hace falta:
- una rama que represente exactamente lo que hay en producción;
- un sitio donde integrar features sin bloquear un parche urgente;
- un periodo de estabilización por versión (QA en Play Internal y TestFlight, revisión de tiendas) en el que solo entren correcciones.

## Decisión
Git Flow simplificado, con PRs y el CI como guardia:

| Rama | Sale de | Entra en (PR) | Contenido |
|---|---|---|---|
| `main` | — | — | **Producción.** Cada merge se etiqueta `vX.Y.Z` |
| `develop` | `main` (una vez) | — | Integración; siempre en verde. Rama por defecto del repo |
| `feature/NNN-slug` | `develop` | `develop` | Una spec o una tarea (SDD) |
| `fix/slug` | `develop` o `release/X.Y.Z` | la rama de la que sale | Corrección |
| `chore/slug` | `develop` | `develop` | CI, dependencias, documentación |
| `release/X.Y.Z` | `develop` | `main`, y después `develop` | Estabilización de una versión: solo `fix/*` |
| `hotfix/X.Y.Z` | `main` | `main`, y después `develop` | Parche urgente de producción |

- **Versiones:** SemVer. La versión vive en `gradle/libs.versions.toml` (`projectVersionName`) y en `iosApp/Configuration/Config.xcconfig` (`MARKETING_VERSION`), y `scripts/start-release.sh` las sube a la vez. El `versionCode` lo pone el CI (`github.run_number`), así que siempre crece.
- **Builds de release:**
  - candidato de QA: el workflow `Release` lanzado a mano sobre `release/X.Y.Z` o `hotfix/X.Y.Z` sube un borrador a Play Internal;
  - producción: el tag `vX.Y.Z` sobre `main` construye la AAB que se promociona en Play Console.

  El job `verify` comprueba que el tag o la rama coinciden con la versión, y que el tag está en `main`, antes de pedir la aprobación del Environment `production` (ADR-0008).
- **Guardias en el CI** (job `branch-policy` en cada PR): a `main` solo llegan `release/X.Y.Z` y `hotfix/X.Y.Z`; a `release/*` solo `fix/*`; a `develop`, `feature|fix|chore|release|hotfix/*`. En los PRs de release y hotfix se comprueba además que la versión ya esté subida.
- **Merges:** *merge commit* (sin squash), para conservar los commits `ÁREA | NNN · T-xxx` que enlazan cada cambio con su tarea SDD.

## Flujo
```bash
# Feature
git switch develop && git pull && git switch -c feature/015-slug
# … commits … → PR feature/015-slug → develop

# Release
scripts/start-release.sh release 1.0.0     # release/1.0.0 desde develop, con la versión subida
git push -u origin release/1.0.0           # Actions › Release › Run workflow (rama release/1.0.0) → QA en Play Internal
# correcciones: fix/* → PR a release/1.0.0
# PR release/1.0.0 → main → merge
git switch main && git pull && git tag v1.0.0 && git push origin v1.0.0   # build de producción
# PR release/1.0.0 → develop (back-merge) y borrar la rama de release

# Hotfix
scripts/start-release.sh hotfix 1.0.1      # hotfix/1.0.1 desde main
# fix + PR → main, tag v1.0.1, PR → develop
```

## Configuración en GitHub (manual, la hace el owner)
- **Rama por defecto:** `develop` (Settings › General).
- **Rulesets o protección** de `main` y `develop`: exigir PR; exigir el check `🧾 CI Summary` (y `🌿 Branch policy` en `main`); bloquear force push y borrado.
- **Environment `production`:** reglas de despliegue para las ramas `release/*` y `hotfix/*` y los tags `v*`.

## Consecuencias
- `main` siempre es desplegable y tiene una etiqueta por versión publicada.
- Hay que hacer el back-merge tras cada release o hotfix. Si se olvida, `develop` pierde los parches.
- Las ramas antiguas (`*-feature`) quedan como históricas; las ya fusionadas se pueden borrar.
