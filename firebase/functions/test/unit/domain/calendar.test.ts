import { describe, expect, it } from "vitest";
import { DateTime } from "luxon";
import { announcementWindow, nextMatchDate } from "../../../src/domain/matchCalendar";
import { DEFAULT_DEADLINE, deadlineInstant, validateDeadlines } from "../../../src/domain/deadlineRule";

const madrid = "Europe/Madrid";
const at = (local: string) => DateTime.fromISO(local, { zone: madrid }).toJSDate();

describe("MatchCalendar (ported from the backend, AC-019-08)", () => {
  it("next match date is strictly after today", () => {
    expect(nextMatchDate("2026-10-07", 4)).toBe("2026-10-08");
    expect(nextMatchDate("2026-10-07", 3)).toBe("2026-10-14");
    expect(nextMatchDate("2026-10-07", 1)).toBe("2026-10-12");
  });

  it("opens the day after the previous match and closes at 22h the day before", () => {
    const w = announcementWindow({ matchAt: at("2026-10-14T20:00"), zone: madrid, previousMatchAt: at("2026-10-07T20:00"), now: at("2026-10-07T22:00") });
    expect(w.opensAt).toEqual(at("2026-10-08T00:00"));
    expect(w.closesAt).toEqual(at("2026-10-13T22:00"));
  });

  it("the first announcement of a club opens immediately", () => {
    const now = at("2026-10-05T10:00");
    const w = announcementWindow({ matchAt: at("2026-10-08T20:00"), zone: madrid, previousMatchAt: null, now });
    expect(w.opensAt).toEqual(now);
    expect(w.closesAt).toEqual(at("2026-10-07T22:00"));
  });

  it("a match created after the cut-off accepts enrollments until kickoff", () => {
    const now = at("2026-10-07T22:30");
    const kickoff = at("2026-10-08T20:00");
    const w = announcementWindow({ matchAt: kickoff, zone: madrid, previousMatchAt: null, now });
    expect(w.opensAt).toEqual(now);
    expect(w.closesAt).toEqual(kickoff);
  });

  it("is computed in the club time zone across daylight saving changes", () => {
    const w = announcementWindow({ matchAt: at("2026-10-28T20:00"), zone: madrid, previousMatchAt: at("2026-10-21T20:00"), now: at("2026-10-21T22:00") });
    expect(w.opensAt).toEqual(new Date("2026-10-21T22:00:00Z"));
    expect(w.closesAt).toEqual(new Date("2026-10-27T21:00:00Z"));
  });

  it("close and draw follow the club configuration and the draw is never before the close", () => {
    const matchAt = at("2026-10-08T20:00");
    const w = announcementWindow({
      matchAt, zone: madrid, previousMatchAt: null, now: at("2026-10-05T10:00"),
      close: { daysBefore: 1, time: "21:00" }, draw: { daysBefore: 0, time: "12:00" }
    });
    expect(w.closesAt).toEqual(at("2026-10-07T21:00"));
    expect(w.drawAt).toEqual(at("2026-10-08T12:00"));
    const late = announcementWindow({ matchAt, zone: madrid, previousMatchAt: null, now: at("2026-10-07T23:00") });
    expect(late.closesAt).toEqual(matchAt);
    expect(late.drawAt).toEqual(matchAt);
  });
});

describe("DeadlineRule (ported from the backend)", () => {
  it("is the local time some days before the match", () => {
    expect(deadlineInstant({ daysBefore: 1, time: "21:00" }, "2026-10-08", madrid)).toEqual(at("2026-10-07T21:00"));
  });

  it("accepts the default and same-day configurations", () => {
    expect(validateDeadlines("20:00", DEFAULT_DEADLINE, DEFAULT_DEADLINE)).toBeNull();
    expect(validateDeadlines("20:00", { daysBefore: 1, time: "21:00" }, { daysBefore: 0, time: "12:00" })).toBeNull();
  });

  it("rejects closing after kickoff, drawing before closing or after kickoff, and negative days", () => {
    expect(validateDeadlines("20:00", { daysBefore: 0, time: "20:00" }, { daysBefore: 0, time: "20:00" })).not.toBeNull();
    expect(validateDeadlines("20:00", { daysBefore: 1, time: "22:00" }, { daysBefore: 2, time: "12:00" })).not.toBeNull();
    expect(validateDeadlines("20:00", { daysBefore: 1, time: "22:00" }, { daysBefore: 0, time: "21:00" })).not.toBeNull();
    expect(validateDeadlines("20:00", { daysBefore: -1, time: "22:00" }, DEFAULT_DEADLINE)).not.toBeNull();
  });
});
