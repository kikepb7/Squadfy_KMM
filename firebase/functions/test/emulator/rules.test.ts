import { readFileSync } from "node:fs";
import { afterAll, beforeAll, describe, it } from "vitest";
import { assertFails, assertSucceeds, initializeTestEnvironment, RulesTestEnvironment } from "@firebase/rules-unit-testing";
import { deleteDoc, doc, getDoc, serverTimestamp, setDoc, updateDoc } from "firebase/firestore";
import { ref as storageRef, uploadBytes } from "firebase/storage";
import { ref as dbRef, set } from "firebase/database";

let env: RulesTestEnvironment;
const CLUB = `rulesclub${Date.now()}`;

beforeAll(async () => {
  env = await initializeTestEnvironment({
    projectId: "demo-squadfy",
    firestore: { rules: readFileSync("../firestore.rules", "utf8"), host: "127.0.0.1", port: 8086 },
    storage: { rules: readFileSync("../storage.rules", "utf8"), host: "127.0.0.1", port: 9198 },
    database: { rules: readFileSync("../database.rules.json", "utf8"), host: "127.0.0.1", port: 9001 }
  });
  await env.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await setDoc(doc(db, "users/alice"), { username: "Alice", usernameLower: "alice", pictureUrl: null, updatedAt: new Date() });
    await setDoc(doc(db, `clubs/${CLUB}`), { name: "Club" });
    await setDoc(doc(db, `clubs/${CLUB}/members/${CLUB}_alice`), { userId: "alice", status: "ACTIVE", role: "OWNER" });
    await setDoc(doc(db, `clubs/${CLUB}/members/${CLUB}_bob`), { userId: "bob", status: "ACTIVE", role: "PLAYER" });
    await setDoc(doc(db, `clubs/${CLUB}/members/${CLUB}_eve`), { userId: "eve", status: "BANNED", role: "PLAYER" });
    await setDoc(doc(db, `clubs/${CLUB}/matches/m1`), { status: "SCHEDULED" });
    await setDoc(doc(db, "chats/chat1"), { participantIds: ["alice", "bob"] });
    await setDoc(doc(db, "chats/chat1/messages/old"), { senderId: "bob", content: "hola", createdAt: new Date() });
  });
});
afterAll(async () => { await env?.cleanup(); });

