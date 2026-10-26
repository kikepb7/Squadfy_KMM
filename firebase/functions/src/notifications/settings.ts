import { onCall } from "firebase-functions/v2/https";
import { db } from "../admin";
import { requireMember } from "../clubs/access";
import { badRequest } from "../errors";
import { body, requireAuth, requireString } from "../guards";
import { paths } from "../paths";

/** Club mute (BE-009): a muted club sends me no match pushes, except my promotion from the waitlist. */
export const getClubNotificationSettings = onCall(async (request) => {
  const uid = requireAuth(request);
  const clubId = requireString(body(request), "clubId");
  await requireMember(clubId, uid);
  const settings = await db().doc(paths.clubSettings(uid, clubId)).get();
  return { clubId, muted: settings.get("muted") === true };
});

export const setClubMuted = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  if (typeof data.muted !== "boolean") badRequest("muted is required", "VALIDATION_ERROR");
  await requireMember(clubId, uid);
  await db().doc(paths.clubSettings(uid, clubId)).set({ muted: data.muted });
  return { clubId, muted: data.muted };
});
