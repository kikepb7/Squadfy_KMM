import { describe, expect, it } from "vitest";
import { allocate, Participant } from "../../../src/domain/enrollmentAllocator";

let id = 0;
const start = Date.parse("2026-10-05T10:00:00Z");
const member = (minute: number): Participant => ({ entryId: `m${id++}`, isGuest: false, enrolledAt: start + minute * 60_000 });
const guest = (minute: number): Participant => ({ entryId: `g${id++}`, isGuest: true, enrolledAt: start + minute * 60_000 });

describe("EnrollmentAllocator (ported from the backend, AC-019-09)", () => {
  it("guests only take places left by members, whatever their enrollment time", () => {
    const earlyGuest = guest(0);
    const lateGuest = guest(1);
    const members = [2, 3, 4].map(member);
    const allocation = allocate([lateGuest, earlyGuest, ...members], 4);
    members.forEach((m) => expect(allocation.get(m.entryId)).toBe("CONFIRMED"));
    expect(allocation.get(earlyGuest.entryId)).toBe("CONFIRMED");
    expect(allocation.get(lateGuest.entryId)).toBe("WAITLISTED");
  });

  it("members beyond the capacity wait ahead of every guest", () => {
    const members = [0, 1, 2].map(member);
    const g = guest(-10);
    const allocation = allocate([...members, g], 2);
    expect(members.map((m) => allocation.get(m.entryId))).toEqual(["CONFIRMED", "CONFIRMED", "WAITLISTED"]);
    expect(allocation.get(g.entryId)).toBe("WAITLISTED");
  });

  it("nobody is confirmed without places", () => {
    expect(allocate([], 10).size).toBe(0);
    expect([...allocate([member(0)], 0).values()]).toEqual(["WAITLISTED"]);
  });
});
