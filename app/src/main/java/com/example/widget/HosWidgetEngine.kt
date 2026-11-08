package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import com.example.R
import com.example.data.HosWidgetStore
import com.example.data.StoredWidgetRecord
import com.example.model.ConfiguredWidgetInstance
import com.example.model.HyperOsAppIcon
import com.example.model.WidgetSizeOption
import com.example.system.SystemAppLauncher

enum class WidgetPinResult {
    PIN_DIALOG_REQUESTED,
    UNSUPPORTED_LAUNCHER
}

object HosWidgetEngine {

    const val EXTRA_PIN_APP_ID = "extra_pin_app_id"
    const val EXTRA_PIN_SIZE_KEY = "extra_pin_size_key"

    fun providerComponentForSize(context: Context, sizeOption: WidgetSizeOption): ComponentName {
        val providerClass = when (sizeOption) {
            WidgetSizeOption.SIZE_1X1 -> HosWidget1x1Provider::class.java
            WidgetSizeOption.SIZE_2X1 -> HosWidget2x1Provider::class.java
            WidgetSizeOption.SIZE_1X2 -> HosWidget1x2Provider::class.java
            WidgetSizeOption.SIZE_2X2 -> HosWidget2x2Provider::class.java
        }
        return ComponentName(context, providerClass)
    }

    fun getActiveLauncherWidgetIds(context: Context): Map<Int, WidgetSizeOption> {
        val manager = AppWidgetManager.getInstance(context)
        val result = mutableMapOf<Int, WidgetSizeOption>()
        for (size in WidgetSizeOption.entries) {
            val ids = try {
                manager.getAppWidgetIds(providerComponentForSize(context, size))
            } catch (_: Exception) {
                IntArray(0)
            }
            for (id in ids) {
                result[id] = size
            }
        }
        return result
    }

    fun getSynchronizedWidgets(context: Context): List<ConfiguredWidgetInstance> {
        val activeLauncherMap = getActiveLauncherWidgetIds(context)
        val storedRecords = HosWidgetStore.getAllRecordsSync(context).toMutableList()
        val storedById = storedRecords.associateBy { it.appWidgetId }.toMutableMap()

        for ((widgetId, providerSize) in activeLauncherMap) {
            if (!storedById.containsKey(widgetId)) {
                val pending = HosWidgetStore.consumePendingPinRequest(context, providerSize)
                val resolvedAppId = pending?.first ?: HosWidgetStore.getLastSelectedAppId(context)
                val record = HosWidgetStore.saveWidgetRecord(
                    context = context,
                    appWidgetId = widgetId,
                    appId = resolvedAppId,
                    sizeOption = providerSize
                )
                storedById[widgetId] = record
            }
        }

        val allRecords = HosWidgetStore.getAllRecordsSync(context)
        return allRecords.map { record ->
            val appIcon = record.appIcon
            ConfiguredWidgetInstance(
                appWidgetId = record.appWidgetId,
                appIcon = appIcon,
                sizeOption = record.sizeOption,
                createdAtMillis = record.createdAtMillis,
                isBoundToLauncher = activeLauncherMap.containsKey(record.appWidgetId),
                isTargetAppAvailable = SystemAppLauncher.isPrimaryAppAvailable(context, appIcon)
            )
        }
    }

    fun pruneUnboundSystemWidgetIds(context: Context) {
        val activeIds = getActiveLauncherWidgetIds(context).keys
        val allRecords = HosWidgetStore.getAllRecordsSync(context)
        val staleIds = allRecords
            .filter { it.appWidgetId > 0 && it.appWidgetId !in activeIds }
            .map { it.appWidgetId }
            .toIntArray()
        if (staleIds.isNotEmpty()) {
            HosWidgetStore.removeWidgetRecords(context, staleIds)
        }
    }

    fun updateWidgetInstance(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        fallbackSize: WidgetSizeOption
    ) {
        var record: StoredWidgetRecord? = HosWidgetStore.getRecordForWidgetSync(context, appWidgetId)
        if (record == null) {
            val pending = HosWidgetStore.consumePendingPinRequest(context, fallbackSize)
            val appId = pending?.first ?: HosWidgetStore.getLastSelectedAppId(context)
            val sizeOption = pending?.second ?: fallbackSize
            record = HosWidgetStore.saveWidgetRecord(
                context = context,
                appWidgetId = appWidgetId,
                appId = appId,
                sizeOption = sizeOption
            )
        }

        val views = buildRemoteViews(
            context = context,
            appWidgetId = appWidgetId,
            appIcon = record.appIcon,
            sizeOption = record.sizeOption
        )
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    fun refreshAllActiveWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val activeMap = getActiveLauncherWidgetIds(context)
        for ((widgetId, size) in activeMap) {
            updateWidgetInstance(context, appWidgetManager, widgetId, size)
        }
    }

