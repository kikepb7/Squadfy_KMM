# Pantallas

## Inventario (16 pantallas, claro y oscuro, ES y EN)
| # | Pantalla | Spec | Componentes propios |
|---|---|---|---|
| 01 | Bienvenida | 002 | Boton |
| 02 | Login | 002 | Campo, Boton |
| 03 | Inicio | 010 | Ticket, Boton, Escudo, Delta, Forma |
| 04 | Club · Partido | 005 | Tabs, Plazas, CuentaAtras, Inscripcion |
| 05 | Club · Equipos | 006 | Equilibrio, Avatar, Aviso |
| 06 | **Campo** (nueva) | 006 | Campo11, Ficha, Equilibrio |
| 07 | Acta del partido | 007 | Marcador, Cronologia |
| 08 | Clasificación | 008 | FilaRanking, Rating, Delta |
| 09 | Ficha de jugador (nueva) | 008 | Rating, Forma, Delta |
| 10 | Unirse con código | 003 | CodigoClub, Aviso |
| 11 | Horario semanal | 004 | Campo, Chip |
| 12 | Ausencias | 014 | Aviso |
| 13 | Perfil | 011 | Avatar, Boton |
| 14 | Chat | heredado | Campo, Avatar |
| 15 | **Sorteo completado** (nueva) | 006 | Boton |
| 16 | **Resultado** (nueva) | 007, 008 | Rating, Forma, Delta |

Estados extra del Partido: libre, convocado, en espera y cerrada (`estado-partido-*`).

## Pantallas nuevas que propongo
- **Campo**: ambos equipos sobre el campo, con las triangulaciones entre jugadores cercanos y tú resaltado. Convierte el sorteo en algo que se *ve*.
- **Sorteo completado**: el momento de «juegas en el Equipo A» al publicarse los equipos (llega por push).
- **Resultado**: marcador, tu variación de rating y la de los demás tras cerrar el partido.
- **Ficha de jugador**: rating, evolución, forma y estadísticas (la spec 008 ya pide «mis estadísticas»).
- Idea extra, sin pantalla todavía: **compartir el acta o la clasificación como imagen** para el grupo de WhatsApp.

## Para decidir: 4 pantallas con 3 direcciones cada una
Mira las imágenes de `decisiones/`. Mi recomendación es la A de cada una; las otras son más arriesgadas.

| Pantalla | A (recomendada) | B | C |
|---|---|---|---|
| Inicio | Ticket del próximo partido + CTA, lista de clubs | «Tu semana» primero: héroe con cuenta atrás y ciclo | Lista compacta con acción en cada fila |
| Partido | Tarjeta con plazas, cuenta atrás e inscripción | Héroe oscuro con avatares y CTA anclado abajo | Checklist del ciclo: te apuntaste → cierre → sorteo |
| Equipos | Dos listas + equilibrio | **Campo** con los jugadores en posición | Alineación por líneas (portero, defensas…) |
| Clasificación | Lista con rating y variación | Podio + lista | Barras de rating |

Mi sugerencia de mezcla: **Inicio A**, **Partido A**, **Equipos B (Campo)** como vista principal con la lista como alternativa, y **Clasificación B (podio)** si quieres más emoción.
