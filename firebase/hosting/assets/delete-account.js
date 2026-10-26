// Web account deletion (Google Play requirement; spec 019 AC-019-06). A fresh sign-in proves it is the owner, then
// the Firebase Auth user is deleted; the `onAuthUserDeleted` function erases the data with the same rules as the app.
import {
  GoogleAuthProvider,
  OAuthProvider,
  deleteUser,
  signInWithEmailAndPassword,
  signInWithPopup,
  signOut
} from "https://www.gstatic.com/firebasejs/11.10.0/firebase-auth.js";
import { authErrorText, firebaseAuth, show } from "./firebase-web.js";

const confirmBox = document.getElementById("confirm");
const form = document.getElementById("password-form");
const passwordSubmit = document.getElementById("password-submit");
const googleButton = document.getElementById("google");
const appleButton = document.getElementById("apple");
const message = document.getElementById("message");
const actions = [passwordSubmit, googleButton, appleButton];

let busy = false;
function refreshButtons() {
  actions.forEach((button) => { button.disabled = busy || !confirmBox.checked; });
}
confirmBox.addEventListener("change", refreshButtons);

async function deleteAfter(signIn) {
  if (!confirmBox.checked) { show(message, "Marca la casilla para confirmar.", "error"); return; }
  busy = true;
  refreshButtons();
  show(message, "Comprobando tu cuenta…");
  let auth;
  try {
    auth = await firebaseAuth();
    const { user } = await signIn(auth);
    show(message, "Borrando la cuenta…");
    await deleteUser(user);
    form.reset();
    document.querySelectorAll("#delete-card input, #delete-card button").forEach((element) => { element.disabled = true; });
    show(message, "Tu cuenta se ha borrado. Tus datos personales desaparecen en unos segundos. Ya puedes cerrar esta página.", "ok");
    return;
  } catch (error) {
    show(message, authErrorText(error), "error");
    if (auth?.currentUser) await signOut(auth).catch(() => undefined);
  }
  busy = false;
  refreshButtons();
}

form.addEventListener("submit", (event) => {
  event.preventDefault();
  const email = form.email.value.trim();
  const password = form.password.value;
  if (!email || !password) { show(message, "Escribe tu email y tu contraseña.", "error"); return; }
  deleteAfter((auth) => signInWithEmailAndPassword(auth, email, password));
});

googleButton.addEventListener("click", () => deleteAfter((auth) => signInWithPopup(auth, new GoogleAuthProvider())));
appleButton.addEventListener("click", () => {
  const provider = new OAuthProvider("apple.com");
  provider.addScope("email");
  deleteAfter((auth) => signInWithPopup(auth, provider));
});
