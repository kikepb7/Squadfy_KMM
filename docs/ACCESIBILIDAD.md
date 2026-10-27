# Accesibilidad en Squadfy (auditoría de 2026-10)

Objetivo: que la app se pueda usar con **el texto del sistema al máximo** y con **TalkBack (Android) y VoiceOver (iOS)**, y cumplir el nivel **WCAG 2.2 AA**, que es la referencia de la norma europea EN 301 549 y del Acta Europea de Accesibilidad.

## 1. Qué hay que cumplir (investigación, octubre de 2026)
| Fuente | Qué pide | Cómo aplica a Squadfy |
|---|---|---|
| **European Accessibility Act** (Directiva 2019/882, exigible desde el 28/6/2025) | Productos y servicios digitales accesibles. La referencia técnica es EN 301 549, que remite a WCAG 2.1 AA (y a WCAG 2.2 en su próxima versión) | Squadfy no parece estar en los sectores obligatorios (banca, transporte, comercio electrónico…), y las microempresas (menos de 10 personas y 2 M€) están exentas; conviene confirmarlo con un asesor. Aun así, tomamos AA como estándar, porque las tiendas y los usuarios lo esperan |
| **WCAG 2.2 AA** | 1.4.3 contraste de 4,5:1 (3:1 en texto grande), 1.4.11 contraste de 3:1 en componentes, 1.4.4 texto al 200 %, 1.4.10 reflow, 2.5.8 objetivos de al menos 24 px, 4.1.2 nombre, rol y valor, 4.1.3 mensajes de estado | Se ha revisado criterio a criterio (§3) |
| **Android** | Áreas táctiles de 48 × 48 dp (Accessibility Scanner y el informe previo al lanzamiento de Play las comprueban). Desde Android 14, fuente hasta el **200 %** con escalado no lineal: el texto grande crece menos que el pequeño | El texto va en `sp`, los márgenes en `dp` y no se calcula nada con `fontScale` a mano. Se comprueba al 200 % |
| **iOS** | Dynamic Type (tamaños de accesibilidad de hasta unas 3×) y VoiceOver | Compose Multiplatform pasa la semántica de Compose a la accesibilidad nativa de iOS y VoiceOver la lee con los gestos nativos. JetBrains la describe como «en fase temprana». El árbol se sincroniza solo cuando hay un servicio de accesibilidad activo. El alto contraste (*Aumentar contraste*) no viene de serie |
| **Compose** | Los componentes de Material ya traen su semántica. Los propios necesitan `role`, `selected`, `stateDescription`, `contentDescription`, `mergeDescendants` o `clearAndSetSemantics`, `heading()`, `liveRegion` y `error()`. El orden de lectura se ajusta con `isTraversalGroup` y `traversalIndex` | El design system «Triangulación» está hecho a mano, así que la auditoría se centra en él |

