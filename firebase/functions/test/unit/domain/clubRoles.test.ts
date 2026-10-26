import { describe, expect, it } from "vitest";
import { canAssign, canRemove, successor } from "../../../src/domain/clubRoles";
import { ClubMemberRole } from "../../../src/domain/types";

describe("ClubRolePolicy (ported from the backend, AC-019-07)", () => {
  it("the owner can remove anyone except an owner", () => {
    (["ADMIN", "CAPTAIN", "PLAYER"] as ClubMemberRole[]).forEach((t) => expect(canRemove("OWNER", t)).toBe(true));
    expect(canRemove("OWNER", "OWNER")).toBe(false);
  });

  it("an admin can only remove captains and players", () => {
    expect(canRemove("ADMIN", "PLAYER")).toBe(true);
    expect(canRemove("ADMIN", "CAPTAIN")).toBe(true);
    expect(canRemove("ADMIN", "ADMIN")).toBe(false);
    expect(canRemove("ADMIN", "OWNER")).toBe(false);
  });

  it("captains and players cannot remove anyone", () => {
    (["CAPTAIN", "PLAYER"] as ClubMemberRole[]).forEach((a) =>
      (["OWNER", "ADMIN", "CAPTAIN", "PLAYER"] as ClubMemberRole[]).forEach((t) => expect(canRemove(a, t)).toBe(false)));
  });

  it("the owner grants and revokes admin but never assigns owner", () => {
    expect(canAssign("OWNER", "PLAYER", "ADMIN")).toBe(true);
    expect(canAssign("OWNER", "ADMIN", "PLAYER")).toBe(true);
    expect(canAssign("OWNER", "PLAYER", "OWNER")).toBe(false);
  });

  it("an admin only toggles between captain and player", () => {
    expect(canAssign("ADMIN", "PLAYER", "CAPTAIN")).toBe(true);
    expect(canAssign("ADMIN", "CAPTAIN", "PLAYER")).toBe(true);
    expect(canAssign("ADMIN", "PLAYER", "ADMIN")).toBe(false);
    expect(canAssign("ADMIN", "ADMIN", "PLAYER")).toBe(false);
    expect(canAssign("ADMIN", "OWNER", "ADMIN")).toBe(false);
  });

  it("players and captains cannot change roles", () => {
    expect(canAssign("CAPTAIN", "PLAYER", "CAPTAIN")).toBe(false);
    expect(canAssign("PLAYER", "PLAYER", "CAPTAIN")).toBe(false);
  });
});

describe("OwnerSuccession (ported from the backend, AC-019-06)", () => {
  const day = 86_400_000;
  it("the oldest admin inherits even if a player joined earlier", () => {
    expect(successor("owner", [
      { memberId: "owner", role: "OWNER", joinedAt: 0 },
      { memberId: "oldPlayer", role: "PLAYER", joinedAt: day },
      { memberId: "newAdmin", role: "ADMIN", joinedAt: 5 * day },
      { memberId: "oldAdmin", role: "ADMIN", joinedAt: 3 * day }
    ])).toBe("oldAdmin");
  });

  it("without admins the oldest member inherits", () => {
    expect(successor("owner", [
      { memberId: "captain", role: "CAPTAIN", joinedAt: 2 * day },
      { memberId: "owner", role: "OWNER", joinedAt: 0 },
      { memberId: "player", role: "PLAYER", joinedAt: day }
    ])).toBe("player");
  });

  it("nobody inherits a club where the owner is alone", () => {
    expect(successor("owner", [{ memberId: "owner", role: "OWNER", joinedAt: 0 }])).toBeNull();
    expect(successor("owner", [])).toBeNull();
  });
});
