package com.example

import android.content.Context
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.data.HosWidgetStore
import com.example.data.HyperOsIconRepository
import com.example.model.WidgetSizeOption
import com.example.widget.HosWidgetBitmapRenderer
import com.example.widget.HosWidgetEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HOS", appName)
    }

    @Test
    fun `hyperOsIconRepository contains 9 HyperOS 3, 9 HyperOS 2, and 9 HyperOS 4 icons`() {
        val v3Icons = HyperOsIconRepository.getIconsForVersion(3)
        val v2Icons = HyperOsIconRepository.getIconsForVersion(2)
        val v4Icons = HyperOsIconRepository.getIconsForVersion(4)
        assertEquals(9, v3Icons.size)
        assertEquals(9, v2Icons.size)
        assertEquals(9, v4Icons.size)
        assertEquals(27, HyperOsIconRepository.allIcons.size)

        val expectedNames = setOf(
            "Settings",
            "Camera",
            "Calculator",
            "Files",
            "Themes",
            "Gallery",
            "Notes",
            "Security",
            "Recorder"
        )
        assertEquals(expectedNames, v3Icons.map { it.name }.toSet())
        assertEquals(expectedNames, v2Icons.map { it.name }.toSet())
        assertEquals(expectedNames, v4Icons.map { it.name }.toSet())
    }

    @Test
    fun `themes v3, v2, and v4 foreground glyphs remove outer border and preserve central brush`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        for (iconId in listOf("themes", "hos2_themes", "hos4_themes")) {
            val themesIcon = HyperOsIconRepository.getIconById(iconId)
            val glyph = HosWidgetBitmapRenderer.renderForegroundGlyphBitmap(context, themesIcon)
            val w = glyph.width
            val h = glyph.height

            // Outer edges must be 100% transparent so no square/squircle border appears on 2x1 or 1x2
            assertEquals("Left edge of $iconId", 0, Color.alpha(glyph.getPixel(0, h / 2)))
            assertEquals("Right edge of $iconId", 0, Color.alpha(glyph.getPixel(w - 1, h / 2)))
            assertEquals("Top edge of $iconId", 0, Color.alpha(glyph.getPixel(w / 2, 0)))
            assertEquals("Bottom edge of $iconId", 0, Color.alpha(glyph.getPixel(w / 2, h - 1)))
            // Lower-left purple background region under the brush in hos2_themes must also be 100% transparent
            if (iconId == "hos2_themes") {
                assertEquals(
                    "Lower-left background of hos2_themes",
                    0,
                    Color.alpha(glyph.getPixel((w * 0.45f).toInt(), (h * 0.80f).toInt()))
                )
            }
            // Central paintbrush glyph must remain fully opaque
            assertEquals("Center of $iconId", 255, Color.alpha(glyph.getPixel(w / 2, h / 2)))
        }
    }

    @Test
    fun `hos2_gallery preserves intact mountains in background and circular moon in foreground`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val galleryV2 = HyperOsIconRepository.getIconById("hos2_gallery")
        val bg = HosWidgetBitmapRenderer.renderBackgroundBitmap(context, galleryV2)
        val fg = HosWidgetBitmapRenderer.renderForegroundGlyphBitmap(context, galleryV2)

        // Moon center (~28%, 28%) must be opaque in fg
        val moonX = (fg.width * 0.28f).toInt()
        val moonY = (fg.height * 0.28f).toInt()
        assertEquals(255, Color.alpha(fg.getPixel(moonX, moonY)))

        // Left lavender mountain (~28%, 62%) and right white mountain (~72%, 58%) must be intact in bg
        val leftMtn = bg.getPixel((bg.width * 0.28f).toInt(), (bg.height * 0.62f).toInt())
        val rightMtn = bg.getPixel((bg.width * 0.72f).toInt(), (bg.height * 0.58f).toInt())
        assertTrue(Color.red(leftMtn) >= 180 && Color.green(leftMtn) >= 170)
        assertTrue(Color.red(rightMtn) >= 225 && Color.green(rightMtn) >= 225)
    }

    @Test
    fun `widgetBitmapRenderer expands background gradient for all HyperOS 3, 2, and 4 icons and sizes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        for (icon in HyperOsIconRepository.allIcons) {
            for (size in WidgetSizeOption.entries) {
                val bmp = HosWidgetBitmapRenderer.renderWidgetBitmap(context, icon, size)
                assertNotNull(bmp)
                assertTrue(bmp.width > 0 && bmp.height > 0)
            }
        }
    }

    @Test
    fun `widgetStore persists and restores independent HyperOS 3, 2, and 4 widget configurations`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        HosWidgetStore.saveWidgetRecord(context, 101, "security", WidgetSizeOption.SIZE_2X2)
        HosWidgetStore.saveWidgetRecord(context, 102, "hos2_themes", WidgetSizeOption.SIZE_2X1)
        HosWidgetStore.saveWidgetRecord(context, 103, "hos4_gallery", WidgetSizeOption.SIZE_1X2)

        val record101 = HosWidgetStore.getRecordForWidgetSync(context, 101)
        val record102 = HosWidgetStore.getRecordForWidgetSync(context, 102)
        val record103 = HosWidgetStore.getRecordForWidgetSync(context, 103)

        assertNotNull(record101)
        assertEquals("security", record101?.appId)
        assertEquals(3, record101?.appIcon?.osVersion)
        assertEquals(WidgetSizeOption.SIZE_2X2, record101?.sizeOption)

        assertNotNull(record102)
        assertEquals("hos2_themes", record102?.appId)
        assertEquals(2, record102?.appIcon?.osVersion)
        assertEquals(WidgetSizeOption.SIZE_2X1, record102?.sizeOption)

        assertNotNull(record103)
        assertEquals("hos4_gallery", record103?.appId)
        assertEquals(4, record103?.appIcon?.osVersion)
        assertEquals(WidgetSizeOption.SIZE_1X2, record103?.sizeOption)
    }

    @Test
    fun `widgetEngine builds RemoteViews for all widget sizes in HyperOS 3, 2, and 4`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsV3 = HyperOsIconRepository.getIconById("settings")
        val settingsV2 = HyperOsIconRepository.getIconById("hos2_settings")
        val settingsV4 = HyperOsIconRepository.getIconById("hos4_settings")

        for (size in WidgetSizeOption.entries) {
            val rv3 = HosWidgetEngine.buildRemoteViews(
                context = context,
                appWidgetId = 500 + size.ordinal,
                appIcon = settingsV3,
                sizeOption = size
            )
            val rv2 = HosWidgetEngine.buildRemoteViews(
                context = context,
                appWidgetId = 600 + size.ordinal,
                appIcon = settingsV2,
                sizeOption = size
            )
            val rv4 = HosWidgetEngine.buildRemoteViews(
                context = context,
                appWidgetId = 700 + size.ordinal,
                appIcon = settingsV4,
                sizeOption = size
            )
            assertNotNull(rv3)
            assertNotNull(rv2)
            assertNotNull(rv4)
        }
    }
}
