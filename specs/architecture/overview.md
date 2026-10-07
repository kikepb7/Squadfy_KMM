# Arquitectura (estado a 2026-10-07)

## Sistema
```
┌──────────────── Squadfy_App (KMP) ────────────────┐  HTTPS /api/v1  ┌──────── Squadfy_Backend (monolito modular) ────────┐
│ composeApp (Android + iOS, NavigationRoot, Koin)   │ ──────────────▶ │ app: seguridad JWT, Flyway V1–V6, OpenAPI (dev)    │
│ core: domain · data (Ktor, auth, errores) ·        │  WSS /ws/chat   │ user · club · match · chat · notification · common │
│       presentation · designsystem                  │ ◀────────────── │ PostgreSQL (un esquema por módulo) · RabbitMQ      │
│ feature: auth · chat · club · globalPosition       │   FCM push      │ Redis · Supabase Storage · Firebase Admin          │
└────────────────────────────────────────────────────┘ ◀────────────── └──── Kotlin 2.3 · JVM 21 · Spring Boot 4.1 ────────┘
```
La documentación completa del backend está en `../Squadfy_Backend/docs/BACKEND.md`, y sus specs en `../Squadfy_Backend/specs/` (BE-001…BE-007, todas hechas; el despliegue está pendiente).

## Backend: procesos que afectan a la app
| Proceso (UTC) | Efecto visible en la app |
|---|---|
| Planificación cada hora | Aparece el siguiente partido junto con su convocatoria → `announcements/current` deja de dar 404 |
| Cierre de convocatorias cada 5 min | `status` pasa a CLOSED hasta 5 min después de `closesAt` (APP-RN-01) y **se publican los equipos** |
| Push de convocatoria cada 5 min | `match.announcement.opened` / `closing_soon` |

## App: módulos y estado
| Módulo | Estado | Trabajo pendiente |
|---|---|---|
| `core/data` | Ktor + bearer + refresh y `Result`/`DataError` | Base `/api/v1`, `RemoteError` con `code`, `coerceInputValues`, logging por tipo de build (002, 011) |
| `core/domain` | `Result`, `DataError`, `AuthInfo` | `UserModel` sin `profilePictureUrl` obligatorio; `Clock` (002) |
| `feature/auth` | Flujos completos | Rutas v1, `EMAIL_NOT_VERIFIED`, 429, deep link `squadfy://reset-password` (002) |
| `feature/chat` | Completo, 151 tests | Rutas v1, `/users`, `/devices`, `creator` (002) |
| `feature/club` | UI de un contrato que no existe | Migración completa a v1 (003–008) |
| `feature/globalPosition` | Clubes reales, mocks | Inicio real (010); unificar sus modelos con los de club |
| `feature/economy`, `feature/onboarding` | Vacíos | Se retiran del build en el MVP (011) |

### Estructura objetivo de `feature/club`
```
domain/   model/ (Club, ClubMember, ClubMemberRole, PlayerPosition, ClubSchedule, MatchFormat,
                  Match, MatchStatus, MatchEvent, MatchAnnouncement, CurrentAnnouncement, MyEnrollmentStatus,
                  TeamBalance, RatingEntry, StatsEntry, StatsSortBy)
          policy/ (AnnouncementWindowPolicy, MemberPermissions)       ← reglas APP-RN-01/04/05, Kotlin puro
          repository/ (ClubRepository, ScheduleRepository, MatchRepository, AnnouncementRepository, RankingRepository)
          error/  (ClubError: tipado a partir de RemoteError.code)
data/     dto/ v1 · mappers · datasource/ (Ktor*RemoteDataSource) · repository/ (OfflineFirst para club, miembros y horario; InMemory para el ciclo de partido)
database/ Room v3: club, club_member (sin stats ni email), club_schedule
presentation/ clubs (lista) · create · join · detail (pestañas: Partido · Clasificación · Miembros · Ajustes)
              · announcement · match (detalle y gestión) · member · settings · schedule
```

## Flujo del MVP
```
Owner crea club ─▶ comparte código ─▶ miembros se unen (APP-RN-12: navegar al club)
      │
Gestor crea horario (día, hora, zona, formato, duración) ─▶ backend planifica el partido + convocatoria OPEN
      │                                                         └─ push «convocatoria abierta»
Miembros: apuntarse / desapuntarse (lista de espera automática) ─ push «recordatorio 24 h» / «tienes plaza»
      │
22:00 del día anterior: el backend cierra y publica los equipos ─ push «equipos publicados»
      │      gestor: ver equilibrio · rectificar AUTO/MANUAL
Día del partido: gestor registra eventos y minutos ─▶ cerrar ─▶ ratings + estadísticas ─▶ clasificaciones
      │      (reabrir el último partido para corregir)
Al día siguiente 00:00: se abre la siguiente convocatoria
```
