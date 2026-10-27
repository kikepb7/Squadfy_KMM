# Kit de tiendas de Squadfy (2026-10)

Todo lo necesario para crear las fichas de Google Play y App Store.

- **Textos** (en este repositorio): `docs/store/textos/`.
- **Gráficos:** se generan con `docs/store/generador/` y se entregan en `Squadfy-Store-Kit.zip`. No se versionan los PNG.

## Contenido del zip
| Carpeta | Qué hay | Medidas |
|---|---|---|
| `iconos/` | Icono de Play y de App Store, sin transparencia | 512 × 512 y 1024 × 1024 |
| `graficos/` | Gráfico destacado de Play (ES y EN) | 1024 × 500 |
| `capturas/google-play/{es,en}/` | 7 capturas con titular | 1080 × 1920 (9:16) |
| `capturas/iphone-6.9/{es,en}/` | 7 capturas con titular, sin el chat (D-13) | 1320 × 2868 |
| `capturas/iphone-6.5/{es,en}/` | 7 capturas con titular, sin el chat | 1284 × 2778 |
| `textos/` | Fichas ES y EN de las dos tiendas, privacidad, clasificación y notas para la revisión | — |
| `generador/` | Plantillas HTML y el script de Playwright para regenerarlo todo | — |

**Orden de las capturas:**
1. Partido;
2. Equipos;
3. Clasificación por temporada;
4. Acta;
5. Horario con el selector de hora;
6. Semana cancelada con su motivo;
7. Unirse con código.

Las tres primeras son las que más se ven en las búsquedas.

## Importante antes de subirlas
- **Las pantallas son recreaciones fieles del diseño «Triangulación» con datos ficticios** (club «Albaicín FC», jugadores inventados). Apple (guía 2.3.3) y Google piden que las capturas muestren la app real.
  - Antes de enviar, sustituye la pantalla interior por **capturas reales de la app PRO** con la cuenta de demo. Las tomas al mismo tamaño: 393 × 852 pt en un iPhone 15/16 Pro, o 1080 × 2340 en Android.
  - Mantén los titulares y el marco: el generador acepta cualquier imagen en lugar de la pantalla.
- Las funciones tras feature flag apagadas en PRO no aparecen en los textos ni en las capturas: invitados, ausencias, hora de sorteo y marcador manual.
- **Hosting** tiene que estar desplegado: la política de privacidad y la página de borrado de cuenta viven allí.
- Play exige una prueba cerrada con 12 testers durante 14 días si la cuenta de desarrollador es personal y nueva.

## Regenerar
```bash
cd docs/store/generador
node render.mjs /ruta/de/salida        # necesita Playwright y Chromium
```
Los colores y las fuentes son los de `core/designsystem` (Tiza; Bricolage Grotesque e Instrument Sans).
