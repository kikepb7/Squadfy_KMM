import { deleteApp, initializeApp, FirebaseApp } from "firebase/app";
import { connectAuthEmulator, createUserWithEmailAndPassword, getAuth } from "firebase/auth";
import { connectFirestoreEmulator, getFirestore } from "firebase/firestore";
import { connectFunctionsEmulator, getFunctions, httpsCallable } from "firebase/functions";

/** Must match firebase.json. The emulators run with `--project demo-squadfy` (`npm run test:emulator`). */
export const PROJECT_ID = "demo-squadfy";
const HOST = "127.0.0.1";

let counter = 0;

/** A fresh client app signed in as a new user, talking only to the emulators. */
export async function newClient(prefix = "user") {
  const app: FirebaseApp = initializeApp({ projectId: PROJECT_ID, apiKey: "demo-key" }, `client-${Date.now()}-${counter++}`);
  const auth = getAuth(app);
  connectAuthEmulator(auth, `http://${HOST}:9098`, { disableWarnings: true });
  const firestore = getFirestore(app);
  connectFirestoreEmulator(firestore, HOST, 8086);
  const functions = getFunctions(app, "europe-west1");
  connectFunctionsEmulator(functions, HOST, 5003);
  const email = `${prefix}.${Date.now()}.${counter}@squadfy.test`;
  const credential = await createUserWithEmailAndPassword(auth, email, "Secret1234");
  const call = async <T = unknown>(name: string, data: unknown = {}) =>
    (await httpsCallable<unknown, T>(functions, name)(data)).data;
  return { app, auth, firestore, uid: credential.user.uid, email, call, close: () => deleteApp(app) };
}

/** Runs [block] and returns the callable error it threw (code + details), failing if nothing was thrown. */
export async function callError(block: () => Promise<unknown>): Promise<{ code: string; details: unknown }> {
  try {
    await block();
  } catch (e) {
    const error = e as { code: string; details: unknown };
    return { code: error.code, details: error.details };
  }
  throw new Error("Expected the call to fail");
}
