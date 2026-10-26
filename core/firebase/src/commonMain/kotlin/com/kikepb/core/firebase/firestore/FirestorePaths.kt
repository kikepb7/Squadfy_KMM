package com.kikepb.core.firebase.firestore

import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.FirebaseFirestore

/**
 * Single source of truth for the Firestore data model (spec 019 plan, `specs/contracts/firebase.md`). Keep it in sync
 * with `firebase/firestore.rules` and `firebase/functions/src/paths.ts`.
 *
 * ```
 * users/{uid}                          profile (username, picture; no email)
 * users/{uid}/devices/{fcmToken}       push notification tokens
 * users/{uid}/clubSettings/{clubId}    per-club settings (mute)
 * clubs/{clubId}                       club
 * clubs/{clubId}/members/{uid}         membership (memberId = uid)
 * clubs/{clubId}/schedule/current      weekly schedule
 * clubs/{clubId}/scheduleExceptions/{yyyy-MM-dd}
 * clubs/{clubId}/matches/{matchId}
 * clubs/{clubId}/announcements/{matchId}
 * clubs/{clubId}/ratings/{memberId}
 * clubs/{clubId}/absences/{absenceId}
 * chats/{chatId}                       chat with its participants
 * chats/{chatId}/messages/{messageId}
 * ```
 */
object FirestorePaths {
    const val USERS = "users"
    const val DEVICES = "devices"
    const val CLUB_SETTINGS = "clubSettings"
    const val CLUBS = "clubs"
    const val MEMBERS = "members"
    const val SCHEDULE = "schedule"
    const val SCHEDULE_CURRENT = "current"
    const val SCHEDULE_EXCEPTIONS = "scheduleExceptions"
    const val MATCHES = "matches"
    const val ANNOUNCEMENTS = "announcements"
    const val RATINGS = "ratings"
    const val ABSENCES = "absences"
    const val CHATS = "chats"
    const val MESSAGES = "messages"
}

fun FirebaseFirestore.userDocument(uid: String): DocumentReference =
    collection(FirestorePaths.USERS).document(uid)

fun FirebaseFirestore.userCollection(uid: String, collection: String): CollectionReference =
    userDocument(uid).collection(collection)

fun FirebaseFirestore.clubDocument(clubId: String): DocumentReference =
    collection(FirestorePaths.CLUBS).document(clubId)

fun FirebaseFirestore.clubCollection(clubId: String, collection: String): CollectionReference =
    clubDocument(clubId).collection(collection)
