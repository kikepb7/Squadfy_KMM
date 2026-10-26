#!/usr/bin/env node
/**
 * Demo data in the Firebase Emulator Suite (spec 019), so every screen can be tried with real data: a 7-a-side squad,
 * three played matches (results, goals, cards, minutes, Elo), the next match with its teams drawn and a waitlist,
 * absences and the club mute. Everything goes through the same callables as the app; only dates are moved with the
 * Admin SDK, because a callable cannot create a past match.
 *
 * Only for the emulators (`npm run serve`); it refuses to run otherwise.
 *
 *   npm run seed                                   # demo owner demo.owner@squadfy.test
 *   OWNER_EMAIL=… OWNER_PASSWORD=… npm run seed     # or your own account in the emulator (register it first)
 *
 * Every demo account (`demo.owner@squadfy.test`, `demo.<name>@squadfy.test`) uses DEMO_PASSWORD, by default the
 * emulator-only test password below. These accounts only exist in the local emulator.
 */
import { initializeApp as initAdmin } from "firebase-admin/app";
import { getFirestore as adminFirestore, Timestamp } from "firebase-admin/firestore";
import { deleteApp, initializeApp } from "firebase/app";
import { connectAuthEmulator, createUserWithEmailAndPassword, getAuth, signInWithEmailAndPassword } from "firebase/auth";
import { connectFunctionsEmulator, getFunctions, httpsCallable } from "firebase/functions";

const PROJECT_ID = process.env.GCLOUD_PROJECT ?? "squadfy-5233d";
const HOST = "127.0.0.1";
process.env.FIRESTORE_EMULATOR_HOST ??= `${HOST}:8086`;
process.env.FIREBASE_AUTH_EMULATOR_HOST ??= `${HOST}:9098`;

/** Emulator-only test password of the demo accounts. */
const demoPassword = process.env.DEMO_PASSWORD ?? "Squadfy2026";
try {
  await fetch(`http://${process.env.FIREBASE_AUTH_EMULATOR_HOST}/`);
} catch {
  console.error("The Auth emulator is not running: start it with `npm run serve` first. This script never touches a real project.");
  process.exit(1);
}

initAdmin({ projectId: PROJECT_ID });
const admin = adminFirestore();

const SQUAD = [
  ["Marcos Peña", 1, "GOALKEEPER"], ["Iván Soto", 4, "DEFENDER"], ["Sergio Mora", 5, "DEFENDER"],
  ["Hugo Prieto", 3, "DEFENDER"], ["Dani Moreno", 8, "MIDFIELDER"], ["Álex Ruiz", 9, "FORWARD"],
  ["Nico Vidal", 13, "GOALKEEPER"], ["Jaime Ortiz", 2, "DEFENDER"], ["Tomás Gallego", 6, "DEFENDER"],
  ["Gabi Sanz", 15, "DEFENDER"], ["Pablo Ruiz", 16, "MIDFIELDER"], ["Nacho León", 14, "MIDFIELDER"],
  ["Leo Vega", 11, "FORWARD"]
];
const WAITLIST = [["Rubén Castro", 17, "MIDFIELDER"], ["Kike Lara", 19, "FORWARD"]];
/** Played matches: [days ago, goals A, goals B]. */
const HISTORY = [[23, 3, 3], [16, 2, 4], [9, 4, 3]];

let appCounter = 0;
async function client(email, password, username) {
  const app = initializeApp({ projectId: PROJECT_ID, apiKey: "demo-key" }, `seed-${appCounter++}`);
  const auth = getAuth(app);
  connectAuthEmulator(auth, `http://${process.env.FIREBASE_AUTH_EMULATOR_HOST}`, { disableWarnings: true });
  const functions = getFunctions(app, "europe-west1");
  connectFunctionsEmulator(functions, HOST, 5003);
  let created = false;
  try {
    await signInWithEmailAndPassword(auth, email, password);
  } catch {
    await createUserWithEmailAndPassword(auth, email, password);
    created = true;
  }
  const call = async (name, data = {}) => (await httpsCallable(functions, name)(data)).data;
  if (created && username) await call("completeProfile", { username });
  return { app, uid: auth.currentUser.uid, call };
}

const slug = (name) => name.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase().replace(/\s+/g, ".");
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const DAYS = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];

