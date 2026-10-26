import { FieldValue } from "firebase-admin/firestore";
import { db } from "./admin";

export type LiveScope = "MATCH" | "SCHEDULE" | "ABSENCES";

/**
 * Tells the app what changed in a club (backend LiveUpdatePublisher, BE-012 RN-A1): the app listens to
 * `clubs/{clubId}/live/changes` and reloads that part, as it did with `CLUB_DATA_CHANGED` over the WebSocket.
 * Called after the change is committed; a lost notice only delays a refresh.
 */
export async function publishChange(clubId: string, scope: LiveScope, matchId: string | null = null) {
  await db().doc(`clubs/${clubId}/live/changes`).set({
    scope,
    matchId,
    at: FieldValue.serverTimestamp(),
    seq: FieldValue.increment(1)
  }, { merge: true });
}