describe("AC-019-21 Firestore rules", () => {
  it("only active members read a club and its subcollections; nobody writes them", async () => {
    const bob = env.authenticatedContext("bob").firestore();
    const eve = env.authenticatedContext("eve").firestore();
    const stranger = env.authenticatedContext("mallory").firestore();
    await assertSucceeds(getDoc(doc(bob, `clubs/${CLUB}`)));
    await assertSucceeds(getDoc(doc(bob, `clubs/${CLUB}/matches/m1`)));
    await assertFails(getDoc(doc(eve, `clubs/${CLUB}`)));
    await assertFails(getDoc(doc(stranger, `clubs/${CLUB}/matches/m1`)));
    await assertFails(setDoc(doc(bob, `clubs/${CLUB}/matches/m2`), { status: "SCHEDULED" }));
    await assertFails(updateDoc(doc(bob, `clubs/${CLUB}/members/${CLUB}_bob`), { role: "OWNER" }));
  });

  it("users only change their own picture; profiles are readable when signed in", async () => {
    const alice = env.authenticatedContext("alice").firestore();
    const bob = env.authenticatedContext("bob").firestore();
    await assertSucceeds(getDoc(doc(bob, "users/alice")));
    await assertFails(getDoc(doc(env.unauthenticatedContext().firestore(), "users/alice")));
    await assertSucceeds(updateDoc(doc(alice, "users/alice"), { pictureUrl: "https://x/a.jpg", updatedAt: serverTimestamp() }));
    await assertFails(updateDoc(doc(alice, "users/alice"), { username: "Root", updatedAt: serverTimestamp() }));
    await assertFails(updateDoc(doc(bob, "users/alice"), { pictureUrl: null, updatedAt: serverTimestamp() }));
  });

  it("devices are private and well formed", async () => {
    const alice = env.authenticatedContext("alice").firestore();
    await assertSucceeds(setDoc(doc(alice, "users/alice/devices/tok"), { platform: "android", locale: "es-ES", timeZone: "Europe/Madrid", appVersion: "1.0.0", updatedAt: serverTimestamp() }));
    await assertFails(setDoc(doc(alice, "users/alice/devices/tok2"), { platform: "windows", updatedAt: serverTimestamp() }));
    await assertFails(getDoc(doc(env.authenticatedContext("bob").firestore(), "users/alice/devices/tok")));
  });

  it("only participants read and write chat messages, as themselves, and only authors delete", async () => {
    const alice = env.authenticatedContext("alice").firestore();
    const mallory = env.authenticatedContext("mallory").firestore();
    await assertSucceeds(getDoc(doc(alice, "chats/chat1")));
    await assertFails(getDoc(doc(mallory, "chats/chat1")));
    await assertSucceeds(setDoc(doc(alice, "chats/chat1/messages/m1"), { senderId: "alice", content: "¿Jugamos?", createdAt: serverTimestamp() }));
    await assertFails(setDoc(doc(alice, "chats/chat1/messages/m2"), { senderId: "bob", content: "fake", createdAt: serverTimestamp() }));
    await assertFails(setDoc(doc(mallory, "chats/chat1/messages/m3"), { senderId: "mallory", content: "spam", createdAt: serverTimestamp() }));
    await assertFails(setDoc(doc(alice, "chats/chat1/messages/m4"), { senderId: "alice", content: "", createdAt: serverTimestamp() }));
    await assertFails(deleteDoc(doc(alice, "chats/chat1/messages/old")));
    await assertSucceeds(deleteDoc(doc(alice, "chats/chat1/messages/m1")));
  });

  it("invitation codes and usernames are never readable", async () => {
    const alice = env.authenticatedContext("alice").firestore();
    await assertFails(getDoc(doc(alice, "invitationCodes/ABCDEFGH")));
    await assertFails(getDoc(doc(alice, "usernames/alice")));
  });
});

describe("AC-019-17 Storage rules", () => {
  const image = new Uint8Array([0xff, 0xd8, 0xff, 0xe0]);
  it("only the owner writes their avatar, and only images", async () => {
    const alice = env.authenticatedContext("alice").storage();
    await assertSucceeds(uploadBytes(storageRef(alice, "users/alice/avatar.jpg"), image, { contentType: "image/jpeg" }));
    await assertFails(uploadBytes(storageRef(alice, "users/bob/avatar.jpg"), image, { contentType: "image/jpeg" }));
    await assertFails(uploadBytes(storageRef(alice, "users/alice/avatar.jpg"), image, { contentType: "application/pdf" }));
  });

  it("only managers change the club logo; members their own club picture", async () => {
    const alice = env.authenticatedContext("alice").storage();
    const bob = env.authenticatedContext("bob").storage();
    await assertSucceeds(uploadBytes(storageRef(alice, `clubs/${CLUB}/logo.jpg`), image, { contentType: "image/jpeg" }));
    await assertFails(uploadBytes(storageRef(bob, `clubs/${CLUB}/logo.jpg`), image, { contentType: "image/jpeg" }));
    await assertSucceeds(uploadBytes(storageRef(bob, `clubs/${CLUB}/members/bob.jpg`), image, { contentType: "image/png" }));
    await assertFails(uploadBytes(storageRef(bob, `clubs/${CLUB}/members/alice.jpg`), image, { contentType: "image/png" }));
  });
});

describe("AC-019-16 Realtime Database rules", () => {
  it("users only write their own presence and typing", async () => {
    const alice = env.authenticatedContext("alice").database();
    await assertSucceeds(set(dbRef(alice, "presence/alice"), { online: true, lastSeen: 1 }));
    await assertFails(set(dbRef(alice, "presence/bob"), { online: true, lastSeen: 1 }));
    await assertSucceeds(set(dbRef(alice, "typing/chat1/alice"), Date.now()));
    await assertFails(set(dbRef(alice, "typing/chat1/bob"), Date.now()));
  });
});
