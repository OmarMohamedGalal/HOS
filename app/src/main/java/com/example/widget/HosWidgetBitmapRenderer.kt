package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.example.model.HyperOsAppIcon
import com.example.model.WidgetSizeOption
import kotlin.math.abs
import kotlin.math.max

/**
 * Unified renderer for both the Android Home Screen RemoteViews and the in-app Compose preview,
 * supporting both HyperOS 3 icons (full-bleed square PNGs) and HyperOS 2 icons (squircle PNGs):
 * 1. Trims any transparent outer padding (`decodeRawIcon`).
 * 2. Extracts the exact pure 2D/vertical background gradient directly from the icon's pixels
 *    (`renderBackgroundBitmap`) to fill 100% of the widget container (`@id/widget_bg_image`).
 * 3. Removes 100% of the exterior square/squircle background around the central icon artwork
 *    (`renderForegroundGlyphBitmap`) so that when centered in `@id/widget_app_icon`, NO icon
 *    border or background artifact can ever appear.
 */
object HosWidgetBitmapRenderer {

    private val rawCache = HashMap<Int, Bitmap>()
    private val bgCache = HashMap<String, Bitmap>()
    private val glyphCache = HashMap<String, Bitmap>()
    private val fullSquareCache = HashMap<String, Bitmap>()
    private val previewCache = HashMap<String, Bitmap>()

