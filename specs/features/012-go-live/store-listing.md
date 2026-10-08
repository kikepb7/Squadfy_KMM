# 012 · Fichas de tienda (borrador para revisar)

Los textos son una propuesta. Lo marcado con **[owner]** lo decide o lo aporta el owner.

## Datos comunes
- **Nombre:** Squadfy
- **Paquete / bundle:** `com.kikepb.squadfy` (Android e iOS, decisión D-5)
- **Categoría:** Deportes (Play: *Sports*; App Store: principal *Sports*, secundaria *Social Networking*)
- **Email de contacto y web de soporte:** **[owner]**
- **Política de privacidad:** `PRIVACY_POLICY_URL` (por defecto `https://squadfy.app/privacy`). Hay que publicarla antes de enviar la app (ver `privacy-data.md`).
- **URL de borrado de cuenta (Play):** `https://<api>/account/delete` (servida por el backend, spec 010)
- **Idiomas:** español (principal) e inglés

## Textos — español
- **Descripción corta (Play, ≤ 80):** `Organiza el partido semanal de tu equipo: convocatoria, equipos y estadísticas.`
- **Subtítulo (App Store, ≤ 30):** `Tu fútbol semanal, organizado`
- **Descripción larga:**

> Squadfy organiza el partido semanal de tu grupo de fútbol amateur sin hojas de cálculo ni mensajes perdidos.
>
> • Crea tu club e invita a tus compañeros con un código.
> • Define el horario semanal: la convocatoria se abre y se cierra sola.
> • Apúntate en un toque; si se llena, entras en la lista de espera y subes automáticamente cuando alguien se borra.
> • Equipos equilibrados en un sorteo automático según el nivel de cada jugador.
> • Registra goles, asistencias y minutos y consulta la clasificación de rating y las estadísticas del club.
> • Notificaciones cuando se abre la convocatoria, se publican los equipos o te toca plaza.
> • Chat con los miembros del club.
>
> Tu cuenta se puede eliminar en cualquier momento desde la app.

- **Palabras clave (App Store, ≤ 100):** `fútbol,futbol,amateur,partido,equipo,convocatoria,pachanga,fútbol 7,sorteo,estadísticas,club`

## Textos — inglés
- **Short description (≤ 80):** `Run your team's weekly match: sign-ups, balanced teams and stats.`
- **Subtitle (≤ 30):** `Your weekly football, sorted`
- **Full description:**

> Squadfy runs your amateur football group's weekly match — no spreadsheets, no lost messages.
>
> • Create your club and invite teammates with a code.
> • Set the weekly schedule: sign-ups open and close on their own.
> • Join in one tap; when it's full you go on the waiting list and move up automatically.
> • Balanced teams drawn automatically from each player's level.
> • Log goals, assists and minutes, and check the club's rating table and stats.
> • Notifications when sign-ups open, teams are published or you get a spot.
> • Chat with your club members.
>
> You can delete your account at any time from the app.

- **Keywords (≤ 100):** `football,soccer,amateur,match,team,sign up,pickup,5-a-side,7-a-side,team picker,stats,club`

## Recursos gráficos
| Recurso | Tamaño | Estado |
|---|---|---|
| Icono Play | 512×512 PNG | ✅ `docs/brand/store/playstore-icon-512.png` (kit de marca, spec 016) |
| Imagen destacada Play | 1024×500 | ✅ `docs/brand/store/google-play-feature-1024x500.png` (kit de marca) |
| Capturas de teléfono | 2–8, 1080×2400 | Sacarlas de la release con la cuenta de demo: Inicio, Partido (convocatoria abierta), Equipos, Clasificación, Chat |
| Capturas de iPhone | 6,9" (1320×2868) y 6,5" (1284×2778) | Las mismas pantallas en el simulador |

## Clasificación de contenido
- **Play (IARC):** sin violencia, sexo, drogas ni apuestas. *¿Los usuarios pueden interactuar o intercambiar contenido?* **Sí** (chat). *¿Se comparte la ubicación?* No. *¿Compras digitales?* No.
- **App Store:** todo «Ninguno», salvo que haya contenido generado por usuarios, lo que normalmente lleva a 12+. **[owner: edad mínima de uso]**
- **Público objetivo (Play):** adultos (18+) o 13+. **[owner]** Con menores de 13 años se aplican las políticas de Familias, así que se recomienda 16+ o 18+.

## Requisitos de revisión pendientes
- **D-13, contenido generado por usuarios (Apple 1.2):** denunciar mensajes, bloquear usuarios y un contacto publicado. Ahora mismo la app no lo tiene.
- **Cuenta de demo:** ver `release-checklist.md` §3.
