import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { DateTime } from "luxon";
import { initialSeason, seasonIdOf } from "../../src/domain/season";
import { callError, newClient } from "./helpers";

type Client = Awaited<ReturnType<typeof newClient>>;
interface Club { id: string; invitationCode: string }
interface Season { id: string; name: string; startsOn: string; endsOn: string; status: string }
interface Rating { clubMemberId: string; rating: number; matchesRated: number }
interface Exception { id: string; date: string; type: string; reason: string | null }

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
const today = () => DateTime.now().setZone("Europe/Madrid").toISODate() as string;

describe("Spec 020 seasons and schedule exceptions", () => {
  let owner: Client;
  let player: Client;
  let club: Club;
  const expected = initialSeason(today());

  beforeAll(async () => {
    owner = await user("sown");
    club = await owner.call<Club>("createClub", { name: "Season FC" });
    player = await user("spla");
    await player.call("joinClub", { invitationCode: club.invitationCode });
  });

  it("AC-020-01/02 a new club has its season, from September to June", async () => {
    const seasons = await player.call<Season[]>("getSeasons", { clubId: club.id });
    expect(seasons).toHaveLength(1);
    expect(seasons[0]).toMatchObject({ id: seasonIdOf(expected.startYear), startsOn: expected.startsOn, endsOn: expected.endsOn });
    expect(seasons[0].status).toBe(today() >= expected.startsOn ? "ACTIVE" : "UPCOMING");
  });

  it("AC-020-03 managers pick the days within September and June; players cannot", async () => {
    const id = seasonIdOf(expected.startYear);
    const endsOn = `${expected.startYear + 1}-06-15`;
    const updated = await owner.call<Season>("updateSeason", { clubId: club.id, seasonId: id, startsOn: expected.startsOn, endsOn });
    expect(updated.endsOn).toBe(endsOn);
    const october = await callError(() => owner.call("updateSeason", { clubId: club.id, seasonId: id, startsOn: `${expected.startYear}-10-01`, endsOn }));
    expect(october.code).toBe("functions/invalid-argument");
    expect((october.details as { code: string }).code).toBe("VALIDATION_ERROR");
    const july = await callError(() => owner.call("updateSeason", { clubId: club.id, seasonId: id, startsOn: expected.startsOn, endsOn: `${expected.startYear + 1}-07-01` }));
    expect(july.code).toBe("functions/invalid-argument");
    expect((await callError(() => player.call("updateSeason", { clubId: club.id, seasonId: id, startsOn: expected.startsOn, endsOn }))).code)
      .toBe("functions/permission-denied");
  });

  it("AC-020-08 an exception needs a reason; D-20 a week only moves within itself; a new one replaces the old", async () => {
    const inThreeDays = DateTime.now().setZone("Europe/Madrid").plus({ days: 3 });
    await owner.call("createSchedule", {
      clubId: club.id, matchDayOfWeek: DAYS[inThreeDays.weekday - 1], matchTime: "20:00", timeZone: "Europe/Madrid", format: "FIVE_A_SIDE"
    });
    const date = inThreeDays.toISODate() as string;
    const noReason = await callError(() => owner.call("addScheduleException", { clubId: club.id, date, type: "CANCELLED" }));
    expect(noReason.code).toBe("functions/invalid-argument");
    expect((noReason.details as { code: string }).code).toBe("VALIDATION_ERROR");
    const blank = await callError(() => owner.call("addScheduleException", { clubId: club.id, date, type: "CANCELLED", reason: "   " }));
    expect(blank.code).toBe("functions/invalid-argument");

    const nextWeek = inThreeDays.plus({ days: 7 }).set({ hour: 21, minute: 0 }).toUTC().toISO();
    const otherWeek = await callError(() => owner.call("addScheduleException", {
      clubId: club.id, date, type: "RESCHEDULED", newScheduledAt: nextWeek, reason: "Campo ocupado"
    }));
    expect(otherWeek.code).toBe("functions/invalid-argument");

    await owner.call("addScheduleException", { clubId: club.id, date, type: "CANCELLED", reason: "Festivo" });
    const sameDayLater = inThreeDays.set({ hour: 21, minute: 30 }).toUTC().toISO();
    await owner.call("addScheduleException", { clubId: club.id, date, type: "RESCHEDULED", newScheduledAt: sameDayLater, reason: "Campo ocupado" });
    const exceptions = await player.call<Exception[]>("getScheduleExceptions", { clubId: club.id });
    expect(exceptions).toHaveLength(1);
    expect(exceptions[0]).toMatchObject({ date, type: "RESCHEDULED", reason: "Campo ocupado" });
  });

  it("AC-020-04/05 ratings are per season; the roll-over freezes the closed one and is idempotent", { timeout: 60_000 }, async () => {
    const extra = await owner.call<{ id: string }>("createExtraMatch", { clubId: club.id, scheduledAt: new Date(Date.now() + 5000).toISOString() });
    await owner.call("enroll", { announcementId: extra.id });
    await player.call("enroll", { announcementId: extra.id });
    const drawn = await owner.call<{ teamA: string[] }>("generateTeams", { matchId: extra.id, mode: "AUTO" });
    await owner.call("addMatchEvent", { matchId: extra.id, clubMemberId: drawn.teamA[0], type: "GOAL" });
    await sleep(6000);
    await owner.call("completeMatch", { matchId: extra.id });
    const seasonId = seasonIdOf(expected.startYear);
    const during = await player.call<Rating[]>("getRatings", { clubId: club.id });
    expect(during.find((r) => r.clubMemberId === drawn.teamA[0])!.rating).toBeGreaterThan(1000);

    const afterEnd = DateTime.fromISO(`${expected.startYear + 1}-06-16T12:00`, { zone: "Europe/Madrid" }).toUTC().toISO();
    await owner.call("runJobsNow", { at: afterEnd });
    await owner.call("runJobsNow", { at: afterEnd });
    const seasons = await player.call<Season[]>("getSeasons", { clubId: club.id });
    expect(seasons.map((s) => s.id)).toEqual([seasonIdOf(expected.startYear + 1), seasonId]);
    expect(seasons[1].status).toBe("CLOSED");
    expect(seasons[0]).toMatchObject({ status: "UPCOMING", startsOn: `${expected.startYear + 1}-09-01`, endsOn: `${expected.startYear + 2}-06-15` });

    const next = await player.call<Rating[]>("getRatings", { clubId: club.id });
    expect(next.every((r) => r.rating === 1000 && r.matchesRated === 0)).toBe(true);
    const frozen = await player.call<Rating[]>("getRatings", { clubId: club.id, seasonId });
    expect(frozen.find((r) => r.clubMemberId === drawn.teamA[0])!.rating).toBeGreaterThan(1000);
    const stats = await player.call<Array<{ clubMemberId: string; goals: number }>>("getStats", { clubId: club.id, seasonId });
    expect(stats.find((s) => s.clubMemberId === drawn.teamA[0])!.goals).toBe(1);

    const reopen = await callError(() => owner.call("reopenMatch", { matchId: extra.id }));
    expect(reopen.code).toBe("functions/failed-precondition");
    const closedEdit = await callError(() => owner.call("updateSeason", { clubId: club.id, seasonId, startsOn: expected.startsOn, endsOn: expected.endsOn }));
    expect(closedEdit.code).toBe("functions/failed-precondition");
  });
});