    @Synchronized
    private fun decodeRawIcon(context: Context, iconResId: Int): Bitmap? {
        rawCache[iconResId]?.let { cached ->
            if (!cached.isRecycled) return cached
        }

        val options = BitmapFactory.Options().apply {
            inScaled = false
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeResource(context.resources, iconResId, options) ?: return null
        val w = decoded.width
        val h = decoded.height
        val pixels = IntArray(w * h)
        decoded.getPixels(pixels, 0, w, 0, 0, w, h)

        // Trim any transparent outer margin (such as the 8px padding on HyperOS 2 PNGs)
        // using alpha >= 180 so noisy semi-transparent border pixels are excluded.
        var minX = w
        var maxX = -1
        var minY = h
        var maxY = -1

        for (y in 0 until h) {
            val rowOffset = y * w
            for (x in 0 until w) {
                if (Color.alpha(pixels[rowOffset + x]) >= 180) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        val result = if (minX in 1 until maxX && minY in 1 until maxY &&
            (minX > 2 || minY > 2 || maxX < w - 3 || maxY < h - 3)
        ) {
            Bitmap.createBitmap(decoded, minX, minY, maxX - minX + 1, maxY - minY + 1)
        } else {
            decoded
        }

        rawCache[iconResId] = result
        return result
    }

    /**
     * Returns a 100% full-bleed square version of the original icon (with any transparent
     * squircle corners seamlessly filled by the icon's background gradient, and 100% of the
     * opaque interior untouched). Used for 1x1 and 2x2 square widgets and grid tiles.
     */
    @Synchronized
    fun renderFullSquareIconBitmap(
        context: Context,
        appIcon: HyperOsAppIcon
    ): Bitmap {
        fullSquareCache[appIcon.id]?.let { cached ->
            if (!cached.isRecycled) return cached
        }

        val rawIcon = decodeRawIcon(context, appIcon.iconResId)
            ?: return renderBackgroundBitmap(context, appIcon)

        if (appIcon.osVersion == 3 || appIcon.osVersion == 4) {
            fullSquareCache[appIcon.id] = rawIcon
            return rawIcon
        }

        val w = rawIcon.width
        val h = rawIcon.height
        val rawPixels = IntArray(w * h)
        rawIcon.getPixels(rawPixels, 0, w, 0, 0, w, h)

        val bgBitmap = renderBackgroundBitmap(context, appIcon)
        val bgPixels = IntArray(w * h)
        bgBitmap.getPixels(bgPixels, 0, w, 0, 0, w, h)

        val outPixels = IntArray(w * h)
        for (i in 0 until w * h) {
            val c = rawPixels[i]
            val a = Color.alpha(c)
            if (a >= 248) {
                outPixels[i] = Color.argb(255, Color.red(c), Color.green(c), Color.blue(c))
            } else {
                outPixels[i] = bgPixels[i]
            }
        }

        val fullBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        fullBmp.setPixels(outPixels, 0, w, 0, 0, w, h)
        fullSquareCache[appIcon.id] = fullBmp
        return fullBmp
    }

    /**
     * Returns a pure, seamless background gradient bitmap reconstructed directly
     * from the icon's pixels. Stretches cleanly via fitXY to fill any
     * 1x2, 2x1, 2x2, or 1x1 widget aspect ratio with zero seams.
     */
    @Synchronized
    fun renderBackgroundBitmap(
        context: Context,
        appIcon: HyperOsAppIcon
    ): Bitmap {
        bgCache[appIcon.id]?.let { cached ->
            if (!cached.isRecycled) return cached
        }

        val rawIcon = decodeRawIcon(context, appIcon.iconResId)
            ?: return createFallbackBackground(appIcon)

        val w = rawIcon.width
        val h = rawIcon.height
        val rawPixels = IntArray(w * h)
        rawIcon.getPixels(rawPixels, 0, w, 0, 0, w, h)

        val bgPixels = buildPureBackgroundPixels(rawPixels, w, h, appIcon)
        val bgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bgBitmap.setPixels(bgPixels, 0, w, 0, 0, w, h)

        bgCache[appIcon.id] = bgBitmap
        return bgBitmap
    }

    /**
     * Returns the icon bitmap with 100% of its exterior square/squircle background removed
     * (transparent alpha = 0), leaving only the central icon glyph so its outer edges
     * never appear when centered inside a 2x1 or 1x2 widget.
     */
    @Synchronized
    fun renderForegroundGlyphBitmap(
        context: Context,
        appIcon: HyperOsAppIcon
    ): Bitmap {
        glyphCache[appIcon.id]?.let { cached ->
            if (!cached.isRecycled) return cached
        }

        val rawIcon = decodeRawIcon(context, appIcon.iconResId)
            ?: return Bitmap.createBitmap(240, 240, Bitmap.Config.ARGB_8888)

        val w = rawIcon.width
        val h = rawIcon.height
        val rawPixels = IntArray(w * h)
        rawIcon.getPixels(rawPixels, 0, w, 0, 0, w, h)

        val bgPixels = buildPureBackgroundPixels(rawPixels, w, h, appIcon)
        val outPixels = IntArray(w * h)

        // Special handling for HyperOS 2 Gallery (com.miui.gallery.png):
        // The Sky + Two Flowing Mountains are preserved intact in renderBackgroundBitmap
        // (with the circular Moon airbrushed out of the sky), while renderForegroundGlyphBitmap
        // extracts ONLY the circular Moon so it remains a 1:1 circle in 2x1 and 1x2 widgets.
        if (appIcon.id == "hos2_gallery") {
            val moonMinX = (w * 0.11f).toInt()
            val moonMaxX = (w * 0.44f).toInt()
            val moonMinY = (h * 0.11f).toInt()
            val moonMaxY = (h * 0.44f).toInt()

            for (y in moonMinY..moonMaxY) {
                val rowOffset = y * w
                for (x in moonMinX..moonMaxX) {
                    val idx = rowOffset + x
                    val c = rawPixels[idx]
                    val bg = bgPixels[idx]
                    val diff = maxColorDiff(c, bg)
                    if (diff > 10) {
                        val alphaFactor = ((diff - 10f) / 26f).coerceIn(0f, 1f)
                        val newAlpha = (255f * alphaFactor).toInt().coerceIn(0, 255)
                        outPixels[idx] = Color.argb(newAlpha, Color.red(c), Color.green(c), Color.blue(c))
                    }
                }
            }

            val glyphBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            glyphBitmap.setPixels(outPixels, 0, w, 0, 0, w, h)
            glyphCache[appIcon.id] = glyphBitmap
            return glyphBitmap
        }

        // Special exact handling for HyperOS 2 Themes (com.android.thememanager.png):
        // In hos2_themes, bgPixels matches the native background outside the paintbrush with diff == 0,
        // and every pixel of the frosted bristles and white handle has a Green channel jump >= 34.
        if (appIcon.id == "hos2_themes") {
            for (y in 0 until h) {
                val rowOffset = y * w
                for (x in 0 until w) {
                    val idx = rowOffset + x
                    val c = rawPixels[idx]
                    if (Color.alpha(c) < 248) {
                        outPixels[idx] = Color.TRANSPARENT
                        continue
                    }
                    val bg = bgPixels[idx]
                    val gDiff = Color.green(c) - Color.green(bg)
                    val mDiff = maxColorDiff(c, bg)
                    val signal = max(gDiff, mDiff - 8)
                    if (signal <= 10) {
                        outPixels[idx] = Color.TRANSPARENT
                    } else {
                        val alphaFactor = ((signal - 10f) / 24f).coerceIn(0f, 1f)
                        val newAlpha = (255f * alphaFactor).toInt().coerceIn(0, 255)
                        outPixels[idx] = Color.argb(newAlpha, Color.red(c), Color.green(c), Color.blue(c))
                    }
                }
            }

            val glyphBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            glyphBitmap.setPixels(outPixels, 0, w, 0, 0, w, h)
            glyphCache[appIcon.id] = glyphBitmap
            return glyphBitmap
        }

        val isNotesV3 = appIcon.id == "notes" || appIcon.id == "hos4_notes"
        rawPixels.copyInto(outPixels)

        // Identify pixels within 4px of any semi-transparent squircle corner/rim pixel
        val nearRim = BooleanArray(w * h)
        for (y in 0 until h) {
            val rowOffset = y * w
            for (x in 0 until w) {
                if (Color.alpha(rawPixels[rowOffset + x]) < 245) {
                    val y0 = (y - 4).coerceAtLeast(0)
                    val y1 = (y + 4).coerceAtMost(h - 1)
                    val x0 = (x - 4).coerceAtLeast(0)
                    val x1 = (x + 4).coerceAtMost(w - 1)
                    for (ny in y0..y1) {
                        val nRow = ny * w
                        for (nx in x0..x1) {
                            nearRim[nRow + nx] = true
                        }
                    }
                }
            }
        }

        // Exterior flood-fill (BFS from the 4 edges of the icon) to find all exterior
        // background pixels connected to the outer border.
        val visited = BooleanArray(w * h)
        val queue = IntArray(w * h)
        var head = 0
        var tail = 0

        fun tryEnqueue(x: Int, y: Int) {
            if (x < 0 || x >= w || y < 0 || y >= h) return
            val idx = y * w + x
            if (visited[idx]) return
            val c = rawPixels[idx]
            val alpha = Color.alpha(c)
            val diff = maxColorDiff(c, bgPixels[idx])
            val threshold = if (nearRim[idx]) 34 else 24
            if (alpha < 245 || diff <= threshold) {
                visited[idx] = true
                queue[tail++] = idx
            }
        }

        for (x in 0 until w) {
            tryEnqueue(x, 0)
            tryEnqueue(x, h - 1)
        }
        for (y in 0 until h) {
            tryEnqueue(0, y)
            tryEnqueue(w - 1, y)
        }

        while (head < tail) {
            val idx = queue[head++]
            val x = idx % w
            val y = idx / w
            tryEnqueue(x - 1, y)
            tryEnqueue(x + 1, y)
            tryEnqueue(x, y - 1)
            tryEnqueue(x, y + 1)
        }

        for (y in 0 until h) {
            for (x in 0 until w) {
                val idx = y * w + x
                val c = rawPixels[idx]
                val bg = bgPixels[idx]
                val diff = maxColorDiff(c, bg)

                var touchesUnvisited = false
                var touchesVisited = false
                for (dy in -1..1) {
                    val ny = y + dy
                    if (ny < 0 || ny >= h) continue
                    for (dx in -1..1) {
                        val nx = x + dx
                        if (nx < 0 || nx >= w) continue
                        if (visited[ny * w + nx]) {
                            touchesVisited = true
                        } else {
                            touchesUnvisited = true
                        }
                    }
                }

                if (visited[idx] && !touchesUnvisited) {
                    outPixels[idx] = Color.TRANSPARENT
                } else if (touchesVisited && touchesUnvisited) {
                    val alphaFactor = when {
                        Color.alpha(c) < 220 -> 0f
                        diff <= 16 -> 0f
                        diff >= 42 -> 1f
                        else -> (diff - 16f) / 26f
                    }
                    val origAlpha = Color.alpha(c)
                    val newAlpha = (origAlpha * alphaFactor).toInt().coerceIn(0, 255)
                    outPixels[idx] = Color.argb(newAlpha, Color.red(c), Color.green(c), Color.blue(c))
                }

                // For HyperOS 3 Notes, softly fade the top-right corner where the diagonal pencil reaches the edge
                if (isNotesV3) {
                    val normX = x.toFloat() / (w - 1f)
                    val normY = y.toFloat() / (h - 1f)
                    val edgeDist = minOf(1f - normX, normY)
                    if (normX > 0.82f && normY < 0.18f) {
                        val fade = (edgeDist / 0.18f).coerceIn(0f, 1f)
                        val curA = Color.alpha(outPixels[idx])
                        outPixels[idx] = Color.argb(
                            (curA * fade).toInt(),
                            Color.red(outPixels[idx]),
                            Color.green(outPixels[idx]),
                            Color.blue(outPixels[idx])
                        )
                    }
                }
            }
        }

        val glyphBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        glyphBitmap.setPixels(outPixels, 0, w, 0, 0, w, h)
        glyphCache[appIcon.id] = glyphBitmap
        return glyphBitmap
    }

    /**
     * Composites the expanded background gradient and the centered foreground glyph into a
     * single rounded-squircle bitmap for in-app previews and grid tiles.
     */
    @Synchronized
    fun renderWidgetBitmap(
        context: Context,
        appIcon: HyperOsAppIcon,
        sizeOption: WidgetSizeOption
    ): Bitmap {
        val cacheKey = "${appIcon.id}_${sizeOption.key}"
        previewCache[cacheKey]?.let { cached ->
            if (!cached.isRecycled) return cached
        }

        val (targetWidth, targetHeight, cornerRadius) = when (sizeOption) {
            WidgetSizeOption.SIZE_2X2 -> Triple(440, 440, 104f)
            WidgetSizeOption.SIZE_2X1 -> Triple(540, 250, 74f)
            WidgetSizeOption.SIZE_1X2 -> Triple(250, 560, 74f)
            WidgetSizeOption.SIZE_1X1 -> Triple(260, 260, 64f)
        }

        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val clipPath = Path().apply {
            addRoundRect(
                RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat()),
                cornerRadius,
                cornerRadius,
                Path.Direction.CW
            )
        }
        canvas.save()
        canvas.clipPath(clipPath)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)

        if (sizeOption == WidgetSizeOption.SIZE_2X2 || sizeOption == WidgetSizeOption.SIZE_1X1) {
            // For 1x1 and 2x2 square widgets/tiles, render the untouched full-bleed square icon
            val fullSquare = renderFullSquareIconBitmap(context, appIcon)
            canvas.drawBitmap(
                fullSquare,
                Rect(0, 0, fullSquare.width, fullSquare.height),
                RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat()),
                paint
            )
        } else {
            // 1. Draw the pure extracted background gradient stretched across the entire container
            val bgBitmap = renderBackgroundBitmap(context, appIcon)
            canvas.drawBitmap(
                bgBitmap,
                Rect(0, 0, bgBitmap.width, bgBitmap.height),
                RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat()),
                paint
            )

            // 2. Center the transparent-background foreground glyph in the middle
            val iconSide = minOf(targetWidth, targetHeight).toFloat()
            val iconLeft = (targetWidth - iconSide) / 2f
            val iconTop = (targetHeight - iconSide) / 2f

            val fgBitmap = renderForegroundGlyphBitmap(context, appIcon)
            canvas.drawBitmap(
                fgBitmap,
                Rect(0, 0, fgBitmap.width, fgBitmap.height),
                RectF(iconLeft, iconTop, iconLeft + iconSide, iconTop + iconSide),
                paint
            )
        }