Fuentes:
- [WCAG 2.2](https://www.w3.org/TR/WCAG22/)
- [EN 301 549 y EAA (Deque)](https://www.deque.com/?p=30994)
- [Android 14: fuente no lineal al 200 %](https://developer.android.com/about/versions/14/features)
- [Accesibilidad en iOS con Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-ios-accessibility.html)
- [Orden de recorrido en Compose](https://developer.android.com/develop/ui/compose/accessibility/traversal)
- [Codelab de accesibilidad en Compose](https://developer.android.com/codelabs/jetpack-compose-accessibility)

## 2. Contraste de la marca (calculado con la fórmula de WCAG)
Todos los pares de texto de Tiza y Noche superan 4,5:1. La mayoría pasan de 6:1:
- `inkMuted` sobre `surface300`: 6,22 en Tiza.
- `danger` sobre `dangerSoft`: 5,23 en Tiza.
- `inkSubtle` sobre `surface300`: 4,51. Es el mínimo, y solo se usa en texto deshabilitado o secundario.

Los bordes `lineStrong` dan 3,6:1, por encima del 3:1 que se pide a los componentes. El Volt siempre lleva `onVolt` encima (14,2:1). **No hay que cambiar ningún color.**

## 3. Hallazgos y correcciones (rama `firebase-feature`)
| # | Problema | Criterio | Corrección |
|---|---|---|---|
| 1 | `TriChip`: ni TalkBack ni VoiceOver decían si estaba seleccionado; usaba el rol *pestaña* también en chips de acción; medía 40 dp; con fuente grande el texto se cortaba | 4.1.2, 2.5.8, 1.4.4 | Rol `RadioButton` con `selected` (o `Button` en las acciones), mínimo de 48 dp, el texto salta de línea, y `contentDescription` opcional |
| 2 | `TriTabs` (Partido, Equipos, Tabla, Miembros): sin estado *seleccionado*; al 200 % quedaba «Miem…» | 4.1.2, 1.4.4 | `selected` y `selectableGroup`; 48 dp; dos líneas antes de los puntos suspensivos |
| 3 | `TriTag` y las posiciones: el lector leía «OWNER», «DEL» o «PROV.» en mayúsculas o abreviado | 1.3.1 | La etiqueta se lee tal cual y la posición, completa («Delantero») |
| 4 | Los títulos de sección (`TriSectionTitle`) no eran encabezados, así que no se podía saltar entre secciones | 1.3.1, 2.4.6 | `heading()` |
| 5 | Avisos (`TriNotice`), burbujas de chat y eventos del acta se leían a trozos | 1.3.1 | Se leen de una vez. El evento se lee como «Minuto 23. Gol. Ana. Equipo A» |
| 6 | Fila de clasificación: se leía «1, A, Ana, TÚ, #10, DEL, 1040, ▲ 12»; al 200 % el nombre se cortaba en dos letras | 1.3.1, 1.4.4, 1.4.10 | Una frase: «Puesto 1. Ana, tú. Dorsal 10. Delantero. 1040 de rating. Sube 12». Con fuente ≥ 130 % el valor baja bajo el nombre y el nombre puede ocupar dos líneas |
| 7 | El campo de juego solo decía «Alineación de los dos equipos» | 1.1.1 | Lee las dos alineaciones con los nombres |
| 8 | Filas con interruptor (silenciar club, informes de fallos, estadísticas, horario activo): dos paradas, y el interruptor sin nombre | 4.1.2 | Toda la fila es un único interruptor con su texto (`toggleable`, `Role.Switch`) |
| 9 | Campos de texto: el error no se anunciaba; las reglas de la contraseña no decían si se cumplían; la etiqueta se leía en mayúsculas | 3.3.1, 4.1.3 | `error()` en el campo y región viva en el mensaje; «Mínimo 8 caracteres: cumple / no cumple»; la etiqueta, tal cual. No se pone `contentDescription` en el campo: se leería en lugar de lo escrito |
| 10 | El ojo de la contraseña medía 24 dp y no tenía rol | 2.5.8, 4.1.2 | 48 dp y `Role.Button` |
| 11 | Hora del partido y chips de hora: se leía solo «20:00, botón» | 2.4.6, 4.1.2 | «Hora: 20:00» y «Cierre de la convocatoria: 22:00» |
| 12 | Jugador del acta, botones del informe y logo al crear el club: sin rol ni etiqueta de acción | 4.1.2 | `Role.Button` y `onClickLabel` («Minutos jugados», «Logo del club»); 48 dp |
| 13 | Selector de hora nuevo (spec 020) | AC-020-10 | Nace accesible: la hora elegida es una región viva, las celdas son botones de radio con su valor («20 horas»), los botones ± tienen nombre, hay un único CTA y la hoja tiene scroll para la fuente grande |
| 14 | Previews | — | `ScreenPreview(fontScale = LARGEST_FONT_SCALE)`: previews al 200 % de Tabla y Horario |

Ya estaban bien (spec 018):
- la barra inferior (`selected`, rol de pestaña);
- la barra superior (`heading`);
- los deltas de Elo («Sube 12»), la cuenta atrás, las plazas, el marcador y la racha, cada uno con su descripción;
- los días de la semana del horario (botones de radio);
- el texto en `sp` y sin alturas fijas en los contenedores de texto;
- los botones de ≥ 48 dp.

## 4. Pendiente
- **Verificar en dispositivo** (no se puede hacer en el contenedor):
  - Android: TalkBack, Accessibility Scanner y la fuente y la visualización al máximo;
  - iOS: VoiceOver, Dynamic Type en los tamaños de accesibilidad y el Accessibility Inspector de Xcode (o `XCUIApplication.performAccessibilityAudit()`).
  - Comprobar en iOS que `LocalDensity.fontScale` sigue a Dynamic Type: la documentación de JetBrains no lo dice.
- **Alto contraste en iOS**: Material3 no lo trae. Haría falta una paleta Tiza/Noche de alto contraste y leer `UIAccessibilityDarkerSystemColorsEnabled`. Propuesta para la 1.1.
- **Reducir movimiento**: respetar *Quitar animaciones* (Android) y *Reducir movimiento* (iOS) en las transiciones y en la cuenta atrás.
- **Informe previo al lanzamiento de Google Play**: revisar sus avisos de accesibilidad con la primera build interna.
- Componentes sin uso (`SquadfyClubDetailBanner` y `SquadfyClubDetailTabRow`, con alturas fijas y una sola línea): borrarlos.

## 5. Cómo probar
1. **Android**:
   - Ajustes → Accesibilidad → Tamaño de visualización y texto: fuente y visualización al máximo.
   - Recorrer Inicio, Club (las 4 pestañas), Horario, Selector de hora, Partido, Acta, Tabla, Chat y Perfil.
   - Nada debe cortarse ni solaparse.
2. **TalkBack**:
   - Recorrer con deslizamientos: cada elemento dice qué es, su estado y qué hace.
   - Con el rotor de encabezados se salta entre secciones.
3. **iOS**: lo mismo con Dynamic Type (tamaños de accesibilidad) y VoiceOver.
4. **Herramientas**:
   - Accessibility Scanner (Android) y Accessibility Inspector (iOS) en cada pantalla;
   - el informe previo al lanzamiento de Play en cada build interna.
