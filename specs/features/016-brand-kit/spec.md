# 016 · Identidad de marca (kit de Squadfy)

- **Estado:** Done (2026-10-08). El owner aportó el kit y pidió aplicarlo en una rama nueva.
- **Fuente:** kit de marca `squadfy-brand-kit.zip`. Lo reutilizable está en `docs/brand/`: LEEME, `tokens.json`, SVG del logo y de los iconos, icono de Play 512 y gráfico destacado 1024×500.

## Objetivo
Aplicar la identidad del kit en toda la app: iconos, splash, logo en la interfaz, paleta y tipografía.

## Criterios de aceptación
- **AC-016-01** Iconos de Android: adaptativo (fondo Night Pitch y escudo), monocromo (Android 13+) y redondo; icono de notificación con la silueta del escudo.
- **AC-016-02** Splash de Android 12+ e iOS: el escudo sobre Night Pitch `#0A2E22`.
- **AC-016-03** iOS: AppIcon 1024 con las variantes oscura y tintada de iOS 18, y AccentColor Squad Green.
- **AC-016-04** Logo en la app: isotipo vectorial, en versión color sobre fondos claros y reverse sobre oscuros.
- **AC-016-05** Paleta del kit en el design system: Night Pitch, Squad Green, Peto Lime, Chalk e Ink, neutros verdosos y rojo de error. Todos los pares de texto cumplen AA (≥ 4,5:1) en los dos temas. La lima nunca se usa como texto sobre fondo claro.
- **AC-016-06** Tipografía Poppins: Bold para titulares, Medium para la interfaz y Regular para el texto. La licencia OFL está anotada en `core/designsystem/FONTS.md`.

## Decisiones
- Los nombres de los tokens (`SquadfyBrand*`, `SquadfyBase*`, `SquadfyRed*`) se mantienen y solo cambian sus valores, así que todos los usos adoptan la paleta sin tocar las pantallas.
- El rojo del kit (`#D93636`) se oscurece a `#CC3131` en el tema claro para cumplir AA sobre Chalk. En el oscuro se usa `#F9A3A3`.
- **Tema oscuro (petición del owner, 2026-10-08):** los fondos y superficies vuelven a los tonos azul pizarra originales (`#101C28`, `#1C2A39`, `#2F3F4F`) mediante los tokens `SquadfyDark*`. Los acentos de marca (lima y Squad Green), el tema claro y el splash siguen el kit. La auditoría de contraste sigue en verde: el peor par del tema oscuro está en 5,21:1.
- El degradado del banner del club queda Night Pitch → Squad Green, para que el texto blanco cumpla AA.
