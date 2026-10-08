# Roadmap hasta producción

> Revisado el 2026-10-08 (antes, el 2026-10-07) a partir de `Squadfy_Backend/docs/BACKEND.md`. El backend v1 está **completo** (BE-001…BE-007) y es la fuente de verdad (ADR-0005). El trabajo restante es sobre todo **migrar y completar la app**, junto con 3 piezas de backend necesarias para publicar.

Fases de cada spec: `Draft → Approved → In progress → Done`. Solo el owner del producto pasa una spec a `Approved`.

## Specs de la app
| # | Feature | Fase | Spec | Plan | Tasks | Depende de | Estimación* |
|---|---|---|---|---|---|---|---|
| 001 | Base de calidad (build, tests, CI) | **In progress** (9/16) | ✅ | — | ✅ | — | 1 d restante |
| 002 | Base de la API v1: red, errores, auth, perfil, chat, dispositivos | **In progress** (14/16; falta el E2E con el backend y `Clock`, que pasa a la 005) | ✅ | ✅ | ✅ | 001 | 4 d |
| 003 | Clubes y membresía (roles, vetos, transferencia, Room v3) | **In progress** (15/16; falta el E2E con dos usuarios) | ✅ | ✅ | ✅ | 002 | 5 d |
| 004 | Horario semanal (+ cierre/sorteo y semanas especiales) | **In progress** (7/8; falta el E2E) | ✅ | — | ✅ | 003 | — |
| 005 | Convocatoria vigente, lista de espera e invitados | **Done** (11/11) | ✅ | ✅ | ✅ | 003 | — |
| 006 | Detalle del partido, equipos y equilibrio | **Done** (9/9) | ✅ | — | ✅ | 005 | — |
| 007 | Acta: eventos, minutos, cerrar, reabrir, cancelar, partido extra | **Done** (10/10) | ✅ | — | ✅ | 006 | — |
| 008 | Clasificaciones de rating y estadísticas | **Done** (8/8) | ✅ | — | ✅ | 003 | — |
| 009 | Push del ciclo de partido + silenciar club | **In progress** (9/11; faltan el E2E con push reales y la clave APNs) | ✅ | — | ✅ | 002, 005, 006 | — |
| 010 | Inicio con mis clubes y el estado de la convocatoria | **Done** (7/7) | ✅ | — | ✅ | 003, 005 | — |
| 011 | Preparación de la app para producción | **In progress** (12/13; falta T-013, manual del owner) | ✅ | ✅ | ✅ | 002 (en paralelo) | 5 d |
| 012 | Puesta en producción (coordinada con el backend) | Draft (lado app documentado) | ✅ | ✅ | ✅ | todas, BE-008/009/010 | 3 d + revisión de tiendas |
| 013 | Feature flags por entorno (PRE / PRO) | **Done** | ✅ | ✅ | ✅ | 001 | — |
| 014 | Ausencias de jugadores | **Done** (7/7) | ✅ | — | ✅ | 003, 005 | — |
| 015 | Cierre del MVP y paridad con el backend 011–013 | **Done** (11/11) | ✅ | — | ✅ | 002–014 | 3 d |

\* Días ideales de una persona. Son orientativos, para priorizar, no un compromiso.

## Specs que se necesitan en el backend (a abrir en `Squadfy_Backend/specs/`)
| Propuesta | Origen | Bloquea |
|---|---|---|
| ~~**Borrado de cuenta**~~ **Hecho en el backend (spec 010, rama `account-deletion-feature`)**: `DELETE /me` con contraseña, anonimización y página web `/account/delete`. La app lo consume tras `ACCOUNT_DELETION` | BE-GAP-1 | 012 (fusionar y desplegar) |
| ~~**Rate limit del refresh**~~ **Hecho en el backend (spec 010)**: límites por cuenta y 429 con `Retry-After` | BE-GAP-2 | 012 (fusionar y desplegar) |
| **BE-010 Despliegue**: hosting, dominio, CD, backups (cierra BE-006) | BE-GAP-3 | 012 |
| ~~Invitados, excepciones, ausencias, `drawTime`, marcador manual~~ **Hecho en el backend (BE-008, 2026-10-07)** | D-1 | La app los conecta en 004, 005, 006, 007 y 014 |
| ~~Merge de `backend-documentation` → `master`~~ ✅ | BE-GAP-7 | — |
| (Opcional) Página de confirmación de la verificación de email | BE-GAP-4 | — |
| (Opcional, post-MVP) **Atestación de la app**: Play Integrity y App Attest verificados en el servidor, si hace falta frenar clientes que no sean la app (ADR-0008) | — | — |

