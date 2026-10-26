import { afterAll, describe, expect, it } from "vitest";
import { doc, getDoc, serverTimestamp, setDoc } from "firebase/firestore";
import { callError, newClient } from "./helpers";

type Client = Awaited<ReturnType<typeof newClient>>;
const clients: Client[] = [];
const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));
async function user(prefix: string) {
  const c = await newClient(prefix);
  clients.push(c);
  await c.call("completeProfile", { username: `${prefix}${Date.now() % 1_000_000}`.slice(0, 20) });
  return c;
}
afterAll(async () => { await Promise.all(clients.map((c) => c.close())); });

describe("AC-019-15 chat on Firestore", () => {
  it("creates a chat, keeps its last message and lets the last participant delete it", async () => {
    const ana = await user("ana");
    const ben = await user("ben");
    expect((await callError(() => ana.call("createChat", { otherUserIds: [] }))).details).toEqual({ code: "INVALID_CHAT_SIZE" });
    const { id } = await ana.call<{ id: string }>("createChat", { otherUserIds: [ben.uid] });
    await setDoc(doc(ana.firestore, `chats/${id}/messages/msg1`), { senderId: ana.uid, content: "¿Jugamos el jueves?", createdAt: serverTimestamp() });
    let last: unknown = null;
    for (let i = 0; i < 20 && !last; i++) {
      await sleep(300);
      last = (await getDoc(doc(ben.firestore, `chats/${id}`))).get("lastMessage");
    }
    expect(last).toMatchObject({ id: "msg1", senderId: ana.uid, content: "¿Jugamos el jueves?" });
    await ana.call("leaveChat", { chatId: id });
    await expect(getDoc(doc(ana.firestore, `chats/${id}`))).rejects.toThrow();
    await ben.call("leaveChat", { chatId: id });
    expect((await callError(() => ben.call("leaveChat", { chatId: id }))).code).toBe("functions/not-found");
  });

  it("a new picture reaches the chats of the user", async () => {
    const carla = await user("carla");
    const dani = await user("dani");
    const { id } = await carla.call<{ id: string }>("createChat", { otherUserIds: [dani.uid] });
    await setDoc(doc(carla.firestore, `users/${carla.uid}`), { pictureUrl: "https://x/c.jpg", updatedAt: serverTimestamp() }, { merge: true });
    let picture: unknown = null;
    for (let i = 0; i < 20 && !picture; i++) {
      await sleep(300);
      picture = (await getDoc(doc(dani.firestore, `chats/${id}`))).get(`participants.${carla.uid}.pictureUrl`);
    }
    expect(picture).toBe("https://x/c.jpg");
  });
});
