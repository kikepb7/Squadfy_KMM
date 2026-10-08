package com.kikepb.core.data.util

import com.kikepb.core.domain.featureflag.AppPlatform

/** Platform of this build, for per-platform flag defaults (D-13). */
expect val currentAppPlatform: AppPlatform
