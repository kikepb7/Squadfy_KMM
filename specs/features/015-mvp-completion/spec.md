# 015 · Cierre del MVP y paridad con el backend (BE 011–013)

- **Estado:** Done (2026-10-08) (el owner lo delega el 2026-10-08: «elige tú D-12 y D-13 y continúa hasta finalizar el MVP»)
- **Depende de:** 002–014. Backend: specs 011 (flags y verificación), 012 (tiempo real, estadísticas por periodo, foto por club, búsqueda) y 013 (Render), ya en `master` del backend.

## Problema / objetivo
El backend cerró su MVP con funciones que la app no consume, y quedaban dos decisiones de tienda abiertas (D-12 y D-13). Esta spec deja la app **igualada con el backend y lista para la 1.0.0**.

## Reglas
- Backend: **BE-011** RN-A4 (`GET /features`), RN-B (página de verificación) y RN-C1 (baja solo de los dispositivos propios); **BE-012** RN-A (`CLUB_DATA_CHANGED`), RN-B (`from`/`to`), RN-C (foto por club, `pictureUrl`), RN-D (`/users/search`) y RN-E (`USER_EXISTS`); **BE-013** (staging en `release` y producción en `master`).
- **APP-RN-20** (D-13): el chat entre usuarios se oculta en iOS en PRO hasta que haya denunciar y bloquear (Apple 1.2). El Perfil (cuenta, privacidad, informes de fallos y cierre de sesión) no depende del chat.

## Criterios de aceptación
- **AC-015-01** (D-12) En tema claro, el color de marca usado como texto cumple AA (≥ 4,5:1).
- **AC-015-02** (D-13) Con `CHAT` apagado, la pestaña Chat no aparece. Por defecto: activo en PRE y en PRO Android, apagado en PRO iOS.
- **AC-015-03** El Perfil se abre desde el engranaje de Inicio en las dos plataformas y permite cerrar sesión, con confirmación. En PRE, además, enlaza con la pantalla de feature flags.
- **AC-015-04** (BE-012 RN-D) Al crear un chat, la búsqueda de usuarios es parcial (`/users/search?q=`, mínimo 2 caracteres) y muestra hasta 20 resultados.
- **AC-015-05** (BE-012 RN-C) Los miembros se muestran con `pictureUrl` (la foto del club o, si no hay, la del perfil). Desde «Mi ficha», el jugador puede subir y quitar su foto para ese club.
- **AC-015-06** (BE-012 RN-A) Con el WebSocket conectado, `CLUB_DATA_CHANGED` refresca la pestaña del club afectada (Partido, horario o ausencias) sin intervención del usuario.
- **AC-015-07** (BE-012 RN-B) Las estadísticas se pueden filtrar por periodo (todo, este año y últimos 30 días) con `from` y `to`; el rating no se filtra.
- **AC-015-08** (BE-013) Las builds de QA (`release/*`) apuntan a staging y las de producción (tag `v*`) a producción.
- **AC-015-09** Los mocks de Inicio (`HOME_RECENT_MATCHES` y `HOME_NEWS`) se eliminan, junto con sus flags.
- **AC-015-10** El mapa de contrato (`api-v1.md`) y el gap analysis reflejan BE 011–013.

## Fuera de alcance
- Denunciar y bloquear en el chat (necesita una spec del backend; se propone en el roadmap).
- Rango de fechas libre en las estadísticas: el backend lo admite, pero en la 1.0.0 la app ofrece periodos predefinidos.
- Leer `GET /features`: la app ya sabe por la respuesta del registro si la verificación está desactivada.

## Preguntas abiertas
- (ninguna)
