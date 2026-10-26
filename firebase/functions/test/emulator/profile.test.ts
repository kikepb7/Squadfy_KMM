import { afterAll, describe, expect, it } from "vitest";
import { doc, getDoc } from "firebase/firestore";
import { callError, newClient } from "./helpers";

describe("AC-019-04 completeProfile reserves a unique username", () => {
  const clients: Array<{ close: () => Promise<void> }> = [];
  afterAll(async () => { await Promise.all(clients.map((c) => c.close())); });

  it("creates users/{uid} without the email", async () => {
    const alice = await newClient("alice");
    clients.push(alice);
    const name = `Alice${Date.now() % 100000}`;
    await alice.call("completeProfile", { username: name });
    const profile = await getDoc(doc(alice.firestore, `users/${alice.uid}`));
    expect(profile.get("username")).toBe(name);
    expect(profile.get("usernameLower")).toBe(name.toLowerCase());
    expect(profile.get("email")).toBeUndefined();
  });

  it("rejects a username taken by someone else, ignoring case, with USER_EXISTS", async () => {
    const first = await newClient("first");
    const second = await newClient("second");
    clients.push(first, second);
    const name = `Taken${Date.now() % 100000}`;
    await first.call("completeProfile", { username: name });
    const error = await callError(() => second.call("completeProfile", { username: name.toUpperCase() }));
    expect(error.code).toBe("functions/failed-precondition");
    expect(error.details).toEqual({ code: "USER_EXISTS" });
  });

  it("validates the length like the own backend (3-20)", async () => {
    const bob = await newClient("bob");
    clients.push(bob);
    const error = await callError(() => bob.call("completeProfile", { username: "ab" }));
    expect(error.code).toBe("functions/invalid-argument");
    expect(error.details).toEqual({ code: "VALIDATION_ERROR" });
  });
});
