> **Actualizado en `docs/store/` (2026-10-27)**: textos para la variante Firebase (temporadas, semanas especiales con motivo, Google y Apple), privacidad con Firebase y el generador de capturas. Este documento queda como histórico.

# Kit de tienda

Complementa a `specs/features/012-go-live/` del repositorio (`store-listing.md`, `privacy-data.md`, `release-checklist.md`), que ya cubre secretos, Firebase, cuenta de demo y smoke test. Aquí están los textos pulidos, los gráficos y las medidas.

## Identidad en las tiendas
| Campo | Español | Inglés |
|---|---|---|
| Nombre en la tienda (≤ 30) | `Squadfy: tu fútbol semanal` (26) | `Squadfy: weekly football` (24) |
| Subtítulo App Store (≤ 30) | `Tu fútbol semanal, organizado` (29) | `Your weekly football, sorted` (28) |
| Descripción corta Play (≤ 80) | `Organiza el partido semanal de tu equipo: convocatoria, equipos y estadísticas.` (79) | `Run your team's weekly match: sign-ups, balanced teams and stats.` (65) |
| Texto promocional App Store (≤ 170) | `Crea tu club, abre la convocatoria y deja que Squadfy haga los equipos. Sin hojas de cálculo ni mensajes perdidos.` (115) | `Create your club, open sign-ups and let Squadfy draw the teams. No spreadsheets, no lost messages.` (98) |
| Palabras clave App Store (≤ 100) | `fútbol,futbol,amateur,partido,equipo,convocatoria,pachanga,fútbol 7,sorteo,estadísticas,club` | `football,soccer,amateur,match,team,sign up,pickup,5-a-side,7-a-side,team picker,stats,club` |

## Descripción larga (ES)
Squadfy organiza el partido semanal de tu grupo de fútbol amateur, sin hojas de cálculo ni mensajes perdidos.

**Tu semana, en un vistazo**
• Crea tu club e invita a tus compañeros con un código de 8 caracteres.
• Define el horario: la convocatoria se abre y se cierra sola.
• Apúntate en un toque. Si se llena, entras en la lista de espera y subes automáticamente cuando alguien se borra.

**Equipos parejos, sin discutir**
• Sorteo automático por posición y nivel de cada jugador.
• Mira a los dos equipos sobre el campo y su equilibrio.
• Los gestores pueden rectificar los equipos a mano.

**Resultados y clasificación**
• Registra goles, asistencias, tarjetas y minutos.
• Rating automático por club y clasificaciones de goleadores y asistentes.
• Tu ficha de jugador con evolución, forma y estadísticas.

**Siempre al día**
• Avisos cuando se abre la convocatoria, se publican los equipos o te toca plaza.
• Marca tus ausencias y silencia clubs cuando quieras.
• Elimina tu cuenta desde la app en cualquier momento.

## Full description (EN)
Squadfy runs your amateur football group's weekly match, with no spreadsheets and no lost messages.

**Your week at a glance**
• Create your club and invite teammates with an 8-character code.
• Set the weekly schedule: sign-ups open and close on their own.
• Join in one tap. When it's full you go on the waiting list and move up automatically when someone drops out.

**Fair teams, no arguing**
• Automatic draw by each player's position and level.
• See both teams on the pitch, and how balanced they are.
• Managers can adjust the teams by hand.

**Results and standings**
• Log goals, assists, cards and minutes.
• Automatic rating per club, plus scorer and assist tables.
• Your player card with trend, form and stats.

**Always up to date**
• Notifications when sign-ups open, teams are published or you get a spot.
• Mark your absences and mute clubs whenever you like.
• Delete your account from the app at any time.

> Los textos se han escrito con las funciones que están marcadas como hechas en el roadmap. Las funciones tras feature flag (invitados, excepciones de calendario, hora de sorteo, marcador manual y ausencias) están apagadas en PRO: **quita las líneas de ausencias y de rectificación manual de los equipos si no se activan antes del envío**. Mantén el chat fuera de las capturas de iOS mientras D-13 esté abierta.

## Notas de la versión 1.0
ES: `Primera versión de Squadfy: crea tu club, apúntate al partido semanal y consulta equipos, resultados y clasificación.`
EN: `First release of Squadfy: create your club, join the weekly match and check teams, results and standings.`

## Categorías y clasificación
- Play: Deportes. App Store: principal Deportes, secundaria Redes sociales.
- Edad: decisión tuya (pendiente en el repo). Con chat entre usuarios, lo prudente es **16+** (Play: público objetivo 16+ o 18+; App Store: probablemente 12+ por contenido generado por usuarios).
- Datos: ver `privacy-data.md` (no hay analítica, publicidad ni seguimiento; Crashlytics solo Android y con consentimiento).

## Gráficos incluidos (`store/`)
| Recurso | Medida | Fichero |
|---|---|---|
| Icono Play | 512×512 | `icons/play-icon-512.png` |
| Icono App Store | 1024×1024, sin transparencia | `icons/appstore-icon-1024.png` |
| Capas adaptativas Android | 1024×1024 | `icons/android-adaptive-*.png`, `android-monochrome-1024.png` |
| Gráfico destacado Play | 1024×500 | `graphics/play-feature-graphic-1024x500-{es,en}.png` |
| Capturas con titular, Play | 1080×1920 | `promo/play/{es,en}/` (8) |
| Capturas con titular, iPhone 6.9" | 1320×2868 | `promo/ios69/{es,en}/` (8) |
| Capturas con titular, iPhone 6.5" | 1284×2778 | `promo/ios65/{es,en}/` (8) |
| Pantallas sin marco | mismas medidas | `../screens/{play,ios69,ios65}/tiza/{es,en}/` |

Orden recomendado de las capturas: Inicio · Partido · Equipos · Campo · Acta · Clasificación · Ficha · Unirse. En App Store, solo se cargan las que se ven mejor en tu primera revisión (las 3 primeras son las que más se ven en la búsqueda).

## Antes de subirlo
- Estas capturas son maquetas del diseño. Cuando la app aplique el diseño, **repite las capturas con la cuenta de demo** (`release-checklist.md` §3) y reutiliza los marcos y titulares.
- Faltan por tu parte: URL de política de privacidad, email y web de soporte, tablet de 7" y 10" si quieres aparecer en tablets, y vídeo promocional (opcional).
- Play exige una versión de prueba cerrada con 12 testers durante 14 días si la cuenta es personal nueva.
