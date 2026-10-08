package com.kikepb.core.domain.config

/** Public legal pages linked from Register and Profile (spec 011 AC-011-07/08). Values come from the build config. */
data class LegalLinks(
    val privacyPolicyUrl: String,
    /** Web page to delete the account without the app (Google Play requirement, backend spec 010). */
    val accountDeletionUrl: String
)
