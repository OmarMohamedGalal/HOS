package com.example.model

enum class WidgetSizeOption(
    val key: String,
    val label: String,
    val shortLabel: String,
    val spanX: Int,
    val spanY: Int,
    val minWidthDp: Int,
    val minHeightDp: Int,
    val aspectHint: String,
    val occupiedSlotsText: String
) {
    SIZE_1X1(
        key = "1x1",
        label = "1×1",
        shortLabel = "1x1",
        spanX = 1,
        spanY = 1,
        minWidthDp = 40,
        minHeightDp = 40,
        aspectHint = "Compact 1×1 Grid",
        occupiedSlotsText = "1 icon grid space (1 horizontal, 1 vertical)"
    ),
    SIZE_2X1(
        key = "2x1",
        label = "2×1",
        shortLabel = "2x1",
        spanX = 2,
        spanY = 1,
        minWidthDp = 110,
        minHeightDp = 40,
        aspectHint = "Aspect ratio",
        occupiedSlotsText = "2 icon grid spaces (2 horizontal, 1 vertical)"
    ),
    SIZE_1X2(
        key = "1x2",
        label = "1×2",
        shortLabel = "1x2",
        spanX = 1,
        spanY = 2,
        minWidthDp = 40,
        minHeightDp = 110,
        aspectHint = "Touch to preview",
        occupiedSlotsText = "2 icon grid spaces (1 horizontal, 2 vertical)"
    ),
    SIZE_2X2(
        key = "2x2",
        label = "2×2",
        shortLabel = "2x2",
        spanX = 2,
        spanY = 2,
        minWidthDp = 110,
        minHeightDp = 110,
        aspectHint = "Standard 4×4 Grid",
        occupiedSlotsText = "4 icon grid spaces (2 horizontal, 2 vertical)"
    );

    companion object {
        fun fromKey(key: String?): WidgetSizeOption {
            if (key.isNullOrBlank()) return SIZE_2X2
            val normalized = key.replace(" ", "").replace("×", "x").lowercase()
            return entries.firstOrNull { it.key == normalized } ?: SIZE_2X2
        }
    }
}
