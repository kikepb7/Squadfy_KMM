# 008 · Clasificaciones: rating y estadísticas

- **Estado:** Draft
- **Reglas:** BE-003 RN-6/11, BE-004 RN-7/8, APP-RN-06, APP-RN-09
- **Backend:** `BACKEND.md` §7.5 y §8.5
- **Depende de:** 003 (miembros)
- **Repos afectados:** Squadfy_App

## Problema / objetivo
La pestaña «Clasificación» enseña un índice de rendimiento calculado en el cliente con estadísticas que siempre eran 0. El backend ofrece dos clasificaciones reales:
- por **rating Elo**, que es pública en el club;
- por **estadísticas**, ordenable.

Además ofrece mi posición y mis cifras.

## Criterios de aceptación
- **AC-008-01** La pestaña «Clasificación» tiene un selector «Rating | Estadísticas».
- **AC-008-02** **Rating** (`GET /ratings`):
  - columnas: posición (los empates comparten), jugador, rating redondeado y partidos;
  - etiqueta «Provisional» si `isProvisional` (APP-RN-09);
  - mi fila resaltada;
  - una tarjeta fija «Tu posición: {rank} de {totalPlayers} · {rating}» (`/ratings/me`).
- **AC-008-03** **Estadísticas** (`GET /stats?sortBy=`):
  - chips de orden: Goles, Asistencias, Partidos, Minutos y Victorias (por defecto Goles);
  - columnas: posición, jugador, PJ, V, E, D, Goles, Asistencias, 🟨 y 🟥, con scroll horizontal;
  - incluye a los miembros con ceros.
- **AC-008-04** La ficha de un miembro muestra sus estadísticas y su rating:
  - si es la mía, con `/stats/me` y `/ratings/me`;
  - si es de otro, con su fila de `/stats` y de `/ratings`, sin pedir nada nuevo al backend.
- **AC-008-05** Los datos son network-first con caché en memoria (ADR-0006) y se invalidan al cerrar o reabrir un partido (spec 007).
- **AC-008-06** Estado vacío: «Aún no hay partidos cerrados» cuando todos tienen 0 partidos.
- **AC-008-07** Se eliminan `performanceIndex` (`Extensions.kt`), `StandingRowUiModel` y las columnas antiguas.
- **AC-008-08** **Valoración del partido** (BE-008 RN-F1): en un partido `COMPLETED`, cada miembro de los equipos muestra su variación de rating (`ratingChanges`: «+12» en verde, «−8» en rojo). La ficha del miembro muestra la variación de sus últimos partidos. No existe valoración manual.