## Hitos
| Hito | Contenido | Criterio de salida |
|---|---|---|
| **M1 · App conectada** | 001 + 002 | Login, chat y perfil funcionan contra el backend v1 local; CI en verde |
| **M2 · Club gestionable** | 003 + 004 | Dos usuarios crean, se unen, gestionan roles y vetos y configuran el horario |
| **M3 · Ciclo semanal** | 005 + 006 + 007 | Se completa un ciclo en local: convocatoria → equipos automáticos → acta → cerrar |
| **M4 · Producto completo** | 008 + 009 + 010 | Clasificaciones, push y un Inicio útil |
| **M5 · Listo para tiendas** | 011 + BE-008/009 | Release firmada y minificada, borrado de cuenta, privacidad, i18n |
| **M6 · Producción** | 012 + BE-010 | Backend desplegado, Play Internal y TestFlight, beta de 4 semanas con un club real |

## Ruta crítica
```
001 ─▶ 002 ─▶ 003 ─┬─▶ 004
                   ├─▶ 005 ─▶ 006 ─▶ 007 ─┐
                   ├─▶ 008                ├─▶ 009 ─▶ 012
                   └─▶ 010 (tras 005)     │
002 ─▶ 011 (en paralelo; AC-011-07 espera a BE-008) ─▶ 012
BE-008, BE-009, BE-010 (en paralelo en el backend) ──────▶ 012
```

## Plan hasta la 1.0.0 (revisado el 2026-10-08)
**Estado:** el código del MVP está completo en `main`, con el CI en verde: specs 002–010, 013 y 014, más la 011 salvo su tarea manual T-013. Lo que falta es cerrar la calidad, tomar unas pocas decisiones, que el backend esté en producción y hacer el trabajo de consolas y tiendas. Las ramas siguen el ADR-0009, y la 1.0.0 se corta con `scripts/start-release.sh release 1.0.0` cuando el bloque A esté hecho.

**A · Código en `develop`** (ramas `feature/*`, `fix/*` y `chore/*`)
| # | Trabajo | Origen | Tamaño |
|---|---|---|---|
| A1 | ktlint en todos los módulos: `ktlintFormat` en un commit aislado y `ignoreFailures = false` | 001 T-012 | ½ d |
| A2 | Kover agregado con un gate de cobertura en el CI | 001 T-013 | ½ d |
| ~~A3~~ | ✅ D-13: chat oculto en iOS en PRO (flag `CHAT`); el Perfil sale del chat (spec 015) | 012 T-006 | — |
| ~~A4~~ | ✅ D-12: `primary` del tema claro = Brand900 (spec 015) | 011 AC-011-11 | — |
| ~~A5~~ | ✅ Mocks de Inicio eliminados, junto con sus módulos (spec 015) | 010 T-006 | — |
| A7 | ✅ Paridad con el backend 011–013: búsqueda parcial, foto por club, tiempo real, estadísticas por periodo y staging/producción (spec 015) | 015 | — |
| A6 | (Opcional) PRE instalable junto a PRO con `applicationIdSuffix = ".pre"` | D-11 | ¼ d |

**B · Backend** (`Squadfy_Backend`, su propio SDD; **bloquea la 1.0.0**)
| # | Trabajo |
|---|---|
| ~~B1~~ | ✅ La spec 010 del backend está en `master`, junto con las 011 y 012 |
| B2 | Despliegue en producción (D-4): HTTPS en un dominio propio, servicios gestionados, backups y runbook (012 AC-012-01…06) |

**C · Rama `release/1.0.0`** (solo `fix/*`)
| # | Trabajo | Origen |
|---|---|---|
| ~~C1~~ | ✅ `ACCOUNT_DELETION` activo en PRO (spec 010 del backend en `master`; E2E en verde) | 012 T-005 |
| C2 | E2E del candidato contra PRE o producción: auth y chat (002 T-016), clubes con dos usuarios (003 T-016), horario (004 T-008) y push reales (009 T-011) | QA |
| C3 | Smoke test en producción con dos dispositivos | 012 T-012 |