        canvas.restore()
        previewCache[cacheKey] = output
        return output
    }

    /**
     * Reconstructs the pure 2D background of an icon, handling full-bleed square icons (HyperOS 3),
     * rounded squircle icons (HyperOS 2), the curved 2D gradient of HyperOS 2 Themes, and the
     * panoramic Sky + Mountains landscape of HyperOS 2 Gallery.
     */
    private fun buildPureBackgroundPixels(
        rawPixels: IntArray,
        w: Int,
        h: Int,
        appIcon: HyperOsAppIcon
    ): IntArray {
        val isNotesV3 = appIcon.id == "notes" || appIcon.id == "hos4_notes"

        val leftColors = IntArray(h)
        val rightColors = IntArray(h)
        val rowXStart = IntArray(h)
        val rowXEnd = IntArray(h)

        var firstFullY = -1
        var lastFullY = -1

        for (y in 0 until h) {
            val rowOffset = y * w
            var xStart = -1
            var xEnd = -1
            for (x in 0 until w) {
                if (Color.alpha(rawPixels[rowOffset + x]) >= 248) {
                    xStart = x
                    break
                }
            }
            for (x in w - 1 downTo 0) {
                if (Color.alpha(rawPixels[rowOffset + x]) >= 248) {
                    xEnd = x
                    break
                }
            }
            rowXStart[y] = xStart
            rowXEnd[y] = xEnd

            val span = xEnd - xStart
            if (xStart >= 0 && span >= (w * 0.74f).toInt()) {
                val lx = (xStart + 4).coerceAtMost(w - 1)
                val rx = (xEnd - 4).coerceAtLeast(0)
                val lc = rawPixels[rowOffset + lx]
                val rc = if (isNotesV3) lc else rawPixels[rowOffset + rx]
                leftColors[y] = lc
                rightColors[y] = rc
                if (firstFullY == -1) firstFullY = y
                lastFullY = y
            }
        }

        if (firstFullY == -1) {
            val fallback = IntArray(w * h)
            for (i in fallback.indices) fallback[i] = appIcon.bgTopColor
            return fallback
        }

        // Fill top rounded corner rows (y < firstFullY)
        val topRefL = leftColors[firstFullY]
        val topRefR = rightColors[firstFullY]
        val topDeltaR = (Color.red(topRefR) - Color.red(topRefL)) / 2
        val topDeltaG = (Color.green(topRefR) - Color.green(topRefL)) / 2
        val topDeltaB = (Color.blue(topRefR) - Color.blue(topRefL)) / 2

        for (y in firstFullY - 1 downTo 0) {
            val xs = rowXStart[y]
            val xe = rowXEnd[y]
            if (xs >= 0 && xe - xs >= (w * 0.28f).toInt()) {
                val midX = (xs + xe) / 2
                val midC = rawPixels[y * w + midX]
                val mr = Color.red(midC)
                val mg = Color.green(midC)
                val mb = Color.blue(midC)
                leftColors[y] = Color.argb(
                    255,
                    (mr - topDeltaR).coerceIn(0, 255),
                    (mg - topDeltaG).coerceIn(0, 255),
                    (mb - topDeltaB).coerceIn(0, 255)
                )
                rightColors[y] = Color.argb(
                    255,
                    (mr + topDeltaR).coerceIn(0, 255),
                    (mg + topDeltaG).coerceIn(0, 255),
                    (mb + topDeltaB).coerceIn(0, 255)
                )
            } else {
                leftColors[y] = leftColors[y + 1]
                rightColors[y] = rightColors[y + 1]
            }
        }

        // Fill bottom rounded corner rows (y > lastFullY)
        val botRefL = leftColors[lastFullY]
        val botRefR = rightColors[lastFullY]
        val botDeltaR = (Color.red(botRefR) - Color.red(botRefL)) / 2
        val botDeltaG = (Color.green(botRefR) - Color.green(botRefL)) / 2
        val botDeltaB = (Color.blue(botRefR) - Color.blue(botRefL)) / 2

        for (y in lastFullY + 1 until h) {
            val xs = rowXStart[y]
            val xe = rowXEnd[y]
            if (xs >= 0 && xe - xs >= (w * 0.28f).toInt()) {
                val midX = (xs + xe) / 2
                val midC = rawPixels[y * w + midX]
                val mr = Color.red(midC)
                val mg = Color.green(midC)
                val mb = Color.blue(midC)
                leftColors[y] = Color.argb(
                    255,
                    (mr - botDeltaR).coerceIn(0, 255),
                    (mg - botDeltaG).coerceIn(0, 255),
                    (mb - botDeltaB).coerceIn(0, 255)
                )
                rightColors[y] = Color.argb(
                    255,
                    (mr + botDeltaR).coerceIn(0, 255),
                    (mg + botDeltaG).coerceIn(0, 255),
                    (mb + botDeltaB).coerceIn(0, 255)
                )
            } else {
                leftColors[y] = leftColors[y - 1]
                rightColors[y] = rightColors[y - 1]
            }
        }

        val bgPixels = IntArray(w * h)
        for (y in 0 until h) {
            val rowOffset = y * w
            val leftColor = leftColors[y]
            val rightColor = rightColors[y]

            val rL = Color.red(leftColor)
            val gL = Color.green(leftColor)
            val bL = Color.blue(leftColor)

            val rR = Color.red(rightColor)
            val gR = Color.green(rightColor)
            val bR = Color.blue(rightColor)

            for (x in 0 until w) {
                val t = if (w > 1) x.toFloat() / (w - 1f) else 0f
                val r = (rL + (rR - rL) * t + 0.5f).toInt().coerceIn(0, 255)
                val g = (gL + (gR - gL) * t + 0.5f).toInt().coerceIn(0, 255)
                val b = (bL + (bR - bL) * t + 0.5f).toInt().coerceIn(0, 255)
                bgPixels[rowOffset + x] = Color.argb(255, r, g, b)
            }
        }

        // For HyperOS 2 Themes: preserve the exact curved 2D background of the icon outside
        // the diagonal paintbrush, and interpolate strictly across the paintbrush span on each row.
        if (appIcon.id == "hos2_themes") {
            for (y in 0 until h) {
                val rowOffset = y * w
                val xs = rowXStart[y]
                val xe = rowXEnd[y]
                if (xs < 0 || xe - xs < (w * 0.35f).toInt()) continue

                val safeLeft = (xs + 4).coerceAtMost(w - 1)
                val safeRight = (xe - 4).coerceAtLeast(0)
                if (safeLeft >= safeRight) continue

                var brushLeft = -1
                var brushRight = -1
                for (x in safeLeft..safeRight) {
                    val c = rawPixels[rowOffset + x]
                    val lin = bgPixels[rowOffset + x]
                    if (Color.green(c) - Color.green(lin) > 10) {
                        if (brushLeft == -1) brushLeft = x
                        brushRight = x
                    }
                }

                // Copy exact native background pixels inside [safeLeft..safeRight] outside the brush
                for (x in safeLeft..safeRight) {
                    if (brushLeft == -1 || x < brushLeft - 2 || x > brushRight + 2) {
                        val c = rawPixels[rowOffset + x]
                        bgPixels[rowOffset + x] = Color.argb(255, Color.red(c), Color.green(c), Color.blue(c))
                    }
                }

                // Interpolate smoothly across the paintbrush span [anchorLeft..anchorRight]
                if (brushLeft != -1) {
                    val anchorLeft = (brushLeft - 3).coerceAtLeast(safeLeft)
                    val anchorRight = (brushRight + 3).coerceAtMost(safeRight)
                    val cL = bgPixels[rowOffset + anchorLeft]
                    val cR = bgPixels[rowOffset + anchorRight]
                    val span = (anchorRight - anchorLeft).coerceAtLeast(1)
                    for (x in anchorLeft..anchorRight) {
                        val t = (x - anchorLeft).toFloat() / span.toFloat()
                        bgPixels[rowOffset + x] = lerpColor(cL, cR, t)
                    }
                }
            }
        }

        // For HyperOS 2 Gallery: preserve the complete Sky + Left Lavender Mountain + Right White Mountain
        // inside the squircle, and airbrush out ONLY the circular Moon in the upper-left sky.
        if (appIcon.id == "hos2_gallery") {
            val moonMinX = (w * 0.11f).toInt()
            val moonMaxX = (w * 0.44f).toInt()
            val moonMinY = (h * 0.11f).toInt()
            val moonMaxY = (h * 0.44f).toInt()

            for (y in 0 until h) {
                val rowOffset = y * w
                val xs = rowXStart[y]
                val xe = rowXEnd[y]
                if (xs < 0 || xe - xs < (w * 0.25f).toInt()) continue

                val safeLeft = (xs + 3).coerceAtMost(w - 1)
                val safeRight = (xe - 3).coerceAtLeast(0)
                for (x in safeLeft..safeRight) {
                    val c = rawPixels[rowOffset + x]
                    bgPixels[rowOffset + x] = Color.argb(255, Color.red(c), Color.green(c), Color.blue(c))
                }

                if (y in moonMinY..moonMaxY && safeLeft < moonMinX && safeRight > moonMaxX) {
                    val cL = bgPixels[rowOffset + moonMinX - 1]
                    val cR = bgPixels[rowOffset + moonMaxX + 1]
                    val span = (moonMaxX - moonMinX + 2).coerceAtLeast(1)
                    for (x in moonMinX..moonMaxX) {
                        val t = (x - (moonMinX - 1)).toFloat() / span.toFloat()
                        bgPixels[rowOffset + x] = lerpColor(cL, cR, t)
                    }
                }

                // Fill outer left/right corner pixels of row y from safeLeft and safeRight
                val leftEdgeColor = bgPixels[rowOffset + safeLeft]
                for (x in 0 until safeLeft) {
                    bgPixels[rowOffset + x] = leftEdgeColor
                }
                val rightEdgeColor = bgPixels[rowOffset + safeRight]
                for (x in (safeRight + 1) until w) {
                    bgPixels[rowOffset + x] = rightEdgeColor
                }
            }

            // Copy top/bottom corner rows from first/last valid rows
            var firstValidY = 0
            while (firstValidY < h && rowXEnd[firstValidY] - rowXStart[firstValidY] < (w * 0.25f).toInt()) {
                firstValidY++
            }
            if (firstValidY in 1 until h) {
                for (y in (firstValidY - 1) downTo 0) {
                    System.arraycopy(bgPixels, (y + 1) * w, bgPixels, y * w, w)
                }
            }
            var lastValidY = h - 1
            while (lastValidY >= 0 && rowXEnd[lastValidY] - rowXStart[lastValidY] < (w * 0.25f).toInt()) {
                lastValidY--
            }
            if (lastValidY in 0 until (h - 1)) {
                for (y in (lastValidY + 1) until h) {
                    System.arraycopy(bgPixels, (y - 1) * w, bgPixels, y * w, w)
                }
            }
        }

        return bgPixels
    }

    private fun lerpColor(c1: Int, c2: Int, t: Float): Int {
        val r = (Color.red(c1) + (Color.red(c2) - Color.red(c1)) * t + 0.5f).toInt().coerceIn(0, 255)
        val g = (Color.green(c1) + (Color.green(c2) - Color.green(c1)) * t + 0.5f).toInt().coerceIn(0, 255)
        val b = (Color.blue(c1) + (Color.blue(c2) - Color.blue(c1)) * t + 0.5f).toInt().coerceIn(0, 255)
        return Color.argb(255, r, g, b)
    }

    private fun maxColorDiff(c1: Int, c2: Int): Int {
        val dr = abs(Color.red(c1) - Color.red(c2))
        val dg = abs(Color.green(c1) - Color.green(c2))
        val db = abs(Color.blue(c1) - Color.blue(c2))
        return max(dr, max(dg, db))
    }

    private fun createFallbackBackground(appIcon: HyperOsAppIcon): Bitmap {
        val bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(appIcon.bgTopColor)
        return bmp
    }
}
