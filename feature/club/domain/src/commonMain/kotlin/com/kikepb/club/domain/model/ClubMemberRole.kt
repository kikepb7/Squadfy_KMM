package com.kikepb.club.domain.model

/** BE-001 RN-2. Manager = OWNER or ADMIN; CAPTAIN has no management rights. */
enum class ClubMemberRole {
    OWNER,
    ADMIN,
    CAPTAIN,
    PLAYER;

    val isManager: Boolean get() = this == OWNER || this == ADMIN

    companion object {
        /** APP-RN-13: unknown or legacy values (e.g. pre-v1 `MEMBER`) are treated as PLAYER. */
        fun fromRaw(raw: String?): ClubMemberRole =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: PLAYER
    }
}

/** BE-001 RN-6. Optional: a member without a declared position is `null`. */
enum class PlayerPosition {
    GOALKEEPER,
    DEFENDER,
    MIDFIELDER,
    FORWARD;

    companion object {
        /** APP-RN-13: legacy free-text positions or unknown values map to "no position". */
        fun fromRaw(raw: String?): PlayerPosition? =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) }
    }
}