    /**
     * Builds the RemoteViews for a widget instance using the two-layer architecture:
     * - `@id/widget_bg_image` (`scaleType="fitXY"`, `match_parent x match_parent` inside a
     *   `clipToOutline="true"` squircle container) fills 100% of the widget's launcher bounds
     *   with the pure extracted background gradient (eliminating all empty space in 1x2, 2x1, 2x2).
     * - `@id/widget_app_icon` (`scaleType="fitCenter"`, centered) draws the foreground icon glyph
     *   with its exterior square background removed so no square border ever appears.
     */
    fun buildRemoteViews(
        context: Context,
        appWidgetId: Int,
        appIcon: HyperOsAppIcon,
        sizeOption: WidgetSizeOption
    ): RemoteViews {
        val layoutRes = when (sizeOption) {
            WidgetSizeOption.SIZE_1X1 -> R.layout.widget_hos_2x2
            WidgetSizeOption.SIZE_2X2 -> R.layout.widget_hos_2x2
            WidgetSizeOption.SIZE_2X1 -> R.layout.widget_hos_2x1
            WidgetSizeOption.SIZE_1X2 -> R.layout.widget_hos_1x2
        }

        val views = RemoteViews(context.packageName, layoutRes)

        val isSquareWidget = sizeOption == WidgetSizeOption.SIZE_1X1 || sizeOption == WidgetSizeOption.SIZE_2X2
        val bgBitmap = if (isSquareWidget) {
            HosWidgetBitmapRenderer.renderFullSquareIconBitmap(
                context = context,
                appIcon = appIcon
            )
        } else {
            HosWidgetBitmapRenderer.renderBackgroundBitmap(
                context = context,
                appIcon = appIcon
            )
        }
        val fgGlyphBitmap = HosWidgetBitmapRenderer.renderForegroundGlyphBitmap(
            context = context,
            appIcon = appIcon
        )

        views.setImageViewBitmap(R.id.widget_bg_image, bgBitmap)
        views.setImageViewBitmap(R.id.widget_app_icon, fgGlyphBitmap)
        views.setContentDescription(R.id.widget_app_icon, appIcon.name)

        val launchPendingIntent = createLaunchPendingIntent(
            context = context,
            appWidgetId = appWidgetId,
            appIcon = appIcon
        )
        views.setOnClickPendingIntent(R.id.widget_root, launchPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_card_container, launchPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_app_icon, launchPendingIntent)

        return views
    }

    fun requestPinWidget(
        context: Context,
        appIcon: HyperOsAppIcon,
        sizeOption: WidgetSizeOption
    ): WidgetPinResult {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !appWidgetManager.isRequestPinAppWidgetSupported) {
            return WidgetPinResult.UNSUPPORTED_LAUNCHER
        }

        HosWidgetStore.setPendingPinRequest(context, appIcon.id, sizeOption)

        val providerComponent = providerComponentForSize(context, sizeOption)
        val previewViews = buildRemoteViews(
            context = context,
            appWidgetId = 0,
            appIcon = appIcon,
            sizeOption = sizeOption
        )
        val extras = Bundle().apply {
            putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, previewViews)
        }

        val callbackIntent = Intent(context, HosWidgetPinReceiver::class.java).apply {
            action = HosWidgetPinReceiver.ACTION_WIDGET_PINNED
            data = Uri.parse("hos://pin/${appIcon.id}/${sizeOption.key}/${System.currentTimeMillis()}")
            putExtra(EXTRA_PIN_APP_ID, appIcon.id)
            putExtra(EXTRA_PIN_SIZE_KEY, sizeOption.key)
        }

        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val successCallback = PendingIntent.getBroadcast(
            context,
            (System.currentTimeMillis() and 0xFFFFFFF).toInt(),
            callbackIntent,
            pendingFlags
        )

        val requested = try {
            appWidgetManager.requestPinAppWidget(providerComponent, extras, successCallback)
        } catch (_: Exception) {
            false
        }

        return if (requested) {
            WidgetPinResult.PIN_DIALOG_REQUESTED
        } else {
            HosWidgetStore.clearPendingPinRequest(context)
            WidgetPinResult.UNSUPPORTED_LAUNCHER
        }
    }

    fun isPinSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        return try {
            AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported
        } catch (_: Exception) {
            false
        }
    }

    private fun createLaunchPendingIntent(
        context: Context,
        appWidgetId: Int,
        appIcon: HyperOsAppIcon
    ): PendingIntent {
        val intent = Intent(context, HosWidgetLaunchActivity::class.java).apply {
            action = HosWidgetLaunchActivity.ACTION_LAUNCH_FROM_WIDGET
            data = Uri.parse("hos://launch/$appWidgetId/${appIcon.id}")
            putExtra(HosWidgetLaunchActivity.EXTRA_APP_ID, appIcon.id)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, appWidgetId, intent, flags)
    }
}
