# 003 · Clubes y membresía (v1)

- **Estado:** In progress. Aprobada por el owner el 2026-10-07 («continúa con las siguientes fases»).
- **Reglas:** BE-001 RN-1…14, APP-RN-04, APP-RN-05, APP-RN-06, APP-RN-12, APP-RN-13
- **ADRs:** ADR-0005, ADR-0006
- **Backend:** BE-001 (hecha), `BACKEND.md` §7.1 y §8.2
- **Depende de:** 002
- **Repos afectados:** Squadfy_App

## Problema / objetivo
Crear un club, unirse y ver el detalle se apoyan en rutas antiguas. Además, `ClubMemberDTO` exige `email` y estadísticas que v1 ya no envía, así que la lista de miembros falla con `SERIALIZATION`.

Toda la administración del club (editar, código, roles, expulsar, vetar, transferir, salir) existe en el backend pero en la app son filas que no hacen nada. Y no hay una lista de «mis clubes».

## Historias de usuario
- **US-003-01** Como usuario quiero ver mis clubes en la pestaña Clubs, crear uno nuevo o unirme con un código.
- **US-003-02** Como miembro quiero compartir el código de invitación, editar mi dorsal y mi posición, y salir del club.
- **US-003-03** Como gestor quiero editar el club (nombre, descripción, límite y logo), regenerar el código, cambiar roles y expulsar o vetar según la jerarquía.
- **US-003-04** Como owner quiero transferir la propiedad para poder salir del club.

## Criterios de aceptación
**Lista, crear y unirse**
- **AC-003-01** La pestaña «Clubs» muestra mis clubes (Room, sincronizados con `GET /clubs`) con su logo, el número de miembros y mi rol, más los CTA «Crear» y «Unirme». Si no hay clubes, se muestra el estado vacío con los dos CTA.
- **AC-003-02** Al crear (201) o unirse con éxito, se navega a `ClubDetailRoute(clubId)` y desaparecen de la pila las pantallas de crear o unirse (APP-RN-12).
- **AC-003-03** Si el club se crea pero falla la subida del logo, el club existe: se navega a él con el aviso «No se pudo subir el logo» y la opción de reintentarlo desde Ajustes.
- **AC-003-04** Al unirse, los errores se muestran según el código:

  | Error | Texto |
  |---|---|
  | 400 `INVALID_INVITATION_CODE` | «Código no válido» |
  | 403 `BANNED_FROM_CLUB` | «Has sido vetado en este club» |
  | 409 | «Ya eres miembro o el club está lleno» |

  El código se normaliza a mayúsculas. La UI y el use case validan lo mismo: 6–12 caracteres alfanuméricos, dorsal 1–999 y posición elegida con chips del enum.

**Miembros**
- **AC-003-05** `ClubMemberRole` (`OWNER`, `ADMIN`, `CAPTAIN`, `PLAYER`) y `PlayerPosition` son enums de domain con mapeo seguro (APP-RN-13). `ClubMember` ya **no** tiene email ni estadísticas.
- **AC-003-06** `myRole` se calcula a partir del miembro cuyo `userId` coincide con el de la sesión. `MemberPermissions` (domain, puro) expone `canManageClub`, `canChangeRole(target, newRole)`, `canRemove(target)`, `canBan(target)` y `canTransfer(target)`, siguiendo BE-001 RN-9 y APP-RN-05. Está **cubierta por tests de tabla** con todas las combinaciones de actor y objetivo.
- **AC-003-07** En la ficha de un miembro, el gestor solo ve las acciones que permite `MemberPermissions`: «Cambiar rol», «Expulsar», «Vetar» y, si es owner, «Transferir propiedad». Las acciones destructivas piden confirmación. Si el servidor responde 403, aparece «No tienes permiso» y se refresca la lista.
- **AC-003-08** «Mi ficha»: dorsal y posición → `PATCH /clubs/{id}/members/me`. La foto que se muestra es la de perfil global (`profilePictureUrl`). **Se elimina** la subida de foto por club.
- **AC-003-09** Los miembros que salen, son expulsados o vetados desaparecen de la lista tras refrescar. Un `clubMemberId` que no está en la lista se muestra como «Exjugador» (APP-RN-06).

**Ajustes del club**
- **AC-003-10** Lo que ve cada miembro en Ajustes depende de su rol:
  - Todos: el código con «Copiar» y «Compartir» (share sheet: «Únete a {club} en Squadfy con el código {code}»), «Silenciar notificaciones» (spec 009) y «Salir del club».
  - El owner no ve «Salir del club»; en su lugar ve «Transfiere la propiedad para poder salir».
  - Los gestores, además: «Editar club», «Logo», «Regenerar código» (con confirmación), «Horario» (spec 004) y «Vetados».
- **AC-003-11** «Editar club» (`PATCH /clubs/{id}`) valida que el nombre no esté vacío y tenga ≤ 120 caracteres, que la descripción tenga ≤ 2000 y que `maxMembers` sea > 0. Un 400 por `maxMembers` menor que los miembros actuales muestra un mensaje específico.
- **AC-003-12** «Vetados» lista `GET /clubs/{id}/bans`, con «Levantar veto» según los permisos.
- **AC-003-13** Al salir (204), el club se elimina de Room y de mis clubes y se navega a la pestaña Clubs.
- **AC-003-14** Logo: `PUT /clubs/{id}/logo` multipart con la parte `clubLogo` (jpeg, png o webp).

**Datos**
- **AC-003-15** Room v3 (`Migration(2,3)` con test):
  - `club_member` pierde `email` y las columnas de estadísticas; `role` y `position` se guardan como el nombre del enum;
  - `club` pierde los campos de horario y de temporada, que pasan a la tabla `club_schedule` (spec 004);
  - **se elimina `fallbackToDestructiveMigration`**.
- **AC-003-16** Se elimina el código muerto: `ClubService`, `KtorClubRepositoryImpl`, `UpdateClubMemberRequestDto` (PATCH de admin), `UploadMemberPhotoUseCase`, `ClubLogoUploadUrlsResponseDTO`, los `Fetch*UseCase` sin uso y los duplicados de globalPosition (`GlobalPositionService`, `KtorGlobalPositionRepositoryImpl`). Los modelos de club de globalPosition pasan a reutilizar los de `feature/club/domain`.

## Preguntas abiertas
- ❓ ¿Se muestra el rol `CAPTAIN` con un distintivo (por ejemplo «C») aunque no tenga permisos? Propuesta: sí, como etiqueta visual.
