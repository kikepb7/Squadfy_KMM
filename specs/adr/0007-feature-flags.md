# ADR-0007 · Feature flags por entorno (PRE / PRO)

- **Estado:** Aceptado (pedido por el owner del producto el 2026-10-07)
- **Fecha:** 2026-10-07

## Contexto
Hay funcionalidades que el producto quiere conservar pero que aún no están disponibles:
- unas dependen de que el backend las publique (invitados, excepciones, `drawTime`, marcador manual);
- otras están a medio construir o son herramientas de desarrollo (partido de prueba, mocks de Inicio).

Borrarlas obliga a rehacerlas, y dejarlas visibles rompe la experiencia en producción. Se necesita activarlas o desactivarlas **por entorno** (PRE para QA, PRO para usuarios) sin tocar el código de la funcionalidad.

## Opciones
1. **Flags en tiempo de compilación por entorno.** Simple, sin dependencias y reproducible. Activar algo en PRO exige publicar una release.
2. **Flags remotos** (Firebase Remote Config o un endpoint del backend). Se cambian sin release, pero añaden una dependencia y un estado de red, y necesitan valores por defecto igualmente.
3. **Capas**: valor por defecto por entorno (1), más remoto (2), más override local en PRE para QA.

## Decisión
**Opción 3, por fases:**
- **Ahora:**
  - catálogo tipado `FeatureFlag` en `core/domain`, con un valor por defecto para PRE y otro para PRO;
  - entorno de compilación `SQUADFY_ENV` (`pre` o `pro`) expuesto por BuildKonfig;
  - **overrides locales persistidos en DataStore, solo efectivos en PRE**;
  - pantalla de depuración «Feature flags», solo en PRE.
- **Más adelante:** una interfaz `RemoteFeatureFlagSource` ya preparada (con una implementación `NoOp`), para conectar Firebase Remote Config o un endpoint del backend sin tocar a los consumidores (decisión D-10).

**Resolución:** override (solo PRE) → remoto → valor por defecto del entorno (APP-RN-16).

**Consumo:**
- Los ViewModels reciben `FeatureFlags` por Koin y exponen los flags en su `State`.
- La UI no lee flags directamente, salvo en el gate de navegación.
- Con un flag apagado no se hacen llamadas de red (APP-RN-17).

**Ciclo de vida de un flag:**
1. Se crea al empezar la feature, con `pre = false, pro = false`.
2. Pasa a `pre = true` cuando está lista para QA.
3. Pasa a `pro = true` cuando la spec está `Done`.
4. **Se elimina** (flag y ramas de código) como mucho una release después, para no acumular deuda.

## Consecuencias
- ➕ Nada se borra para ocultarlo. QA prueba en PRE activando flags sin recompilar.
- ➕ Una build PRO es determinista: no tiene overrides ni pantalla de depuración.
- ➖ Mientras no exista la capa remota, activar algo en PRO requiere una release.
- ➖ Cada flag añade ramas de código; el paso 4 del ciclo de vida limita la deuda.
