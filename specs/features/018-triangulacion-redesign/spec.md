# 018 · Rediseño «Triangulación»

- **Estado:** Done (2026-10-10). El owner pidió un rediseño completo de la app según el kit `Squadfy-Brand-Kit.zip` (sistema de diseño Triangulación), en la rama `squadfy-redesign-feature`.
- **Fuente:** el kit (guía `IMPLEMENTAR-EN-CLAUDE-CODE.md`, `tokens.json`, librería Compose, logos y maquetas de las 16 pantallas en Tiza y Noche). Lo reutilizable se copia a `docs/brand/` (tokens, logos y guía). Las maquetas y capturas promocionales son de prueba y **no** se incluyen en el repo (owner, 2026-10-10).
- **Alcance:** solo presentación. No cambia ninguna regla del backend, ni el dominio, los DTOs o el contrato. Lo que está detrás de un flag en PRO sigue detrás de su flag.
- **Sustituye** a la identidad de la 016 (escudo, paleta Night Pitch y Poppins) y a la decisión de mantener el fondo azul pizarra en el tema oscuro.

## Decisiones del owner (2026-10-10)
- **Logo:** opción 1 «Corte» (contenedor biselado con triángulo de pases y nodo Volt). Icono de app Corte en tinta sobre Volt.
- **Tema oscuro:** «Noche» del kit (fondos verde casi negro, `brand` = Volt). Tema claro: «Tiza». Siguen al sistema.
- **Variantes:** Inicio A, Partido A, Equipos B (campo con jugadores) con la lista como alternativa, Clasificación A.
- **Sistema de diseño:** dentro de `core/designsystem`, paquete `triangulacion`.
- **Fuentes:** Bricolage Grotesque (500–800, display y cifras) e Instrument Sans (400–700, texto), TTF estáticos de Google Fonts (OFL).

## Criterios de aceptación
- **AC-018-01 Tokens.** Todos los colores salen de los tokens Tiza/Noche (`tokens.json`). Ningún componente usa hex sueltos. Desaparecen los `accent*` y la paleta de la 016. Texto ≥ 4,5:1 y bordes de control ≥ 3:1 en los dos temas.
- **AC-018-02 Tipografía.** Bricolage Grotesque en display y cifras (`stat-xl`, `display-xl/lg/md`, `title`), con dígitos tabulares en marcador, rating, plazas y cuenta atrás. Instrument Sans en el resto. Poppins se elimina.
- **AC-018-03 Marca.** Logo Corte (marca y lockup, versión inversa en fondos oscuros), icono de app Android (adaptativo y monocromo) e iOS, y splash con la marca.
- **AC-018-04 Firma visual.** El Corte (esquina superior derecha biselada) en el botón principal, el ticket, la ficha, el escudo y las tarjetas con identidad. Segmentos inclinados −18° en plazas, equilibrio y pestañas. Glifos de posición (■ POR, ◆ DEF, ● MED, ▲ DEL) y de resultado (● V, ■ E, ▼ D): el significado nunca depende solo del color.
- **AC-018-05 Componentes.** Botón, campo de texto, barra inferior, cabecera, pestañas, escudo, avatar, chip de posición, plazas, cuenta atrás, ticket, equilibrio, marcador, forma, variación de rating, fila de ranking, cronología, aviso y campo con jugadores, con vista previa en claro y oscuro y sin textos fijos.
- **AC-018-06 Pantallas existentes.** Bienvenida, login, registro y recuperación; Inicio (ticket del próximo partido, Apuntarme, tus clubs y última jornada); club (cabecera con escudo, rol y miembros; pestañas Partido, Equipos, Tabla y Miembros; ajustes del club tras el botón de ajustes); partido (plazas, cuenta atrás y estado de inscripción: libre, convocado, en espera y cerrada); acta (marcador y cronología); tabla (rating con variación y filtros); ficha del jugador; unirse con código; horario; ausencias; perfil y chat. Se parecen a su maqueta en Tiza y Noche.
- **AC-018-07 Pantallas nuevas.** Bienvenida; Equipos con el campo (cada jugador en su posición, líneas de triangulación entre cercanos y tú resaltado) y la lista como alternativa; sorteo completado («Juegas en el Equipo A»); resultado tras cerrar el partido (marcador, tu variación y la de los demás).
- **AC-018-08 Navegación.** La barra inferior (Inicio, Clubs, Chat) se ve en todas las pantallas de la app con sesión, con la pestaña activa en píldora.
- **AC-018-09 Calidad.** `testDebugUnitTest`, `assembleDebug`, `checkHardcodedStrings` y la compilación de iOS en verde. Los ViewModels y sus tests no cambian salvo para exponer datos que ya existen.

## Decisiones de implementación
- **Datos que ya existían y se exponen en la UI:** «Última jornada» de Inicio (último partido cerrado, resultado, cambio de rating y forma) sale de `GET /clubs/{id}/matches?status=COMPLETED` y de `ratingChanges`; la variación de la tabla es la del último partido cerrado; la evolución de la ficha se reconstruye con el rating actual y los cambios recientes.
- **Equipos** es una pestaña nueva con su `TeamsViewModel` (solo lectura). Editar o resortear sigue en el detalle del partido (spec 006).
- **Ajustes del club** dejan de ser una pestaña: se abren con el botón de la cabecera.
- **Push «equipos publicados»** abre la pantalla nueva de sorteo (`squadfy://match/{id}/draw`), que lleva al detalle con «Ver equipos».
- **Resultado** se abre desde «Última jornada» y desde el acta de un partido cerrado.
- **Código de invitación:** las celdas crecen de 8 a 12 porque el backend acepta 6–12.
- **Iconos y emojis:** los eventos del acta usan los glifos del kit; desaparecen los emojis.
- **Fuera de la maqueta, sin dato en el backend:** la fila «Notificaciones» del perfil (no hay ajustes globales; silenciar es por club), el contador de no leídos del chat y la campana de Inicio. Quedan para cuando haya backend.
- **Perfil** sigue abriéndose como hoja desde el avatar de Inicio y desde Chat, con el aspecto de página de la maqueta.
- La búsqueda del chat filtra en local los chats cargados.

## Preguntas abiertas
- Ninguna bloqueante.