**D · Manual en consolas** (owner; en paralelo con A y B)
| # | Trabajo | Origen |
|---|---|---|
| D1 | GitHub: rama por defecto `develop`, protección de `main` y `develop`, Environment `production` con sus secretos | ADR-0008/0009, 012 T-008 |
| D2 | Firebase de producción: app iOS con el bundle nuevo, clave APNs, huellas SHA y restricción de las API keys | 009 T-010, 011 T-013, 012 T-009 |
| D3 | Apple: `TEAM_ID`, app en App Store Connect, TestFlight | 011 T-013 |
| D4 | Política de privacidad publicada, Data Safety, etiquetas de privacidad, clasificación, fichas, capturas y cuenta de demo | 012 T-010/T-011 |
| D5 | Closed testing en Play (12 testers, 14 días), TestFlight externo y beta de 4 semanas con un club real | 012 T-013 |

**Salida de la 1.0.0:** PR `release/1.0.0` → `main` → tag `v1.0.0` → se promociona en Play (y en App Store, según D-7) → back-merge a `develop`.

**Candidatos para la 1.1.0 y siguientes:**
- graduar los flags de BE-008 a PRO (invitados, excepciones, `drawTime`, marcador manual y ausencias; D-1);
- Crashlytics y Keychain en iOS;
- flags remotos (D-10);
- job de CI para iOS;
- atestación de la app (ADR-0008);
- el backlog post-MVP de abajo.

## Decisiones abiertas (bloquean el paso a `Approved`)
| # | Pregunta | Propuesta | Afecta a |
|---|---|---|---|
| ~~D-1~~ | **Resuelta el 2026-10-07:** se **mantienen** invitados (los añade el jugador responsable), excepciones, `drawTime` y marcador manual; el backend los implementa y la app los oculta tras feature flags hasta entonces. La valoración es automática (rating) | — | 003–008, 013 |
| ~~D-2~~ | **Resuelta el 2026-10-08 (ADR-0008):** se elimina `x-api-key`/`API_KEY`, porque el backend no la lee y no puede ser secreta en una app. Los secretos de release van en el Environment `production` | — | 002, 011 |
| D-3 | ¿Se abre BE-008 (borrado de cuenta) en el backend? | **Sí**, porque es obligatorio para publicar | 011, 012 |
| D-4 | Hosting del backend | Una opción gestionada con Docker que encaje con los servicios ya elegidos (Supabase, CloudAMQP, Redis Cloud): Fly.io, Railway o Render, en una región de la UE | 012 |
| D-5 | Bundle ID y `applicationId` canónicos (`com.kikepb.squadfy` frente a `org.kikepb.squadfy`) | `com.kikepb.squadfy` en las dos plataformas | 011, 012 |
| D-6 | Idiomas del MVP | ES por defecto con los recursos EN preparados; las push del backend solo están en ES | 011 |
| D-7 | ¿Lanzamiento simultáneo Android + iOS o primero Android? | Android (Play Internal) primero; iOS en TestFlight en paralelo | 012 |
| D-8 | ¿Se acepta el ADR-0006 (ciclo de partido network-first)? | Sí | 005–008 |
| D-9 | ¿Se muestra el rol `CAPTAIN` con un distintivo? | Sí, solo como etiqueta | 003 |
| D-10 | Capa remota de flags: ¿Firebase Remote Config o endpoint del backend? | Firebase Remote Config | 013 |
| D-11 | ¿Instalar PRE junto a PRO (`applicationIdSuffix = ".pre"`)? | Sí, cuando haya backend de PRE | 011, 013 |
| ~~D-12~~ | **Resuelta el 2026-10-08 (spec 015 T-001):** `primary` del tema claro = Brand900. Antes: tema claro: el verde de marca (`primary`, Brand500) se usa como color de texto en ~40 sitios y sobre blanco da 1,70:1 (no cumple AA). ¿Se cambia el texto de marca en claro a Brand900 (6,96:1) o se fija la app en tema oscuro? | Texto de marca en claro con Brand900 (rol propio), sin tocar los botones | 011 |
| ~~D-13~~ | **Resuelta el 2026-10-08 (spec 015 T-002):** chat oculto en iOS en PRO tras el flag `CHAT`; el Perfil sale del chat. Antes: chat con contenido generado por usuarios: Apple (guideline 1.2) pide denunciar contenido, bloquear usuarios y un contacto publicado. ¿Se añaden denuncia y bloqueo antes de enviar a App Store, o se oculta el chat en la primera versión? | Ocultar el chat tras un flag en la v1 de iOS y añadir denuncia y bloqueo después (necesita una spec del backend) | 012 |

## Backlog post-MVP
- Estadísticas por temporada.
- Tiempo real del ciclo de partido.
- App Links y Universal Links.
- Economía y cuotas.
- Chat de club.
- Widget de próximo partido.
