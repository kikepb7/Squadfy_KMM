package org.kikepb.squadfy.push

import org.koin.core.module.Module

/** The native FCM topic subscriber (spec 021 AC-021-07), in both backend variants: FCM serves both. */
internal expect val platformPushTopicsModule: Module
