import { Timestamp } from "firebase-admin/firestore";
import { onDocumentCreated, onDocumentDeleted, onDocumentUpdated } from "firebase-functions/v2/firestore";
import { onCall } from "firebase-functions/v2/https";
import { db } from "../admin";
import { badRequest, forbidden, notFound } from "../errors";
import { body, requireAuth, requireString } from "../guards";
import { sendToUsers } from "../notifications/push";
import { paths } from "../paths";

/**
 * Chat on Firestore (backend ChatService, spec 019 AC-019-15). Chats are created and their participants changed here;
 * messages are written by the app directly (the rules check sender and membership), and a trigger keeps the last
 * message and sends the push.
 */

export interface ChatParticipant {
  username: string;
  pictureUrl: string | null;
}

/** `chats/{chatId}` */
export interface ChatDoc {
  participantIds: string[];
  participants: Record<string, ChatParticipant>;
  lastMessage: { id: string; senderId: string; content: string; createdAt: Timestamp } | null;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

async function profiles(uids: string[]): Promise<Record<string, ChatParticipant>> {
  const docs = await db().getAll(...uids.map((uid) => db().doc(paths.user(uid))));
  const result: Record<string, ChatParticipant> = {};
  docs.forEach((doc, i) => {
    if (!doc.exists) notFound("User not found", "USER_NOT_FOUND");
    result[uids[i]] = { username: (doc.get("username") as string) ?? "", pictureUrl: (doc.get("pictureUrl") as string | null) ?? null };
  });
  return result;
}

function userIds(value: unknown): string[] {
  if (!Array.isArray(value) || value.some((v) => typeof v !== "string")) badRequest("Invalid user ids", "VALIDATION_ERROR");
  return [...new Set(value as string[])];
}

/** A chat needs at least two people (400 INVALID_CHAT_SIZE). */
export const createChat = onCall(async (request) => {
  const uid = requireAuth(request);
  const participantIds = [...new Set([uid, ...userIds(body(request).otherUserIds)])];
  if (participantIds.length < 2) badRequest("A chat needs at least 2 participants", "INVALID_CHAT_SIZE");
  const now = Timestamp.now();
  const chat: ChatDoc = { participantIds, participants: await profiles(participantIds), lastMessage: null, createdAt: now, updatedAt: now };
  const ref = await db().collection(paths.chats()).add(chat);
  return { id: ref.id };
});

/** Only participants can add people. */
export const addChatParticipants = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const chatId = requireString(data, "chatId");
  const newIds = userIds(data.userIds);
  const added = await profiles(newIds);
  await db().runTransaction(async (tx) => {
    const ref = db().doc(paths.chat(chatId));
    const chat = (await tx.get(ref)).data() as ChatDoc | undefined;
    if (!chat) notFound("Chat not found");
    if (!chat!.participantIds.includes(uid)) forbidden("You are not in this chat");
    tx.update(ref, {
      participantIds: [...new Set([...chat!.participantIds, ...newIds])],
      participants: { ...chat!.participants, ...added },
      updatedAt: Timestamp.now()
    });
  });
  return { id: chatId };
});

/** The last one to leave deletes the chat and its messages. */
export const leaveChat = onCall(async (request) => {
  const uid = requireAuth(request);
  const chatId = requireString(body(request), "chatId");
  const ref = db().doc(paths.chat(chatId));
  const empty = await db().runTransaction(async (tx) => {
    const chat = (await tx.get(ref)).data() as ChatDoc | undefined;
    if (!chat) notFound("Chat not found");
    if (!chat!.participantIds.includes(uid)) notFound("You are not in this chat");
    const remaining = chat!.participantIds.filter((id) => id !== uid);
    if (remaining.length === 0) {
      tx.delete(ref);
      return true;
    }
    const participants = { ...chat!.participants };
    delete participants[uid];
    tx.update(ref, { participantIds: remaining, participants });
    return false;
  });
  if (empty) await db().recursiveDelete(db().collection(paths.messages(chatId)));
  return { ok: true };
});

/** Keeps the chat's last message and activity, and notifies the other participants (backend chat push). */
export const onChatMessageCreated = onDocumentCreated("chats/{chatId}/messages/{messageId}", async (event) => {
  const message = event.data?.data();
  if (!message) return;
  const chatId = event.params.chatId;
  const ref = db().doc(paths.chat(chatId));
  const chat = (await ref.get()).data() as ChatDoc | undefined;
  if (!chat) return;
  const createdAt = (message.createdAt as Timestamp | undefined) ?? Timestamp.now();
  await ref.update({
    lastMessage: { id: event.params.messageId, senderId: message.senderId, content: message.content, createdAt },
    updatedAt: createdAt
  });
  const recipients = (chat.participantIds ?? []).filter((id) => id !== message.senderId);
  const text = { title: chat.participants?.[message.senderId]?.username ?? "Squadfy", body: String(message.content).slice(0, 240) };
  await sendToUsers({ userIds: recipients, text: { es: text, en: text }, data: { type: "new_message", chatId }, collapseKey: chatId, muteClubId: null });
});

/** A deleted last message is replaced by the previous one. */
export const onChatMessageDeleted = onDocumentDeleted("chats/{chatId}/messages/{messageId}", async (event) => {
  const ref = db().doc(paths.chat(event.params.chatId));
  const chat = (await ref.get()).data() as ChatDoc | undefined;
  if (!chat || chat.lastMessage?.id !== event.params.messageId) return;
  const previous = await db().collection(paths.messages(event.params.chatId)).orderBy("createdAt", "desc").limit(1).get();
  const last = previous.docs[0];
  await ref.update({
    lastMessage: last ? { id: last.id, senderId: last.get("senderId"), content: last.get("content"), createdAt: last.get("createdAt") } : null
  });
});

/**
 * A new username or picture reaches every copy of the public profile: club memberships and chat participants
 * (`completeProfile` and the app write `users/{uid}`).
 */
export const onUserProfileUpdated = onDocumentUpdated("users/{uid}", async (event) => {
  const before = event.data?.before.data();
  const after = event.data?.after.data();
  if (!before || !after) return;
  if (before.username === after.username && before.pictureUrl === after.pictureUrl) return;
  const uid = event.params.uid;
  const username = (after.username as string) ?? "";
  const pictureUrl = (after.pictureUrl as string | null) ?? null;
  const [members, chats] = await Promise.all([
    db().collectionGroup("members").where("userId", "==", uid).get(),
    db().collection(paths.chats()).where("participantIds", "array-contains", uid).get()
  ]);
  const writer = db().bulkWriter();
  members.docs.forEach((d) => writer.update(d.ref, { username, profilePictureUrl: pictureUrl }));
  chats.docs.forEach((d) => writer.update(d.ref, { [`participants.${uid}`]: { username, pictureUrl } }));
  await writer.close();
});
