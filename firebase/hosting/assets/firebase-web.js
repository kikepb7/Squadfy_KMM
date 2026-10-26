// Firebase Auth for the web pages (spec 019 T-026). On Hosting the config comes from the reserved
// /__/firebase/init.json (it needs a Web app registered in the project); served locally, the pages use the
// Auth emulator of `npm run serve`, so trying them never touches the real project.
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.10.0/firebase-app.js";
import { connectAuthEmulator, getAuth } from "https://www.gstatic.com/firebasejs/11.10.0/firebase-auth.js";

export const isLocal = ["localhost", "127.0.0.1"].includes(location.hostname);
const AUTH_EMULATOR_PORT = 9098;

async function loadConfig() {
  if (isLocal) return { apiKey: "demo-key", projectId: "squadfy-5233d", authDomain: location.host };
  const response = await fetch("/__/firebase/init.json");
  if (!response.ok) throw new Error("Firebase web config unavailable");
  return response.json();
}

let authPromise;
export function firebaseAuth() {
  authPromise ??= loadConfig().then((config) => {
    const auth = getAuth(initializeApp(config));
    auth.languageCode = "es";
    if (isLocal) connectAuthEmulator(auth, `http://${location.hostname}:${AUTH_EMULATOR_PORT}`, { disableWarnings: true });
    return auth;
  });
  return authPromise;
}

/** Shows [text] in the status element with the given kind ("ok", "error" or ""). */
export function show(element, text, kind = "") {
  element.textContent = text;
  element.className = `message ${kind}`.trim();
}

/** Spanish text for the Firebase Auth error codes these pages can meet. */
export function authErrorText(error) {
  switch (error?.code) {
    case "auth/invalid-credential":
    case "auth/wrong-password":
    case "auth/user-not-found":
    case "auth/invalid-email":
      return "El email o la contraseña no son correctos.";
    case "auth/too-many-requests":
      return "Demasiados intentos. Espera unos minutos y vuelve a probar.";
    case "auth/popup-closed-by-user":
    case "auth/cancelled-popup-request":
      return "Has cerrado la ventana de inicio de sesión.";
    case "auth/popup-blocked":
      return "El navegador ha bloqueado la ventana de inicio de sesión. Permite las ventanas emergentes e inténtalo de nuevo.";
    case "auth/operation-not-allowed":
      return "Este método de acceso no está disponible en la web. Borra la cuenta desde la app.";
    case "auth/expired-action-code":
      return "El enlace ha caducado. Pide uno nuevo desde la app.";
    case "auth/invalid-action-code":
      return "El enlace no es válido o ya se ha usado. Pide uno nuevo desde la app.";
    case "auth/user-disabled":
      return "Esta cuenta está desactivada.";
    case "auth/weak-password":
      return "La contraseña es demasiado débil.";
    case "auth/network-request-failed":
      return "No hay conexión. Comprueba tu red e inténtalo de nuevo.";
    default:
      return "Algo ha fallado. Inténtalo de nuevo más tarde.";
  }
}
