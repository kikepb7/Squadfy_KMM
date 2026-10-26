package com.kikepb.club.datafirebase.util

/**
 * Membership ids are `{clubId}_{uid}` (spec 019, `firebase/functions/src/paths.ts`): unique across clubs, as teams,
 * ratings and stats need, and stable when a former member comes back (BE-001 RN-12).
 */
internal object MemberIds {
    fun of(clubId: String, uid: String) = "${clubId}_$uid"

    fun clubOf(memberId: String): String? = memberId.substringBefore('_', missingDelimiterValue = "").ifEmpty { null }
}
