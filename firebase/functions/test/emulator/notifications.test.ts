import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { DateTime } from "luxon";
import { getApps, initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { newClient, PROJECT_ID } from "./helpers";

/**
 * Spec 021: who gets which push. In the emulator `sendToUsers` stores each push in `debugPushes` (with its recipients
 * after the club mute) instead of sending it; the admin SDK reads them here (emulators:exec sets the emulator hosts).
 */
type Client = Awaited<ReturnType<typeof newClient>>;
interface Club { id: string; invitationCode: string }
interface DebugPush { userIds: string[]; data: Record<string, string>; text: { es: { title: string }; en: { title: string } } }

const clients: Client[] = [];
const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));
async function user(prefix: string) {
  const client = await newClient(prefix);
  clients.push(client);
  await client.call("completeProfile", { username: `${prefix}${Date.now() % 1_000_000}`.slice(0, 20) });
  return client;
}
afterAll(async () => { await Promise.all(clients.map((c) => c.close())); });

const admin = () => getFirestore(getApps().find((a) => a.name === "notifications-admin") ?? initializeApp({ projectId: PROJECT_ID }, "notifications-admin"));

async function pushes(type: string, clubId: string): Promise<DebugPush[]> {
  const snapshot = await admin().collection("debugPushes").where("data.type", "==", type).get();
  return snapshot.docs.map((d) => d.data() as DebugPush).filter((p) => p.data.clubId === clubId);
}

const DAYS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

describe("Spec 021 pushes", () => {
  let owner: Client;
  let ana: Client;
  let bea: Client;
  let club: Club;

  beforeAll(async () => {
    owner = await user("nown");
    club = await owner.call<Club>("createClub", { name: "Push FC" });
    ana = await user("nana");
    bea = await user("nbea");
    await ana.call("joinClub", { invitationCode: club.invitationCode });
    await bea.call("joinClub", { invitationCode: club.invitationCode });
  });

  it("AC-021-06 the managers learn who joins; a member learns their new role even with the club muted", async () => {
    const joined = await pushes("club.member_joined", club.id);
    expect(joined).toHaveLength(2);
    expect(joined.every((p) => p.userIds.length === 1 && p.userIds[0] === owner.uid)).toBe(true);

    await bea.call("setClubMuted", { clubId: club.id, muted: true });
    await owner.call("changeMemberRole", { clubId: club.id, memberId: `${club.id}_${bea.uid}`, role: "CAPTAIN" });
    const roles = await pushes("club.role_changed", club.id);
    expect(roles.map((p) => p.userIds)).toEqual([[bea.uid]]);
    expect(roles[0].text.en.title).toBe("Push FC: new role");
  });

  it("AC-021-01 a muted member gets no club push", async () => {
    const inThreeDays = DateTime.now().setZone("Europe/Madrid").plus({ days: 3 });
    await owner.call("createSchedule", {
      clubId: club.id, matchDayOfWeek: DAYS[inThreeDays.weekday - 1], matchTime: "20:00", timeZone: "Europe/Madrid", format: "FIVE_A_SIDE"
    });
    await owner.call("runJobsNow", {});
    const opened = await pushes("match.announcement.opened", club.id);
    expect(opened).toHaveLength(1);
    expect(opened[0].userIds).toContain(ana.uid);
    expect(opened[0].userIds).not.toContain(bea.uid);
  });

  it("AC-021-03/04 the called-up members get the match-day reminder once, and the players the result", { timeout: 60_000 }, async () => {
    const kickoffAt = Date.now() + 20_000;
    const extra = await owner.call<{ id: string }>("createExtraMatch", { clubId: club.id, scheduledAt: new Date(kickoffAt).toISOString() });
    await owner.call("enroll", { announcementId: extra.id });
    await ana.call("enroll", { announcementId: extra.id });
    await owner.call("runJobsNow", {});
    await owner.call("runJobsNow", {});
    const reminders = (await pushes("match.reminder", club.id)).filter((p) => p.data.matchId === extra.id);
    expect(reminders).toHaveLength(1);
    expect(new Set(reminders[0].userIds)).toEqual(new Set([owner.uid, ana.uid]));

    const drawn = await owner.call<{ teamA: string[] }>("generateTeams", { matchId: extra.id, mode: "AUTO" });
    await owner.call("addMatchEvent", { matchId: extra.id, clubMemberId: drawn.teamA[0], type: "GOAL" });
    await sleep(Math.max(0, kickoffAt - Date.now()) + 1000);
    await owner.call("completeMatch", { matchId: extra.id });
    const results = (await pushes("match.completed", club.id)).filter((p) => p.data.matchId === extra.id);
    expect(results.flatMap((p) => p.userIds).sort()).toEqual([owner.uid, ana.uid].sort());
    expect(results.every((p) => /1–0|0–1/.test(p.text.es.title))).toBe(true);
  });
});
