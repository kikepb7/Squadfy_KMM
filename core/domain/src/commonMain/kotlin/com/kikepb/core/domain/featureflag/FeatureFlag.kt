package com.kikepb.core.domain.featureflag

/**
 * Single catalog of feature flags (spec 013, ADR-0007).
 *
 * Lifecycle: add with `pre = false, pro = false` when work starts, enable in PRE for QA,
 * enable in PRO when the spec is Done, then delete the flag and its branches.
 */
enum class FeatureFlag(
    val key: String,
    val description: String,
    val defaultInPre: Boolean,
    val defaultInPro: Boolean,
    /** PRO default on iOS when it differs from [defaultInPro] (store-review constraints, D-13). */
    val defaultInProOnIos: Boolean? = null
) {
    // App-parity features (decision D-1, backend spec BE-008)
    MATCH_GUESTS(
        key = "match_guests",
        description = "Members add up to 2 guests to an announcement (specs 005/006)",
        defaultInPre = true,
        defaultInPro = false
    ),
    SCHEDULE_EXCEPTIONS(
        key = "schedule_exceptions",
        description = "Cancel or move one week of the weekly schedule (spec 004)",
        defaultInPre = true,
        defaultInPro = false
    ),
    CUSTOM_DRAW_TIME(
        key = "custom_draw_time",
        description = "Configurable announcement close and team draw times (spec 004)",
        defaultInPre = true,
        defaultInPro = false
    ),
    MANUAL_SCORE(
        key = "manual_score",
        description = "Enter the final score manually instead of from goal events (spec 007)",
        defaultInPre = true,
        defaultInPro = false
    ),
    MEMBER_ABSENCES(
        key = "member_absences",
        description = "Members register absence periods that withdraw them from open announcements (spec 014)",
        defaultInPre = true,
        defaultInPro = false
    ),

    // User-to-user chat (D-13): Apple guideline 1.2 asks for report/block on user-generated content, which
    // needs a backend spec. Until then the chat is hidden on iOS in PRO; Android keeps it.
    CHAT(
        key = "chat",
        description = "User-to-user chat (hidden on iOS in PRO until report/block exist, D-13)",
        defaultInPre = true,
        defaultInPro = true,
        defaultInProOnIos = false
    ),

    // Release readiness (spec 011)
    ACCOUNT_DELETION(
        key = "account_deletion",
        description = "Delete the account from Profile with the password (spec 011, backend spec 010)",
        defaultInPre = true,
        // Backend spec 010 is on master (production) and the flow passed E2E (spec 015 T-011); kept as a kill switch
        defaultInPro = true
    ),

    // QA tooling and unfinished screens
    DEV_TEST_MATCH(
        key = "dev_test_match",
        description = "Button to create a test match with an immediate sign-up window",
        defaultInPre = true,
        defaultInPro = false
    ),;

    fun defaultFor(environment: AppEnvironment, platform: AppPlatform = AppPlatform.ANDROID): Boolean = when (environment) {
        AppEnvironment.PRE -> defaultInPre
        AppEnvironment.PRO -> if (platform == AppPlatform.IOS) defaultInProOnIos ?: defaultInPro else defaultInPro
    }
}
