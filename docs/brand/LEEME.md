# Squadfy · Kit de marca

**Menos papeleo. Más fútbol.**

## Concepto
El isotipo es un **escudo de club** cruzado por la **línea de medio campo**. La "S" se forma con dos medios círculos desplazados: el **círculo central** del campo, partido y movido, como el saque inicial. Club + campo + Squadfy en un solo símbolo. El logotipo "squadfy" está dibujado a medida con el mismo trazo redondeado. Su "s" es la misma del escudo.

## Colores
| Nombre | HEX | Uso |
|---|---|---|
| Night Pitch | `#0A2E22` | Color principal, fondos oscuros y texto sobre claro |
| Squad Green | `#13804F` | Escudo sobre fondo oscuro, botones, éxito |
| Peto Lime | `#C8F53C` | Acento (la "S", llamadas a la acción, datos destacados). **Nunca como texto sobre fondo claro** |
| Chalk | `#F3F5EF` | Fondo claro y texto sobre oscuro |
| Ink | `#0B1410` | Texto y versión monocromo |

Semánticos: amarilla `#F5B700` (aviso) · roja `#D93636` (error) · info `#2F6FEB`.

## Tipografía
**Poppins** (Google Fonts, licencia OFL). Bold 700 para titulares, Medium 500 para la interfaz y Regular 400 para el texto. Los ficheros están en `06-fonts/`.

## Versiones del logo
- `color`: sobre fondos claros (escudo Night Pitch, S lima, texto Night Pitch).
- `reverse`: sobre fondos oscuros (escudo Squad Green, S lima, texto Chalk).
- `mono-black` / `mono-white`: impresión a una tinta, bordados, sellos y marcas de agua.
- Formatos: **isotipo**, **horizontal** (uso principal), **vertical** y **wordmark**.

**Área de respeto:** deja alrededor del logo un espacio libre igual a la altura de la "S" del escudo.
**Tamaño mínimo:** horizontal 96 px / 25 mm de ancho; isotipo 16 px.
**No hagas:** deformar, rotar, cambiar colores fuera de la paleta, poner sombras o colocar el logo color sobre fotos con poco contraste (usa mono-white).

## Estructura
```
01-logo/        SVG (vectorial, sin fuentes) y PNG transparentes a varios tamaños
02-app-icons/
  ios/AppIcon.appiconset/   Listo para Xcode (1024 + variantes dark y tinted de iOS 18)
  ios/legacy-sizes/         Tamaños sueltos para proyectos antiguos
  android/res/              Copia la carpeta en app/src/main/res (icono adaptativo + monocromo de Android 13)
  android/playstore-icon-512.png
  web/                      favicon.ico/.svg, apple-touch-icon, iconos PWA, site.webmanifest, head-snippet.html
  _master/                  SVG maestros de todos los iconos
03-splash/      Splash 2732×2732 (claro y oscuro, válido para Capacitor, Flutter y RN) + icono splash de Android 12
04-social/      OG image, X, LinkedIn, Facebook, YouTube, Instagram (post y story), avatar, Google Play feature, firma de email
05-tokens/      tokens.json · tokens.css · tokens.ts · squadfy_colors.dart (Flutter)
06-fonts/       Poppins
```

### Frameworks
- **Flutter:** `flutter_launcher_icons` con `02-app-icons/_master` (o copia `android/res` e `ios/AppIcon.appiconset`). Para el splash usa `flutter_native_splash` con `03-splash`.
- **React Native / Expo:** `icon` = `ios/AppIcon.appiconset/AppIcon-1024.png`; `adaptiveIcon.foregroundImage` = `mipmap-xxxhdpi/ic_launcher_foreground.png`; `backgroundColor` = `#0A2E22`.
- **Capacitor/Ionic:** `@capacitor/assets` con `AppIcon-1024.png` y `splash-2732-*.png`.
