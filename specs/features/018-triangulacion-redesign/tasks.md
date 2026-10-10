# 018 · Tareas

## Fase 1 · Tema y marca
- [x] **T-001** Tokens Tiza/Noche, tipografía (Bricolage + Instrument Sans) y medidas en `core/designsystem/triangulacion`. `SquadfyTheme` mapea Material 3 y `ExtendedColors` a los tokens; fuera los `accent*`, la paleta 016 y Poppins (AC-018-01/02).
- [x] **T-002** Logo Corte (vectores marca/lockup, color e inverso), icono de app Android/iOS y splash (AC-018-03).

## Fase 2 · Componentes
- [x] **T-003** Componentes base por dentro con su API: botón, campos, barra inferior, cabecera, diálogos y hojas, avatar, burbujas (AC-018-05).
- [x] **T-004** Componentes propios: CutShape, Escudo, PosChip, Plazas, CuentaAtras, Ticket, Equilibrio, Marcador, Forma, DeltaRating, FilaRanking, Cronologia, Aviso, Tabs, Campo11 (AC-018-04/05).

## Fase 3 · Pantallas
- [x] **T-005** Auth: bienvenida (nueva), login, registro, recuperación y verificación (AC-018-06/07).
- [x] **T-006** Inicio: ticket, Apuntarme, tus clubs y última jornada (AC-018-06).
- [x] **T-007** Club: cabecera, pestañas, Partido con estados de inscripción y ajustes tras el botón (AC-018-06).
- [x] **T-008** Equipos: campo con jugadores, equilibrio y lista (AC-018-07).
- [x] **T-009** Acta: marcador y cronología (AC-018-06).
- [x] **T-010** Tabla y ficha del jugador (AC-018-06).
- [x] **T-011** Unirse, crear club, horario, ausencias y vetos (AC-018-06).
- [x] **T-012** Perfil (pantalla completa) y chat (AC-018-06).
- [x] **T-013** Sorteo completado y resultado del partido (AC-018-07).
- [x] **T-014** Barra inferior en todas las pantallas con sesión (AC-018-08).

## Fase 4 · Cierre
- [x] **T-015** Verificación en el emulador (Tiza y Noche) contra las maquetas, tests, compilación de iOS, `checkHardcodedStrings`; `docs/brand`, CLAUDE.md y roadmap actualizados (AC-018-09).

## Fase 5 · Revisión del owner (2026-10-10)
- [x] **T-016** Previews de todas las pantallas en Tiza y Noche con los datos de las maquetas (`ScreenPreview`, `*Samples`): auth, Inicio, club con cada pestaña, partido y sus estados, campo, acta, tabla, ficha, unirse, crear, horario, ausencias, vetados, mis clubes, sorteo, resultado, chat, perfil y feature flags. Para que se puedan previsualizar, las partes con ViewModel propio exponen su contenido sin estado (`ClubSettingsContent`, `MemberStatsContent`, `tabContent` de `ClubDetailScreen`).
- [x] **T-017** Pantalla «Campo» (06-campo): alineación por líneas con el reparto del diseño, pases solo entre líneas consecutivas, porteros con anillo, «tú» con anillo discontinuo, proporciones y franjas del campo; abre desde el acta («Ver en el campo») y desde el sorteo («Ver equipos»). La pestaña Equipos sigue el mismo orden (campo y equilibrio, y la lista como alternativa). Tests de `PitchLayout`.
- [x] **T-018** Icono de app rehecho: la marca cabe en la zona segura del icono adaptativo (antes la máscara circular recortaba el bisel y las esquinas) y el triángulo y los nodos son más gruesos para leerse a tamaño de icono. Regenerados Android (adaptativo, monocromo, PNG), iOS (claro, oscuro, tintado), splash y la marca de la app.
- [x] **T-019** Cada composable de pantalla tiene sus previews (Tiza y Noche) en su propio archivo; los datos de ejemplo viven en `preview/*Samples.kt` de cada módulo.
- [x] **T-020** `scripts/dev/seed_demo.py`: datos de demo en el backend local para ver todas las funcionalidades con datos reales (plantilla, partidos jugados con ratings, próximo partido con equipos, espera, ausencias, chats). Las cabeceras del acta y del resultado usan el tamaño del propio partido, no el formato actual del horario.

