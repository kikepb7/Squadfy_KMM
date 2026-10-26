// Firebase Auth action links (spec 019 T-026): the email templates point to /auth/action. Verification, password
// reset and email recovery are completed here, so they work on any device; the reset can also be done in the app,
// which takes the same `oobCode` (squadfy://reset-password?token=…).
import {
  applyActionCode,
  checkActionCode,
  confirmPasswordReset,
  verifyPasswordResetCode
} from "https://www.gstatic.com/firebasejs/11.10.0/firebase-auth.js";
import { authErrorText, firebaseAuth, show } from "./firebase-web.js";

const params = new URLSearchParams(location.search);
const mode = params.get("mode");
const oobCode = params.get("oobCode");

const title = document.getElementById("title");
const lead = document.getElementById("lead");
const message = document.getElementById("message");
const resetForm = document.getElementById("reset-form");
const resetSubmit = document.getElementById("reset-submit");
const openApp = document.getElementById("open-app");

// Same rule as the app's PasswordValidator
const isValidPassword = (password) => password.length >= 9 && /\d/.test(password) && /\p{Lu}/u.test(password);

function finish(heading, text, kind = "ok", note = "") {
  title.textContent = heading;
  lead.textContent = text;
  resetForm.classList.add("hidden");
  openApp.classList.add("hidden");
  show(message, note, note ? kind : "");
}

async function verifyEmail(auth) {
  await applyActionCode(auth, oobCode);
  finish("Email verificado", "Ya puedes volver a Squadfy y entrar con tu cuenta.");
}

async function resetPassword(auth) {
  const email = await verifyPasswordResetCode(auth, oobCode);
  title.textContent = "Nueva contraseña";
  lead.textContent = `Elige una contraseña nueva para ${email}.`;
  resetForm.classList.remove("hidden");
  // On a phone with the app installed, the reset can be finished there too
  if (/Android|iPhone|iPad/i.test(navigator.userAgent)) {
    openApp.href = `squadfy://reset-password?token=${encodeURIComponent(oobCode)}`;
    openApp.classList.remove("hidden");
  }
  resetForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const password = document.getElementById("new-password").value;
    const repeated = document.getElementById("repeat-password").value;
    if (!isValidPassword(password)) { show(message, "La contraseña necesita al menos 9 caracteres, un número y una mayúscula.", "error"); return; }
    if (password !== repeated) { show(message, "Las contraseñas no coinciden.", "error"); return; }
    resetSubmit.disabled = true;
    try {
      await confirmPasswordReset(auth, oobCode, password);
      finish("Contraseña cambiada", "Ya puedes entrar en Squadfy con tu nueva contraseña.");
    } catch (error) {
      show(message, authErrorText(error), "error");
      resetSubmit.disabled = false;
    }
  });
}

async function recoverEmail(auth) {
  const info = await checkActionCode(auth, oobCode);
  await applyActionCode(auth, oobCode);
  finish(
    "Email recuperado",
    `Tu cuenta vuelve a usar ${info.data.email}.`,
    "ok",
    "Si no fuiste tú quien lo cambió, cambia tu contraseña desde la app («¿Olvidaste tu contraseña?»)."
  );
}

const handlers = { verifyEmail, resetPassword, recoverEmail };

async function main() {
  const handler = handlers[mode];
  if (!handler || !oobCode) {
    finish("Enlace no válido", "Este enlace no es correcto. Pide uno nuevo desde la app.", "error");
    return;
  }
  try {
    await handler(await firebaseAuth());
  } catch (error) {
    finish("No se ha podido completar", authErrorText(error), "error");
  }
}

main();
