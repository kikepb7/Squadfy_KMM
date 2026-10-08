# 017 · Coherencia de la interfaz: estados de carga, barra superior y barra de estado

- **Estado:** Done (2026-10-08). Lo pidió el owner tras revisar la 016: en varias pantallas aparecía el loader al entrar o la pantalla no cargaba bien; las barras superiores no eran iguales y la barra de estado de Android no seguía a la app.
- **Reglas:** solo presentación (APP-RN). No cambia ninguna regla del backend ni el contrato.

## Criterios de aceptación
- **AC-017-01 Recargas silenciosas.** Dado que una pantalla ya muestra datos, cuando se recarga sola (al abrirse, al volver a primer plano, por un push, por `CLUB_DATA_CHANGED` o por la visibilidad), entonces no aparece el indicador de pull-to-refresh ni se repite el snackbar de error. Solo el gesto de pull enciende el indicador, que se apaga cuando llega la respuesta. Cada recarga cancela la anterior: gana la última.
- **AC-017-02 Primera carga.** Dado que no hay nada que enseñar (sin caché), mientras llega la primera respuesta la pantalla muestra el loader (`SquadfyLoadingIndicator`), no el estado vacío. Si la primera carga falla, se ofrece reintentar (`LoadErrorCard`) en lugar de una pantalla en blanco. La caché vacía solo significa «no hay clubes» cuando el primer fetch ha contestado.
- **AC-017-03 Cambios sin guardar.** Una recarga del horario no pisa un formulario con cambios sin guardar.
- **AC-017-04 Una sola barra superior.** Todas las pantallas usan `SquadfyTopBar`: misma superficie, altura, divisor, botones de 38 dp (48 dp táctiles) y título centrado. El chat también (lista: título y engranaje de perfil; detalle: el chat como título, volver y menú de opciones). En la lista de chats, cerrar sesión pasa al perfil, igual que en Inicio.
- **AC-017-05 Barra de estado coherente (edge to edge).** La barra de estado muestra el color de la pantalla de debajo: la superficie de la barra superior en las pantallas con barra, y el fondo de marca en el login, el registro y los resultados de auth. Los iconos se adaptan: claros sobre fondo oscuro y oscuros sobre fondo claro, también al volver atrás. La barra de navegación del sistema deja ver el fondo de la pantalla y el contenido no queda debajo.

## Decisiones
- El `Scaffold` raíz (`NavigationRoot`) ya no reserva las barras del sistema, solo el espacio de la barra inferior (y lo consume). Cada pantalla pinta detrás de la barra de estado y se aparta de ella con su `SquadfyTopBar`.
- Los iconos de la barra de estado los fija `StatusBarIconsEffect` (expect/actual). En iOS no hace nada: el sistema sigue la apariencia clara u oscura, que coincide con la barra superior. En las pantallas de auth con fondo oscuro en tema claro, iOS mantiene los iconos oscuros (pendiente si se quiere igualar).
- Olvidé la contraseña y nueva contraseña muestran el escudo en la cabecera, como login y registro, en vez de una franja vacía.
