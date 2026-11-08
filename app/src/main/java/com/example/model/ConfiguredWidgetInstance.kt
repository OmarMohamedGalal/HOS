package com.example.model

data class ConfiguredWidgetInstance(
    val appWidgetId: Int,
    val appIcon: HyperOsAppIcon,
    val sizeOption: WidgetSizeOption,
    val createdAtMillis: Long,
    val isBoundToLauncher: Boolean,
    val isTargetAppAvailable: Boolean
)
