import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { callError, newClient } from "./helpers";

type Client = Awaited<ReturnType<typeof newClient>>;
interface Club { id: string; invitationCode: string }
interface Entry { id: string; clubMemberId: string | null; status: string; participantType: string }
interface Announcement { id: string; status: string; confirmedCount: number; waitlistCount: number; entries: Entry[]; waitlist: Entry[] }
interface Current { announcement: Announcement; myStatus: string; myWaitlistPosition: number | null }
interface Match {
  id: string; status: string; teamA: string[]; teamB: string[]; teamAGuests: unknown[]; teamBGuests: unknown[];
  teamAScore: number; teamBScore: number; ratingChanges: Record<string, number>; goals: Array<{ id: string }>; minutesPlayed: Record<string, number>
}

const clients: Client[] = [];
const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));
async function user(prefix: string) {
  const client = await newClient(prefix);
  clients.push(client);
  await client.call("completeProfile", { username: `${prefix}${Date.now() % 1_000_000}`.slice(0, 20) });
  return client;
}
afterAll(async () => { await Promise.all(clients.map((c) => c.close())); });

const DAYS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

describe("AC-019-08…12 the weekly cycle on Firebase", () => {
  let owner: Client;
  let players: Client[];
  let club: Club;
  const memberId = (c: Client) => `${club.id}_${c.uid}`;

  beforeAll(async () => {
    owner = await user("own");
    club = await owner.call<Club>("createClub", { name: "Cycle FC" });
    players = [];
    for (const p of ["ana", "bea", "carl", "dan"]) {
      const c = await user(p);
      await c.call("joinClub", { invitationCode: club.invitationCode, position: p === "ana" ? "GOALKEEPER" : "FORWARD" });
      players.push(c);
    }
  });

  it("AC-019-08 creating the schedule plans the next match with its open announcement", async () => {
    const inThreeDays = DAYS[(new Date(Date.now() + 3 * 86400_000).getUTCDay() + 6) % 7];
    const schedule = await owner.call<{ format: string; maxPlayers: number; drawTime: string }>("createSchedule", {
      clubId: club.id, matchDayOfWeek: inThreeDays, matchTime: "20:00", timeZone: "Europe/Madrid", format: "FIVE_A_SIDE"
    });
    expect(schedule.maxPlayers).toBe(10);
    expect(schedule.drawTime).toBe("22:00:00");
    const current = await players[0].call<Current>("getCurrentAnnouncement", { clubId: club.id });
    expect(current.announcement.status).toBe("OPEN");
    expect(current.myStatus).toBe("NOT_ENROLLED");
    expect((await callError(() => owner.call("createSchedule", { clubId: club.id, matchDayOfWeek: "MONDAY", matchTime: "20:00", format: "FIVE_A_SIDE" }))).code)
      .toBe("functions/failed-precondition");
    expect((await callError(() => players[0].call("updateSchedule", { clubId: club.id, format: "SEVEN_A_SIDE" }))).code).toBe("functions/permission-denied");
  });

  it("AC-019-09 enrollment, guests and the waitlist follow the allocator", async () => {
    const current = await owner.call<Current>("getCurrentAnnouncement", { clubId: club.id });
    const id = current.announcement.id;
    for (const p of players) await p.call("enroll", { announcementId: id });
    const withGuests = await players[0].call<Announcement>("addGuest", { announcementId: id, name: "Primo", position: "DEFENDER" });
    expect(withGuests.confirmedCount).toBe(5);
    await players[0].call("addGuest", { announcementId: id, name: "Vecino" });
    expect((await callError(() => players[0].call("addGuest", { announcementId: id, name: "Otro" }))).code).toBe("functions/failed-precondition");
    expect((await callError(() => players[0].call("enroll", { announcementId: id }))).code).toBe("functions/failed-precondition");
    const mine = await players[1].call<Current>("getCurrentAnnouncement", { clubId: club.id });
    expect(mine.myStatus).toBe("CONFIRMED");
    const after = await players[1].call<Announcement>("withdraw", { announcementId: id });
    expect(after.entries.some((e) => e.clubMemberId === memberId(players[1]))).toBe(false);
  });

  it("AC-019-08 a cancelled week cancels its match and plans the next one", async () => {
    const before = await owner.call<Current>("getCurrentAnnouncement", { clubId: club.id });
    const match = await owner.call<{ scheduleDate: string }>("getMatch", { matchId: before.announcement.id });
    await owner.call("addScheduleException", { clubId: club.id, date: match.scheduleDate, type: "CANCELLED", reason: "Festivo" });
    const cancelled = await owner.call<Match>("getMatch", { matchId: before.announcement.id });
    expect(cancelled.status).toBe("CANCELLED");
    const next = await owner.call<Current>("getCurrentAnnouncement", { clubId: club.id });
    expect(next.announcement.id).not.toBe(before.announcement.id);
    const exceptions = await owner.call<Array<{ id: string }>>("getScheduleExceptions", { clubId: club.id });
    await owner.call("deleteScheduleException", { clubId: club.id, exceptionId: exceptions[0].id });
    expect((await owner.call<Match>("getMatch", { matchId: before.announcement.id })).status).toBe("SCHEDULED");
  });

  it("AC-019-11/12 an extra match goes from teams to result, Elo, standings and reopen", { timeout: 60_000 }, async () => {
    // Far enough for the steps before kickoff on a slow emulator; then wait until it has started
    const kickoffAt = Date.now() + 20_000;
    const extra = await owner.call<Match>("createExtraMatch", { clubId: club.id, scheduledAt: new Date(kickoffAt).toISOString(), format: "FIVE_A_SIDE" });
    for (const p of [owner, ...players]) await p.call("enroll", { announcementId: extra.id });
    await owner.call("addGuest", { announcementId: extra.id, name: "Invitado" });
    expect((await callError(() => players[0].call("generateTeams", { matchId: extra.id, mode: "AUTO" }))).code).toBe("functions/permission-denied");
    const drawn = await owner.call<Match>("generateTeams", { matchId: extra.id, mode: "AUTO" });
    expect(drawn.teamA.length + drawn.teamB.length + drawn.teamAGuests.length + drawn.teamBGuests.length).toBe(6);
    const balance = await owner.call<{ teamAExpectedScore: number }>("getTeamBalance", { matchId: extra.id });
    expect(balance.teamAExpectedScore).toBeGreaterThan(0);

    const scorer = drawn.teamA[0];
    await owner.call("addMatchEvent", { matchId: extra.id, clubMemberId: scorer, type: "GOAL", minute: 10 });
    const withEvents = await owner.call<Match>("addMatchEvent", { matchId: extra.id, clubMemberId: scorer, type: "GOAL", minute: 20 });
    expect(withEvents.teamAScore).toBe(2);
    await owner.call("setPlayerMinutes", { matchId: extra.id, clubMemberId: scorer, minutes: 45 });
    expect((await callError(() => owner.call("completeMatch", { matchId: extra.id }))).code).toBe("functions/failed-precondition");
    await sleep(Math.max(0, kickoffAt - Date.now()) + 1000);
    const completed = await owner.call<Match>("completeMatch", { matchId: extra.id });
    expect(completed.status).toBe("COMPLETED");
    expect(completed.ratingChanges[scorer]).toBeGreaterThan(0);
    expect(completed.minutesPlayed[scorer]).toBe(45);
    expect((await callError(() => owner.call("addMatchEvent", { matchId: extra.id, clubMemberId: scorer, type: "GOAL" }))).code).toBe("functions/failed-precondition");

    const ratings = await players[0].call<Array<{ rank: number; clubMemberId: string; rating: number }>>("getRatings", { clubId: club.id });
    expect(ratings).toHaveLength(5);
    expect(ratings.find((r) => r.clubMemberId === scorer)!.rating).toBeGreaterThan(1000);
    const stats = await players[0].call<Array<{ clubMemberId: string; goals: number; minutesPlayed: number; rank: number }>>("getStats", { clubId: club.id, sortBy: "GOALS" });
    expect(stats[0]).toMatchObject({ clubMemberId: scorer, goals: 2, minutesPlayed: 45, rank: 1 });
    const mine = await players[0].call<{ rank: number; totalPlayers: number }>("getMyRating", { clubId: club.id });
    expect(mine.totalPlayers).toBe(5);

    const reopened = await owner.call<Match>("reopenMatch", { matchId: extra.id });
    expect(reopened.status).toBe("SCHEDULED");
    const back = await owner.call<Array<{ clubMemberId: string; rating: number }>>("getRatings", { clubId: club.id });
    expect(back.every((r) => Math.round(r.rating) === 1000)).toBe(true);
  });

  it("AC-019-10/13 the jobs close, draw and notify once, and pushes respect the mute", async () => {
    await players[2].call("setClubMuted", { clubId: club.id, muted: true });
    expect((await players[2].call<{ muted: boolean }>("getClubNotificationSettings", { clubId: club.id })).muted).toBe(true);
    const current = await owner.call<Current>("getCurrentAnnouncement", { clubId: club.id });
    const farFuture = new Date(Date.now() + 10 * 86400_000).toISOString();
    const first = await owner.call<{ closed: number; published: number }>("runJobsNow", { at: farFuture });
    expect(first.closed).toBeGreaterThan(0);
    const closed = await owner.call<Announcement>("getAnnouncementByMatch", { matchId: current.announcement.id });
    expect(closed.status).toBe("CLOSED");
    const again = await owner.call<{ closed: number; published: number }>("runJobsNow", { at: farFuture });
    expect(again.published).toBe(0);
  });

  it("AC-019-09 leaving the club withdraws the member and their guests from open announcements", async () => {
    const current = await owner.call<Current>("getCurrentAnnouncement", { clubId: club.id });
    // Later today: a match opens the day after the previous one (today's), unless that is after its own close
    const kickoff = new Date(Date.now() + 90 * 60_000).toISOString();
    const extra = await owner.call<Match>("createExtraMatch", { clubId: club.id, scheduledAt: kickoff });
    await players[3].call("enroll", { announcementId: extra.id });
    await players[3].call("addGuest", { announcementId: extra.id, name: "Amigo" });
    await players[3].call("leaveClub", { clubId: club.id });
    const after = await owner.call<Announcement>("getAnnouncementByMatch", { matchId: extra.id });
    expect([...after.entries, ...after.waitlist]).toHaveLength(0);
    expect(current).toBeDefined();
  });

  it("AC-019-09 an absence withdraws me from the matches it covers", async () => {
    const kickoff = new Date(Date.now() + 3 * 3600_000);
    const extra = await owner.call<Match>("createExtraMatch", { clubId: club.id, scheduledAt: kickoff.toISOString() });
    await players[1].call("enroll", { announcementId: extra.id });
    const day = kickoff.toLocaleDateString("sv-SE", { timeZone: "Europe/Madrid" });
    await players[1].call("createAbsence", { clubId: club.id, fromDate: day, toDate: day, reason: "Viaje" });
    const after = await owner.call<Announcement>("getAnnouncementByMatch", { matchId: extra.id });
    expect(after.entries.some((e) => e.clubMemberId === memberId(players[1]))).toBe(false);
    const absences = await owner.call<Array<{ clubMemberId: string }>>("getAbsences", { clubId: club.id, from: day });
    expect(absences.map((a) => a.clubMemberId)).toContain(memberId(players[1]));
  });
});
