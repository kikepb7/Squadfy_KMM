import { FieldValue } from "firebase-admin/firestore";
import { logger } from "firebase-functions/v2";
import { db, messaging } from "../admin";
import { paths } from "../paths";

/** Languages of the pushes (spec 021 APP-RN-P2): English for devices in English, Spanish for everyone else. */
export type Lang = "es" | "en";
export const LANGS: Lang[] = ["es", "en"];

export interface PushText {
  title: string;
  body: string;
}

export interface Push {
  userIds: string[];
  text: Record<Lang, PushText>;
  data: Record<string, string>;
  collapseKey: string;
  /** The club whose mute applies (APP-RN-14, APP-RN-P1); null for personal pushes that ignore it. */
  muteClubId: string | null;
}

const INVALID_TOKEN_CODES = new Set([
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token",
  "messaging/invalid-argument"
]);

/** True inside the Emulator Suite, where nothing must reach real devices. */
export const isEmulator = () => process.env.FUNCTIONS_EMULATOR === "true";

/** The push language of a device locale such as `en-GB` or `es-ES`. */
export const langOf = (locale: unknown): Lang => (typeof locale === "string" && locale.toLowerCase().startsWith("en") ? "en" : "es");

/** Users that muted the club (BE-009). */
export async function mutedUsers(clubId: string, userIds: string[]): Promise<Set<string>> {
  if (userIds.length === 0) return new Set();
  const refs = [...new Set(userIds)].map((uid) => db().doc(paths.clubSettings(uid, clubId)));
  const docs = await db().getAll(...refs);
  return new Set(docs.filter((d) => d.get("muted") === true).map((d) => d.ref.parent.parent!.id));
}

/** The users that will get [push]: its recipients without those who muted its club (APP-RN-P1). */
export async function recipientsOf(push: Push): Promise<string[]> {
  const userIds = [...new Set(push.userIds)];
  if (!push.muteClubId || userIds.length === 0) return userIds;
  const muted = await mutedUsers(push.muteClubId, userIds);
  return userIds.filter((uid) => !muted.has(uid));
}

/**
 * Sends to every device of the users (`users/{uid}/devices`) in its language, deleting tokens FCM reports as gone
 * (backend PushNotificationService). FCM retries temporary failures itself. In the emulator the pushes are only logged
 * and stored in `debugPushes`, so tests can check them and no real phone gets them.
 */
export async function sendToUsers(push: Push) {
  const userIds = await recipientsOf(push);
  if (userIds.length === 0) return;
  if (isEmulator()) {
    logger.info("[push:emulator]", push.data.type, push.text.es.title, userIds.length);
    await db().collection("debugPushes").add({ ...push, userIds, at: FieldValue.serverTimestamp() });
    return;
  }
  const deviceSnapshots = await Promise.all(userIds.map((uid) => db().collection(paths.devices(uid)).get()));
  const tokens = deviceSnapshots.flatMap((snapshot) => snapshot.docs.map((d) => ({ token: d.id, ref: d.ref, lang: langOf(d.get("locale")) })));
  for (const lang of LANGS) {
    const group = tokens.filter((t) => t.lang === lang);
    for (let i = 0; i < group.length; i += 500) {
      const batch = group.slice(i, i + 500);
      const result = await messaging().sendEachForMulticast({
        tokens: batch.map((t) => t.token),
        notification: push.text[lang],
        data: push.data,
        // As the own backend: Android high priority with a collapse key, iOS with sound and a thread per match
        android: { collapseKey: push.collapseKey, priority: "high", notification: { channelId: channelOf(push.data.type) } },
        apns: { payload: { aps: { sound: "default", threadId: push.collapseKey } } }
      });
      await Promise.all(
        result.responses.map((response, index) =>
          !response.success && INVALID_TOKEN_CODES.has(response.error?.code ?? "") ? batch[index].ref.delete() : Promise.resolve()
        )
      );
    }
  }
}

/** The Android channel of a push type; must match `PushNotifier` in the app. */
export function channelOf(type: string | undefined): string {
  if (type === "new_message") return "chat";
  if (type?.startsWith("club.") || type?.startsWith("season.")) return "club";
  if (type?.startsWith("app.")) return "app";
  return "match_updates";
}
