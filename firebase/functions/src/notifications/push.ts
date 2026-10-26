import { FieldValue } from "firebase-admin/firestore";
import { logger } from "firebase-functions/v2";
import { db, messaging } from "../admin";
import { paths } from "../paths";

export interface Push {
  userIds: string[];
  title: string;
  body: string;
  data: Record<string, string>;
  collapseKey: string;
}

const INVALID_TOKEN_CODES = new Set([
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token",
  "messaging/invalid-argument"
]);

/** True inside the Emulator Suite, where nothing must reach real devices. */
export const isEmulator = () => process.env.FUNCTIONS_EMULATOR === "true";

/** Users that muted the club (BE-009); promotions ignore the mute (spec 005 RN-7). */
export async function mutedUsers(clubId: string, userIds: string[]): Promise<Set<string>> {
  if (userIds.length === 0) return new Set();
  const refs = [...new Set(userIds)].map((uid) => db().doc(paths.clubSettings(uid, clubId)));
  const docs = await db().getAll(...refs);
  return new Set(docs.filter((d) => d.get("muted") === true).map((d) => d.ref.parent.parent!.id));
}

/**
 * Sends to every device of the users (`users/{uid}/devices`), deleting tokens FCM reports as gone (backend
 * PushNotificationService). FCM retries temporary failures itself. In the emulator the pushes are only logged and
 * stored in `debugPushes`, so tests can check them and no real phone gets them.
 */
export async function sendToUsers(push: Push) {
  const userIds = [...new Set(push.userIds)];
  if (userIds.length === 0) return;
  if (isEmulator()) {
    logger.info("[push:emulator]", push.data.type, push.title, userIds.length);
    await db().collection("debugPushes").add({ ...push, userIds, at: FieldValue.serverTimestamp() });
    return;
  }
  const deviceSnapshots = await Promise.all(userIds.map((uid) => db().collection(paths.devices(uid)).get()));
  const tokens = deviceSnapshots.flatMap((snapshot) => snapshot.docs.map((d) => ({ token: d.id, ref: d.ref })));
  if (tokens.length === 0) return;
  for (let i = 0; i < tokens.length; i += 500) {
    const batch = tokens.slice(i, i + 500);
    const result = await messaging().sendEachForMulticast({
      tokens: batch.map((t) => t.token),
      notification: { title: push.title, body: push.body },
      data: push.data,
      // As the own backend: Android high priority with a collapse key, iOS with sound and a thread per match
      android: { collapseKey: push.collapseKey, priority: "high" },
      apns: { payload: { aps: { sound: "default", threadId: push.collapseKey } } }
    });
    await Promise.all(
      result.responses.map((response, index) =>
        !response.success && INVALID_TOKEN_CODES.has(response.error?.code ?? "") ? batch[index].ref.delete() : Promise.resolve()
      )
    );
  }
}
