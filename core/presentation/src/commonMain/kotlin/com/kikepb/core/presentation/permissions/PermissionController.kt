package com.kikepb.core.presentation.permissions

expect class PermissionController {
    suspend fun requestPermission(permission: Permission): PermissionState
    suspend fun isGranted(permission: Permission): Boolean
}