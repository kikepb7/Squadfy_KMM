import { afterAll, describe, expect, it } from "vitest";
import { deleteUser } from "firebase/auth";
import { doc, getDoc, serverTimestamp, setDoc } from "firebase/firestore";
import { callError, newClient } from "./helpers";

type Client = Awaited<ReturnType<typeof newClient>>;
const clients: Client[] = [];
async function user(prefix: string) {
  const c = await newClient(prefix);
  clients.push(c);
  await c.call("completeProfile", { username: `${prefix}${Date.now() % 1_000_000}`.slice(0, 20) });
  return c;
}
afterAll(async () => { await Promise.all(clients.map((c) => c.close().catch(() => undefined))); });

describe("AC-019-06 account deletion with the spec 010 rules", () => {
  it("hands the club to the oldest admin, anonymizes the membership and removes chats, messages and the account", async () => {
    const owner = await user("del");
    const player = await user("ply");
    const admin = await user("adm");
    const club = await owner.call<{ id: string; invitationCode: string }>("createClub", { name: "Del FC" });
    await player.call("joinClub", { invitationCode: club.invitationCode });
    await admin.call("joinClub", { invitationCode: club.invitationCode });
    await owner.call("changeMemberRole", { clubId: club.id, memberId: `${club.id}_${admin.uid}`, role: "ADMIN" });
    const { id: chatId } = await owner.call<{ id: string }>("createChat", { otherUserIds: [player.uid] });
    await setDoc(doc(owner.firestore, `chats/${chatId}/messages/bye`), { senderId: owner.uid, content: "Adiós", createdAt: serverTimestamp() });

    await owner.call("deleteAccount");

    const clubDoc = await getDoc(doc(admin.firestore, `clubs/${club.id}`));
    expect(clubDoc.get("ownerId")).toBe(admin.uid);
    expect((await getDoc(doc(admin.firestore, `clubs/${club.id}/members/${club.id}_${admin.uid}`))).get("role")).toBe("OWNER");
    const former = await getDoc(doc(admin.firestore, `clubs/${club.id}/members/${club.id}_${owner.uid}`));
    expect(former.get("status")).toBe("LEFT");
    expect(former.get("username")).toMatch(/^Usuario eliminado /);
    expect((await getDoc(doc(player.firestore, `chats/${chatId}/messages/bye`))).exists()).toBe(false);
    expect((await getDoc(doc(player.firestore, `chats/${chatId}`))).get("participantIds")).toEqual([player.uid]);
    expect((await getDoc(doc(player.firestore, `users/${owner.uid}`))).exists()).toBe(false);
    // The account no longer exists: its token is refused
    expect((await callError(() => owner.call("createClub", { name: "Ghost" }))).code).toBe("functions/unauthenticated");
  });

  it("deletes a club whose owner was the last member", async () => {
    const lonely = await user("lone");
    const club = await lonely.call<{ id: string }>("createClub", { name: "Solo FC" });
    await lonely.call("deleteAccount");
    const watcher = await user("watch");
    await expect(getDoc(doc(watcher.firestore, `clubs/${club.id}`))).rejects.toThrow();
  });

  it("deleting the Auth user outside the app (web page, console) erases the same data", async () => {
    const leaving = await user("web");
    const keeper = await user("keep");
    const club = await leaving.call<{ id: string; invitationCode: string }>("createClub", { name: "Web FC" });
    await keeper.call("joinClub", { invitationCode: club.invitationCode });

    await deleteUser(leaving.auth.currentUser!);

    // The Auth trigger runs asynchronously
    const memberRef = doc(keeper.firestore, `clubs/${club.id}/members/${club.id}_${leaving.uid}`);
    for (let i = 0; i < 40 && (await getDoc(memberRef)).get("status") !== "LEFT"; i++) await new Promise((r) => setTimeout(r, 250));
    expect((await getDoc(memberRef)).get("username")).toMatch(/^Usuario eliminado /);
    expect((await getDoc(doc(keeper.firestore, `clubs/${club.id}`))).get("ownerId")).toBe(keeper.uid);
    // The profile is deleted at the end of the same trigger: wait for it too
    const profileRef = doc(keeper.firestore, `users/${leaving.uid}`);
    for (let i = 0; i < 40 && (await getDoc(profileRef)).exists(); i++) await new Promise((r) => setTimeout(r, 250));
    expect((await getDoc(profileRef)).exists()).toBe(false);
  });
});
