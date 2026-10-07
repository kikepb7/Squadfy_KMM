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
    val defaultInPro: Boolean
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

    // QA tooling and unfinished screens
    DEV_TEST_MATCH(
        key = "dev_test_match",
        description = "Button to create a test match with an immediate sign-up window",
        defaultInPre = true,
        defaultInPro = false
    ),
    HOME_RECENT_MATCHES(
        key = "home_recent_matches",
        description = "Recent matches section on Home (mock data until spec 010)",
        defaultInPre = true,
        defaultInPro = false
    ),
    HOME_NEWS(
        key = "home_news",
        description = "News section on Home (mock data, out of the MVP)",
        defaultInPre = true,
        defaultInPro = false
    );

    fun defaultFor(environment: AppEnvironment): Boolean = when (environment) {
        AppEnvironment.PRE -> defaultInPre
        AppEnvironment.PRO -> defaultInPro
    }
}
