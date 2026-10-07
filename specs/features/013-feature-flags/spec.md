# 013 · Feature flags por entorno (PRE / PRO)

- **Estado:** Done (2026-10-07). La capa remota está pendiente de la decisión D-10.
- **Reglas:** APP-RN-15, APP-RN-16, APP-RN-17
- **ADRs:** ADR-0007
- **Repos afectados:** Squadfy_App

## Problema / objetivo
Poder **activar o desactivar desarrollos en PRE y en PRO** sin borrar código: funciones pendientes del backend, trabajo a medio hacer y herramientas de desarrollo.

## Historias de usuario
- **US-013-01** Como owner quiero que lo pendiente del backend (invitados, excepciones, `drawTime`, marcador manual) siga en el código pero oculto hasta que esté listo.
- **US-013-02** Como QA quiero activar o desactivar un flag en una build PRE desde la propia app, sin recompilar.
- **US-013-03** Como desarrollador quiero declarar un flag en un único sitio, con su valor para PRE y para PRO.

## Criterios de aceptación
- **AC-013-01** El entorno de compilación se elige con `SQUADFY_ENV`:
  - se lee de `-P`, de una variable de entorno o de `local.properties`;
  - acepta `pre` y `pro`, y por defecto es `pre`;
  - un valor distinto **hace fallar el build**;
  - queda expuesto como `AppEnvironment.PRE` o `AppEnvironment.PRO`.
- **AC-013-02** `FeatureFlag` es un catálogo tipado (enum) con `key`, `description`, `defaultInPre` y `defaultInPro`. Una clave duplicada se detecta con un test.
- **AC-013-03** Resolución (APP-RN-16):
  - en PRE, override → remoto → valor por defecto de PRE;
  - en PRO, remoto → valor por defecto de PRO, **ignorando los overrides**.

  Lo verifica un test de tabla del resolver puro.
- **AC-013-04** `FeatureFlags.observe(flag): Flow<Boolean>` emite al cambiar un override, y `isEnabled(flag): Boolean` devuelve el valor actual.
- **AC-013-05** Los overrides se persisten (DataStore) y sobreviven al reinicio. «Restablecer» los borra todos.
- **AC-013-06** Existe la pantalla «Feature flags» **solo en PRE**:
  - lista cada flag con su descripción, su valor efectivo y su origen (por defecto, override o remoto), y un interruptor;
  - se abre desde el botón de ajustes de Inicio mientras no exista la pantalla de Perfil (spec 010).

  En PRO la ruta no se registra y el botón no navega a ella.
- **AC-013-07** Flags iniciales:

  | Flag | PRE | PRO | Motivo |
  |---|---|---|---|
  | `MATCH_GUESTS` | **on** (desde la 005) | off | Backend BE-008; app en las specs 005/006 |
  | `SCHEDULE_EXCEPTIONS` | **on** (desde la 004) | off | Backend BE-008; app en la spec 004 |
  | `CUSTOM_DRAW_TIME` | **on** (desde la 004) | off | Backend BE-008; app en la spec 004 |
  | `MANUAL_SCORE` | **on** | off | Backend BE-008; app hecha en la spec 007 (activo en PRE para QA) |
  | `MEMBER_ABSENCES` | **on** | off | Backend BE-008; app hecha en la spec 014 (activo en PRE para QA) |
  | `DEV_TEST_MATCH` | **on** | off | Herramienta de QA (crear partido de prueba) |
  | `HOME_RECENT_MATCHES` | off | off | Hoy son mocks (spec 010) |
  | `HOME_NEWS` | off | off | Hoy son mocks; fuera del MVP |

  Al estar en off, los cuatro flags pendientes del backend mantienen la UI y el código actuales ocultos.
- **AC-013-08** La UI existente queda condicionada por los flags:
  - botón «Añadir invitado» → `MATCH_GUESTS`;
  - sección de excepciones → `SCHEDULE_EXCEPTIONS`;
  - campo de hora del sorteo → `CUSTOM_DRAW_TIME`;
  - diálogo de resultado → `MANUAL_SCORE`;
  - «Crear partido de prueba» → `DEV_TEST_MATCH`;
  - partidos recientes y noticias de Inicio → `HOME_RECENT_MATCHES` y `HOME_NEWS`.

  Con un flag apagado no se hace la llamada de red asociada (APP-RN-17): por ejemplo, el horario no pide las excepciones.
- **AC-013-09** `RemoteFeatureFlagSource` es una interfaz con una implementación `NoOpRemoteFeatureFlagSource`. Conectar Firebase Remote Config o un endpoint del backend no obliga a cambiar los consumidores.
- **AC-013-10** El CI compila y testea en PRE. El job de release de PRO (spec 011) usa `SQUADFY_ENV=pro`.

## Preguntas abiertas
- ❓ **D-10**: ¿la capa remota será Firebase Remote Config (ya hay Firebase en el proyecto) o un endpoint del backend (`GET /api/v1/config/flags`)? Propuesta: Firebase Remote Config, porque no necesita trabajo en el backend.
- ❓ **D-11**: ¿las builds PRE se instalan junto a PRO (`applicationIdSuffix = ".pre"`)? Exige registrar el paquete `.pre` en Firebase y en `FIREBASE_ANDROID_PACKAGE`. Propuesta: sí, cuando haya backend de PRE.
