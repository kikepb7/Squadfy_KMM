# ADR-0008 · Sin API key de cliente; secretos de release en un entorno protegido

- **Estado:** Aceptado (el owner delegó la decisión el 2026-10-08; cierra D-2)
- **Fecha:** 2026-10-08

## Contexto
La app compilaba una `API_KEY` (BuildKonfig) y la enviaba como `x-api-key` en cada petición HTTP y en el handshake del WebSocket. Era obligatoria para compilar, así que había que repartirla por `local.properties`, por el CI y por la release.

Al revisarlo:
- **El backend v1 no la lee.** No aparece en ningún controlador, filtro ni configuración del backend (comprobado en `Squadfy_Backend` el 2026-10-07 y otra vez el 2026-10-08). Era un resto de la plantilla inicial.
- **No puede ser secreta.** Todo lo que se compila en la app se puede extraer del APK o IPA con herramientas de descompilación, y R8 no lo impide. Una clave embebida identifica a la app, pero no la autentica.
- **El repo es público.** El CI sube el APK debug como artefacto, y cualquier usuario de GitHub puede descargarlo junto con la clave que lleve dentro.

## Opciones
1. Mantenerla como secret de GitHub. Añade un secreto que gestionar y rotar, no aporta seguridad y es fácil filtrarla.
2. Ofuscarla (NDK, cifrado en el binario). Lo único que hace es retrasar la extracción unos minutos; es seguridad por oscuridad.
3. **Eliminarla.** Los usuarios se autentican con JWT (access token de 15 min más un refresh token que rota). Si algún día hace falta autenticar a la app (por abuso, scraping o bots), se usa atestación verificada en el servidor: Play Integrity en Android y App Attest en iOS.

## Decisión
**Opción 3.**
- Se elimina `API_KEY` de la convention de BuildKonfig, de las cabeceras HTTP y WebSocket, de los workflows y de la documentación. Ya no hace falta en `local.properties`; la línea, si existe, se ignora.
- Los secretos que sí importan en la release (firma, cuenta de servicio de Play, URLs de producción y `google-services.json` de producción) van en el **Environment `production`** de GitHub:
  - con *required reviewers*, para que cada release pida aprobación antes de poder leerlos;
  - limitado a los tags `v*` y a la rama `main`;
  - el CI normal no los ve y compila con valores de relleno.
- Se propone como spec opcional del backend, después del MVP: **atestación de la app** (Play Integrity / App Attest).

## Consecuencias
- Hay un secreto menos que dar de alta, rotar y proteger, y una cabecera menos en cada petición.
- Un cliente que no sea la app puede llamar a los endpoints públicos (registro, login), igual que antes, porque la clave no se comprobaba. La protección real son los límites por cuenta e IP del backend (su spec 010) y la autenticación JWT.
- Si el backend empieza a exigir una cabecera de cliente, tiene que ser con atestación, no con una clave estática.
