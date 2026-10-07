# ADR-0003 · Zona horaria por club

- **Estado:** Aceptado. **Implementado en el backend** (`schedule.timeZone`, BE-002 RN-2). En la app: mostrar las fechas en la zona del club (APP-RN-03).
- **Fecha:** 2026-10-05

## Contexto
El backend interpreta `matchTime` como UTC. Un partido configurado a las «20:00» acaba a las 22:00 en Madrid en verano, y los cortes de día (apertura y cierre) se calculan sobre la medianoche UTC.

## Decisión
- `ClubSchedule.timezone` es un ID IANA (por defecto `Europe/Madrid`). El cliente lo propone a partir de `TimeZone.currentSystemDefault()` al crear el calendario.
- El servidor calcula los instantes con `ZonedDateTime.of(localDate, localTime, zone).toInstant()`, de modo que el horario de verano (DST) se resuelve correctamente.
- La API transmite los instantes en UTC. El cliente los formatea en la zona del dispositivo y muestra la del club si es distinta.

## Consecuencias
- Hace falta una migración para añadir la columna `timezone` (se rellena con `Europe/Madrid`).
- Los tests de ventana incluyen semanas con cambio de hora: el último domingo de marzo y el último domingo de octubre.
