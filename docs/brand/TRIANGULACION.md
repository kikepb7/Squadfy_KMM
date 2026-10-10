**Triangulación** es el sistema de diseño personal de Kike, y Squadfy es su primera marca construida sobre él. Sirve para cualquier app futura: se cambia el nombre, el escudo y, si hace falta, el acento; el esqueleto (Corte, segmentos inclinados, triángulos, números grandes) es siempre el mismo.

## Idea
Un buen partido se construye con pases cortos que forman triángulos. Triangulación traduce eso a UI: **tres nodos, líneas limpias y una esquina cortada**. La marca es minimalista, con un único color que llama la atención (Volt) sobre una base verde muy oscura y un blanco hueso. Nunca se usan más de dos colores de acento a la vez.

## Firma visual (lo que hace que sea tuyo)
1. **El Corte.** Las superficies con identidad (botón principal, ticket, escudo, tarjetas) tienen la esquina superior derecha biselada a 45° con un *dobladillo* en otro tono. Tamaños: `cut-sm`, `cut-md`, `cut-lg`. Las demás esquinas usan `radius-md`/`radius-lg`. Nunca biseles las cuatro esquinas.
2. **Segmentos inclinados −18°.** Plazas, equilibrio, progreso y pestañas usan paralelogramos, no barras redondeadas.
3. **Glifos geométricos.** Posiciones: portero ■, defensa ◆, centrocampista ●, delantero ▲. Resultados: victoria ●, empate ■, derrota ▼. El significado nunca depende solo del color.
4. **Cifras héroe.** Los números importantes (plazas, marcador, rating) van en `stat-xl` o `display-lg`, con `font-variant-numeric: tabular-nums`.
5. **Triangulaciones.** En el campo, líneas discontinuas unen a los jugadores cercanos. Es la marca de agua de la identidad (Pase y Corte la usan).

## Color
- Fondo: `surface-100`. Tarjetas y campos: `surface-200`. Zonas hundidas: `surface-300`.
- Bloque de énfasis oscuro (ticket, campo, héroes): `surface-inverse` con texto `on-inverse` / `on-inverse-muted`.
- Texto: `ink` (principal), `ink-muted` (secundario), `ink-subtle` (placeholder). Texto de acento verde: `accent-text`.
- Acción principal: `brand` con `on-brand`. La inscripción y el CTA más importante de cada pantalla usan `volt` con `on-volt`; **solo uno por pantalla**.
- Equipos: `team-a` (Volt) y `team-b` (Brasa, coral). Siempre llevan la letra A o B. Texto sobre blanco: `team-a-text` y `team-b-text`.
- Estados: `success-soft` + `accent-text`; `warning-soft` + `warning`; `danger-soft` + `danger`. Siempre con icono o palabra.
- Tarjetas disciplinarias: `card-yellow`, `card-red`, con la palabra o el icono de tarjeta.
- Los dos temas se llaman **Tiza** (claro, por defecto) y **Noche** (oscuro). En Noche, `brand` pasa a Volt. No escribas hex en componentes: usa tokens.
- Contraste: todos los pares de texto están calculados a ≥ 4.5:1 (3:1 para bordes y anillos) en los dos temas.
- Color de club: `Escudo` acepta seis tonos (`volt`, `cancha`, `brasa`, `arcilla`, `cielo`, `tiza`, `ambar`). Es el único sitio donde el usuario personaliza color.

## Tipografía
- **Bricolage Grotesque** para display y cifras (`stat-xl`, `display-xl`, `display-lg`, `display-md`, `title`): tiene carácter y buen aspecto en negrita.
- **Instrument Sans** para texto e interfaz (`body-lg`, `body`, `body-sm`, `label`, `label-caps`).
- Las etiquetas pequeñas sobre bloques de datos van en `label-caps`, ya escritas en mayúsculas.
- Máximo dos tamaños de display por pantalla.

## Espaciado, radios, sombras
- Rejilla de 4 px: `space-1` … `space-16`. Margen lateral de pantalla `space-4`; entre secciones `space-6`; objetivo táctil mínimo `space-12` (48 px).
- Radios: `radius-xs` (insignias), `radius-sm` (campos, chips), `radius-md` (tarjetas, botones), `radius-lg` (hojas), `radius-pill` solo para píldoras de estado.
- Sombras suaves: `shadow-1` (reposo), `shadow-2` (ticket, barra inferior), `shadow-3` (hojas), `shadow-volt` (solo la inscripción activa). En Noche las sombras casi desaparecen; la elevación se logra con `surface-200` / `surface-300`.
- Foco: anillo de `stroke-focus` en `focus-ring`, con 3 px de separación.

## Voz y contenido
- Español de España, tuteo, frases cortas y verbos de acción: «Apuntarme», «Desapuntarme», «Cerrar partido». Sin signos de exclamación ni emojis decorativos.
- Los estados dicen la verdad en una línea: «Convocado · tienes plaza», «En espera · n.º 2», «Convocatoria cerrada», «Sin conexión · actualizado hace 2 min».
- Las horas siempre en la zona del club; si el dispositivo está en otra, se añade «(Madrid)».
- El rating se muestra entero, con la etiqueta «Provisional» hasta los 10 partidos.

## Iconos
Iconos propios de trazo (24 px, 1.75 px, extremos redondeados) en `Icon`. Para nuevos iconos, mantén ese trazo y evita rellenos, salvo las tarjetas amarilla y roja.

## Componentes propios
`Boton`, `Campo`, `CodigoClub`, `PosChip`, `Avatar`, `Escudo`, `Ficha`, `Campo11` (campo con jugadores), `Plazas`, `CuentaAtras`, `Inscripcion`, `Ticket`, `Equilibrio`, `Marcador`, `Forma`, `Rating`, `FilaRanking`, `Cronologia`, `Ciclo`, `Aviso`, `Tabs`, `BarraInferior`. Cada uno tiene su guía y su vista previa. El espejo en Compose Multiplatform está en el paquete `com.kikepb.triangulacion`.

## Cómo usarlo en otra app
1. Copia `tokens.json` y cambia solo `name`, el acento (`volt`) y el escudo.
2. Mantén El Corte, los segmentos inclinados y la tipografía.
3. Sustituye el logo por uno propio: el sistema pone el mismo marco (contenedor biselado + glifo + wordmark en minúsculas Bricolage 700).
