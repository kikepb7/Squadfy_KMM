# Roadmap hasta producción

> Revisado el 2026-10-07 a partir de `Squadfy_Backend/docs/BACKEND.md`. El backend v1 está **completo** (BE-001…BE-007) y es la fuente de verdad (ADR-0005). El trabajo restante es sobre todo **migrar y completar la app**, junto con 3 piezas de backend necesarias para publicar.

Fases de cada spec: `Draft → Approved → In progress → Done`. Solo el owner del producto pasa una spec a `Approved`.

## Specs de la app
| # | Feature | Fase | Spec | Plan | Tasks | Depende de | Estimación* |
|---|---|---|---|---|---|---|---|
| 001 | Base de calidad (build, tests, CI) | **In progress** (9/16) | ✅ | — | ✅ | — | 1 d restante |
| 002 | Base de la API v1: red, errores, auth, perfil, chat, dispositivos | **In progress** (14/16; falta el E2E con el backend y `Clock`, que pasa a la 005) | ✅ | ✅ | ✅ | 001 | 4 d |
| 003 | Clubes y membresía (roles, vetos, transferencia, Room v3) | **In progress** (fase 1 de dominio hecha) | ✅ | ✅ | ✅ | 002 | 5 d |
| 004 | Horario semanal | Draft | ✅ | — | — | 003 | 1,5 d |
| 005 | Convocatoria vigente y lista de espera | Draft | ✅ | ✅ | ✅ | 003 | 3 d |
| 006 | Detalle del partido, equipos y equilibrio | Draft | ✅ | — | — | 005 | 3 d |
| 007 | Acta: eventos, minutos, cerrar, reabrir, cancelar, partido extra | Draft | ✅ | — | — | 006 | 3 d |
| 008 | Clasificaciones de rating y estadísticas | Draft | ✅ | — | — | 003 | 2 d |
| 009 | Push del ciclo de partido + silenciar club | Draft | ✅ | — | — | 002, 005, 006 | 2,5 d |
| 010 | Inicio con mis clubes y el estado de la convocatoria | Draft | ✅ | — | — | 003, 005 | 1,5 d |
| 011 | Preparación de la app para producción | Draft | ✅ | — | — | 002 (en paralelo) | 5 d |
| 012 | Puesta en producción (coordinada con el backend) | Draft | ✅ | — | — | todas, BE-008/009/010 | 3 d + revisión de tiendas |
| 013 | Feature flags por entorno (PRE / PRO) | **Done** | ✅ | ✅ | ✅ | 001 | — |
| 014 | Ausencias de jugadores | Approved | ✅ | — | — | 003, 005 | 1 d |

\* Días ideales de una persona. Son orientativos, para priorizar, no un compromiso.

## Specs que se necesitan en el backend (a abrir en `Squadfy_Backend/specs/`)
| Propuesta | Origen | Bloquea |
|---|---|---|
| **BE-008 Borrado de cuenta**: endpoint `DELETE /me` con contraseña, anonimización del historial y página web de borrado | BE-GAP-1 (requisito de Apple y Google) | 011, 012 |
| **BE-009 Rate limit del refresh** por usuario o token en lugar de por IP | BE-GAP-2 | 012 (sesiones estables) |
| **BE-010 Despliegue**: hosting, dominio, CD, backups (cierra BE-006) | BE-GAP-3 | 012 |
| ~~Invitados, excepciones, ausencias, `drawTime`, marcador manual~~ **Hecho en el backend (BE-008, 2026-10-07)** | D-1 | La app los conecta en 004, 005, 006, 007 y 014 |
| ~~Merge de `backend-documentation` → `master`~~ ✅ | BE-GAP-7 | — |
| (Opcional) Página de confirmación de la verificación de email | BE-GAP-4 | — |

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

## Decisiones abiertas (bloquean el paso a `Approved`)
| # | Pregunta | Propuesta | Afecta a |
|---|---|---|---|
| ~~D-1~~ | **Resuelta el 2026-10-07:** se **mantienen** invitados (los añade el jugador responsable), excepciones, `drawTime` y marcador manual; el backend los implementa y la app los oculta tras feature flags hasta entonces. La valoración es automática (rating) | — | 003–008, 013 |
| D-2 | ¿Se mantiene la cabecera `x-api-key`? | Eliminarla si el backend v1 no la exige | 002 |
| D-3 | ¿Se abre BE-008 (borrado de cuenta) en el backend? | **Sí**, porque es obligatorio para publicar | 011, 012 |
| D-4 | Hosting del backend | Una opción gestionada con Docker que encaje con los servicios ya elegidos (Supabase, CloudAMQP, Redis Cloud): Fly.io, Railway o Render, en una región de la UE | 012 |
| D-5 | Bundle ID y `applicationId` canónicos (`com.kikepb.squadfy` frente a `org.kikepb.squadfy`) | `com.kikepb.squadfy` en las dos plataformas | 011, 012 |
| D-6 | Idiomas del MVP | ES por defecto con los recursos EN preparados; las push del backend solo están en ES | 011 |
| D-7 | ¿Lanzamiento simultáneo Android + iOS o primero Android? | Android (Play Internal) primero; iOS en TestFlight en paralelo | 012 |
| D-8 | ¿Se acepta el ADR-0006 (ciclo de partido network-first)? | Sí | 005–008 |
| D-9 | ¿Se muestra el rol `CAPTAIN` con un distintivo? | Sí, solo como etiqueta | 003 |
| D-10 | Capa remota de flags: ¿Firebase Remote Config o endpoint del backend? | Firebase Remote Config | 013 |
| D-11 | ¿Instalar PRE junto a PRO (`applicationIdSuffix = ".pre"`)? | Sí, cuando haya backend de PRE | 011, 013 |

## Backlog post-MVP
- Estadísticas por temporada.
- Tiempo real del ciclo de partido.
- App Links y Universal Links.
- Economía y cuotas.
- Chat de club.
- Widget de próximo partido.
