# 004 · Horario semanal del club (v1)

- **Estado:** Draft
- **Reglas:** BE-002 RN-1/2/3/10, APP-RN-03, APP-RN-04
- **Backend:** BE-002, `BACKEND.md` §7.2 y §8.3
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
- **AC-004-04** El horario se cachea en Room, en la tabla `club_schedule` (forma parte de la migración v3 de la spec 003 o de una v4 con su `Migration`), y se muestra sin conexión.
- **AC-004-05** Errores: un 400 por zona horaria desconocida o una duración fuera de rango muestra un texto específico. Un 409 «horario ya existe» provoca un refresco y se pasa a modo edición.
- **AC-004-06** La sección de **excepciones** se muestra solo con el flag `SCHEDULE_EXCEPTIONS`, y el campo **«Hora del sorteo» (`drawTime`)** solo con `CUSTOM_DRAW_TIME`. Ambos flags están desactivados hasta que el backend publique esas funciones; entonces se migran a las rutas que defina. **Se eliminan** las horas de inicio y fin (sustituidas por la duración), `seasonStartMonth`/`Day` y el cálculo de temporada de `IdentityCard`, que desaparece también como causa de cierres inesperados.
- **AC-004-07** Desactivar el horario (`isActive = false`) pide confirmación con el texto «No se crearán más partidos automáticamente».

## Preguntas abiertas
- (ninguna)