async function main() {
  const owner = process.env.OWNER_EMAIL
    ? await client(process.env.OWNER_EMAIL, process.env.OWNER_PASSWORD ?? "")
    : await client("demo.owner@squadfy.test", demoPassword, "Álvaro Gil");
  const club = await owner.call("createClub", { name: "Churriana FC", description: "Fútbol 7 los jueves en el Cortijo", maxMembers: 20 });
  console.log(`club ${club.name} · code ${club.invitationCode}`);
  const memberOf = (uid) => `${club.id}_${uid}`;

  const players = {};
  for (const [name, number, position] of [...SQUAD, ...WAITLIST]) {
    const c = await client(`demo.${slug(name)}@squadfy.test`, demoPassword, name);
    await c.call("joinClub", { invitationCode: club.invitationCode, shirtNumber: number, position });
    players[name] = c;
  }
  console.log(`joined ${Object.keys(players).length} players`);
  const squad = [owner, ...SQUAD.map(([name]) => players[name])];

  // 1) Played matches: created a few seconds ahead, filled, moved to their past date and completed
  for (const [index, [daysAgo, goalsA, goalsB]] of HISTORY.entries()) {
    const kickoff = new Date(Date.now() + 5 * 60_000 + index * 60_000);
    const match = await owner.call("createExtraMatch", { clubId: club.id, scheduledAt: kickoff.toISOString(), format: "SEVEN_A_SIDE", durationMinutes: 60 });
    for (const c of squad) await c.call("enroll", { announcementId: match.id });
    const drawn = await owner.call("generateTeams", { matchId: match.id, mode: "AUTO" });
    const played = new Date(Date.now() - daysAgo * 86400_000);
    played.setUTCHours(18, 0, 0, 0);
    const matchRef = admin.doc(`clubs/${club.id}/matches/${match.id}`);
    await matchRef.update({ scheduledAt: Timestamp.fromDate(played) });
    await admin.doc(`clubs/${club.id}/announcements/${match.id}`).update({
      status: "CLOSED", opensAt: Timestamp.fromDate(new Date(played.getTime() - 6 * 86400_000)),
      closesAt: Timestamp.fromDate(new Date(played.getTime() - 20 * 3600_000)),
      drawAt: Timestamp.fromDate(new Date(played.getTime() - 19 * 3600_000)),
      teamsPublishedAt: Timestamp.fromDate(new Date(played.getTime() - 19 * 3600_000)),
      openedNotifiedAt: Timestamp.fromDate(played), closingReminderSentAt: Timestamp.fromDate(played)
    });
    let minute = 6;
    for (const [team, goals] of [[drawn.teamA, goalsA], [drawn.teamB, goalsB]]) {
      const outfield = team.slice(1).length ? team.slice(1) : team;
      for (let g = 0; g < goals; g++) {
        const scorer = outfield[(g * 2 + index) % outfield.length];
        const assist = outfield[(g * 2 + index + 1) % outfield.length];
        await owner.call("addMatchEvent", { matchId: match.id, clubMemberId: scorer, type: "GOAL", minute });
        if (assist !== scorer) await owner.call("addMatchEvent", { matchId: match.id, clubMemberId: assist, type: "ASSIST", minute });
        minute += 7;
      }
    }
    if (drawn.teamB.length) await owner.call("addMatchEvent", { matchId: match.id, clubMemberId: drawn.teamB.at(-1), type: "YELLOW_CARD", minute: 38 });
    if (drawn.teamA.length > 2) await owner.call("setPlayerMinutes", { matchId: match.id, clubMemberId: drawn.teamA[2], minutes: 45 });
    // The match id carries its original time; the stored date is what the app shows
    await owner.call("completeMatch", { matchId: match.id });
    console.log(`played ${played.toISOString().slice(0, 10)} ${goalsA}-${goalsB}`);
  }

  // 2) The weekly schedule plans the next match in three days
  const day = DAYS[new Date(Date.now() + 3 * 86400_000).getDay()];
  await owner.call("createSchedule", { clubId: club.id, matchDayOfWeek: day, matchTime: "20:00", timeZone: "Europe/Madrid", format: "SEVEN_A_SIDE" });
  const current = await owner.call("getCurrentAnnouncement", { clubId: club.id });
  const nextId = current.announcement.id;
  // It opens the day after the last played match, which is in the past: everyone can enroll now
  for (const c of [...squad, players["Rubén Castro"], players["Kike Lara"]]) await c.call("enroll", { announcementId: nextId });
  await players["Álex Ruiz"].call("addGuest", { announcementId: nextId, name: "Primo de Álex", position: "FORWARD" });
  await owner.call("generateTeams", { matchId: nextId, mode: "AUTO" });
  console.log(`next match ${nextId} with teams drawn and a waitlist`);

  // 3) Absences after the next match, and one muted club
  const iso = (days) => new Date(Date.now() + days * 86400_000).toISOString().slice(0, 10);
  await players["Marcos Peña"].call("createAbsence", { clubId: club.id, fromDate: iso(12), toDate: iso(19), reason: "Vacaciones" });
  await players["Iván Soto"].call("createAbsence", { clubId: club.id, fromDate: iso(20), toDate: iso(34), reason: "Lesión" });
  await players["Leo Vega"].call("setClubMuted", { clubId: club.id, muted: true });

  await sleep(200);
  console.log("done");
  console.log(`club id ${club.id} · members ${memberOf(owner.uid)}…`);
}

try {
  await main();
} catch (e) {
  console.error(e?.message ?? e, e?.details ?? "");
  process.exitCode = 1;
}
process.exit();
