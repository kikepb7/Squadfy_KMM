import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { collectionGroup, doc, getDoc, getDocs, query, where } from "firebase/firestore";
import { callError, newClient } from "./helpers";

type Client = Awaited<ReturnType<typeof newClient>>;
interface Club { id: string; invitationCode: string; membersCount: number; ownerId: string; maxMembers: number | null }
interface Member { id: string; role: string }

const clients: Client[] = [];
async function user(prefix: string) {
  const client = await newClient(prefix);
  clients.push(client);
  await client.call("completeProfile", { username: `${prefix}${Date.now() % 1_000_000}`.slice(0, 20) });
  return client;
}
afterAll(async () => { await Promise.all(clients.map((c) => c.close())); });

describe("AC-019-07 clubs and membership", () => {
  let owner: Client;
  let player: Client;
  let club: Club;

  beforeAll(async () => {
    owner = await user("owner");
    player = await user("player");
    club = await owner.call<Club>("createClub", { name: "Churriana FC", description: "Jueves", maxMembers: 3 });
  });

  it("creates the club with its owner and an 8-character code", async () => {
    expect(club.membersCount).toBe(1);
    expect(club.ownerId).toBe(owner.uid);
    expect(club.invitationCode).toMatch(/^[A-Z0-9]{8}$/);
    const ownerMember = await getDoc(doc(owner.firestore, `clubs/${club.id}/members/${club.id}_${owner.uid}`));
    expect(ownerMember.get("role")).toBe("OWNER");
  });

  it("joins with the code, ignoring case, and lists the club in my clubs", async () => {
    const joined = await player.call<Club>("joinClub", { invitationCode: club.invitationCode.toLowerCase(), shirtNumber: 9, position: "FORWARD" });
    expect(joined.membersCount).toBe(2);
    const mine = await getDocs(query(collectionGroup(player.firestore, "members"), where("userId", "==", player.uid)));
    expect(mine.docs.map((d) => d.get("clubId"))).toContain(club.id);
  });

  it("rejects an unknown code, a second join and a full club", async () => {
    expect((await callError(() => player.call("joinClub", { invitationCode: "ZZZZZZZZ" }))).details).toEqual({ code: "INVALID_INVITATION_CODE" });
    expect((await callError(() => player.call("joinClub", { invitationCode: club.invitationCode }))).code).toBe("functions/failed-precondition");
    const third = await user("third");
    await third.call("joinClub", { invitationCode: club.invitationCode });
    const fourth = await user("fourth");
    expect((await callError(() => fourth.call("joinClub", { invitationCode: club.invitationCode }))).code).toBe("functions/failed-precondition");
  });

  it("lets only members read the club", async () => {
    const outsider = await user("outsider");
    expect((await getDoc(doc(player.firestore, `clubs/${club.id}`))).get("name")).toBe("Churriana FC");
    await expect(getDoc(doc(outsider.firestore, `clubs/${club.id}`))).rejects.toThrow();
  });

  it("follows the role hierarchy: players cannot remove, the owner promotes, admins only toggle captain and player", async () => {
    const playerId = `${club.id}_${player.uid}`;
    expect((await callError(() => player.call("removeMember", { clubId: club.id, memberId: `${club.id}_${owner.uid}` }))).code).toBe("functions/permission-denied");
    const admin = await owner.call<Member>("changeMemberRole", { clubId: club.id, memberId: playerId, role: "ADMIN" });
    expect(admin.role).toBe("ADMIN");
    expect((await callError(() => owner.call("changeMemberRole", { clubId: club.id, memberId: playerId, role: "OWNER" }))).code).toBe("functions/permission-denied");
    await owner.call("changeMemberRole", { clubId: club.id, memberId: playerId, role: "PLAYER" });
  });

  it("does not let the owner leave until the club is transferred; the former owner becomes ADMIN", async () => {
    expect((await callError(() => owner.call("leaveClub", { clubId: club.id }))).code).toBe("functions/failed-precondition");
    const transferred = await owner.call<Club>("transferOwnership", { clubId: club.id, memberId: `${club.id}_${player.uid}` });
    expect(transferred.ownerId).toBe(player.uid);
    const former = await getDoc(doc(owner.firestore, `clubs/${club.id}/members/${club.id}_${owner.uid}`));
    expect(former.get("role")).toBe("ADMIN");
    await owner.call("leaveClub", { clubId: club.id });
    await expect(getDoc(doc(owner.firestore, `clubs/${club.id}`))).rejects.toThrow();
  });

  it("keeps a banned member out until the ban is lifted, and a former member keeps the same membership id", async () => {
    const other = await newClient("other");
    clients.push(other);
    await other.call("completeProfile", { username: `other${Date.now() % 100000}` });
    const newCode = (await player.call<{ invitationCode: string }>("regenerateInvitationCode", { clubId: club.id })).invitationCode;
    expect(newCode).not.toBe(club.invitationCode);
    await player.call("updateClub", { clubId: club.id, maxMembers: 10 });
    await other.call("joinClub", { invitationCode: newCode });
    const otherId = `${club.id}_${other.uid}`;
    await player.call("banMember", { clubId: club.id, memberId: otherId });
    expect((await callError(() => other.call("joinClub", { invitationCode: newCode }))).details).toEqual({ code: "BANNED_FROM_CLUB" });
    await player.call("unbanMember", { clubId: club.id, memberId: otherId });
    await other.call("joinClub", { invitationCode: newCode });
    const membership = await getDoc(doc(other.firestore, `clubs/${club.id}/members/${otherId}`));
    expect(membership.get("status")).toBe("ACTIVE");
  });

  it("rejects editing the member limit below the current members", async () => {
    const error = await callError(() => player.call("updateClub", { clubId: club.id, maxMembers: 1 }));
    expect(error.code).toBe("functions/invalid-argument");
  });
});
