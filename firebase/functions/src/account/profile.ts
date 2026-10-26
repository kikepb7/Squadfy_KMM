import { FieldValue } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { auth, db } from "../admin";
import { conflict } from "../errors";
import { body, requireAuth, requireString } from "../guards";

/** BE-001 register: usernames are 3-20 characters and unique, ignoring case. */
export const USERNAME_MIN = 3;
export const USERNAME_MAX = 20;

/**
 * Creates the public profile `users/{uid}` and reserves its username in `usernames/{lower}` (spec 019 AC-019-04,
 * AC-019-23). Called right after creating the account with email or the first time with Google/Apple. Idempotent for
 * the same user; a username held by someone else is 409 USER_EXISTS, so the app undoes the new account.
 */
export const completeProfile = onCall(async (request) => {
  const uid = requireAuth(request);
  const username = requireString(body(request), "username", USERNAME_MIN, USERNAME_MAX);
  const lower = username.toLowerCase();

  await db().runTransaction(async (tx) => {
    const nameRef = db().doc(`usernames/${lower}`);
    const userRef = db().doc(`users/${uid}`);
    const [name, user] = await Promise.all([tx.get(nameRef), tx.get(userRef)]);
    if (name.exists && name.get("uid") !== uid) conflict("Username already taken", "USER_EXISTS");
    const previous = user.exists ? (user.get("usernameLower") as string | undefined) : undefined;
    if (previous && previous !== lower) tx.delete(db().doc(`usernames/${previous}`));
    tx.set(nameRef, { uid });
    tx.set(
      userRef,
      {
        username,
        usernameLower: lower,
        ...(user.exists ? {} : { pictureUrl: null, createdAt: FieldValue.serverTimestamp() }),
        updatedAt: FieldValue.serverTimestamp()
      },
      { merge: true }
    );
  });
  await auth().updateUser(uid, { displayName: username });
  return { ok: true };
});
