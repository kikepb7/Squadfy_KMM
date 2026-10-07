# 004 · Horario semanal del club (v1)

- **Estado:** In progress (7/8; falta el E2E). Aprobada por el owner el 2026-10-07.
- **Reglas:** BE-002 RN-1/2/3/10, APP-RN-03, APP-RN-04
- **Backend:** BE-002, BE-008 (RN-B, RN-D), `BACKEND.md` §7.2 y §8.3
- **Depende de:** 002, 003 (`myRole`, Room v3)
- **Repos afectados:** Squadfy_App

## Problema / objetivo
El formulario actual de calendario usa un modelo que el backend no tiene:
- hora de inicio y de fin;
- `drawTime`;
- inicio de temporada;
- excepciones con fecha en texto libre.

El backend v1 tiene otro modelo: día, hora, zona horaria, **formato** (que fija el cupo) y duración. Al crear el horario, el primer partido se planifica de inmediato.

## Criterios de aceptación
- **AC-004-01** Los gestores ven el formulario «Horario»:
  - día (chips L–D);
  - hora (`TimePicker`);
  - zona horaria (por defecto la del dispositivo si es IANA, si no `Europe/Madrid`, con un selector con búsqueda);
  - formato (5v5 · 10 plazas / 7v7 · 14 / 11v11 · 22);
  - duración (10–180 min, por defecto 60);
  - «Activo».
- **AC-004-02** Si no hay horario (404 en `GET /schedule`), se muestra «Crear horario» y se hace un `POST` (201). Si existe, se modifica con `PATCH`, enviando solo los campos cambiados. Al guardar, aparece el aviso «El próximo partido se ha planificado» y la pantalla Partido se refresca.
- **AC-004-03** Los no gestores ven un resumen de solo lectura: «Jueves 20:00 (Madrid) · 5v5 · 60 min», o «Sin horario: pide a un gestor que lo configure».
- **AC-004-04** El horario es network-first con caché en memoria (ADR-0006 revisado): sin conexión se muestra el último horario cargado en la sesión, con un aviso.
- **AC-004-05** Errores: un 400 por zona horaria desconocida o una duración fuera de rango muestra un texto específico. Un 409 «horario ya existe» provoca un refresco y se pasa a modo edición.
- **AC-004-06** **Cierre y sorteo** (flag `CUSTOM_DRAW_TIME`, BE-008 RN-D):
  - el formulario añade «Cierre: N días antes a las HH:mm» y «Sorteo: N días antes a las HH:mm» (0–6 días; por defecto 1 día antes a las 22:00, y el sorteo igual que el cierre);
  - se valida en local que el cierre sea anterior al inicio y que el sorteo sea ≥ cierre y < inicio; el 400 del servidor también se muestra;
  - el resumen de solo lectura añade «Cierra el miércoles a las 22:00 · Sorteo el jueves a las 12:00».
- **AC-004-08** **Excepciones** (flag `SCHEDULE_EXCEPTIONS`, BE-008 RN-B):
  - todos los miembros ven la lista (`GET /schedule/exceptions`): «Cancelado» o «Movido al {fecha y hora}», con su motivo;
  - los gestores crean una excepción eligiendo una **fecha futura que caiga en el día de partido** (selector limitado a esos días): `CANCELLED`, o `RESCHEDULED` con fecha y hora futuras; el motivo es opcional (≤ 200);
  - los gestores pueden borrarla, con la confirmación «Se restaurará el partido de esa semana»;
  - un 409 (la fecha ya tiene excepción, o la semana ya se jugó) muestra un texto específico.

  Tras crear o borrar una excepción se refresca la convocatoria (spec 005).
- **AC-004-09** **Se eliminan** las horas de inicio y fin (sustituidas por la duración), `seasonStartMonth`/`Day` y el cálculo de temporada de `IdentityCard`, que desaparece también como causa de cierres inesperados. Las excepciones antiguas (rutas `/club/...`) se sustituyen por las de v1.
- **AC-004-07** Desactivar el horario (`isActive = false`) pide confirmación con el texto «No se crearán más partidos automáticamente».

## Preguntas abiertas
- (ninguna)
