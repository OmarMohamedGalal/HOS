package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.R
import com.example.data.HosWidgetStore
import com.example.data.HyperOsIconRepository
import com.example.model.WidgetSizeOption

abstract class BaseHosWidgetProvider(
    private val providerSize: WidgetSizeOption
) : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            HosWidgetEngine.updateWidgetInstance(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetId = appWidgetId,
                fallbackSize = providerSize
            )
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        HosWidgetEngine.updateWidgetInstance(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetId = appWidgetId,
            fallbackSize = providerSize
        )
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        HosWidgetStore.removeWidgetRecords(context, appWidgetIds)
    }

    override fun onRestored(
        context: Context,
        oldWidgetIds: IntArray,
        newWidgetIds: IntArray
    ) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        val count = minOf(oldWidgetIds.size, newWidgetIds.size)
        for (i in 0 until count) {
            HosWidgetStore.remapWidgetId(context, oldWidgetIds[i], newWidgetIds[i])
        }
        HosWidgetEngine.refreshAllActiveWidgets(context)
    }
}

/**
 * 1 × 1 Compact HyperOS 3 App Icon Widget Provider
 */
class HosWidget1x1Provider : BaseHosWidgetProvider(WidgetSizeOption.SIZE_1X1)

/**
 * 2 × 2 Square HyperOS 3 App Icon Widget Provider
 */
class HosWidget2x2Provider : BaseHosWidgetProvider(WidgetSizeOption.SIZE_2X2)

/**
 * 2 × 1 Horizontal HyperOS 3 App Icon Widget Provider
 */
class HosWidget2x1Provider : BaseHosWidgetProvider(WidgetSizeOption.SIZE_2X1)

/**
 * 1 × 2 Vertical HyperOS 3 App Icon Widget Provider
 */
class HosWidget1x2Provider : BaseHosWidgetProvider(WidgetSizeOption.SIZE_1X2)

/**
 * Callback receiver triggered when the user confirms pinning a widget to the Home Screen.
 */
class HosWidgetPinReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appIdFromIntent = intent.getStringExtra(HosWidgetEngine.EXTRA_PIN_APP_ID)
        val sizeKeyFromIntent = intent.getStringExtra(HosWidgetEngine.EXTRA_PIN_SIZE_KEY)
        val fallbackSize = WidgetSizeOption.fromKey(sizeKeyFromIntent)

        val pending = HosWidgetStore.consumePendingPinRequest(context, fallbackSize)
        val resolvedAppId = appIdFromIntent
            ?: pending?.first
            ?: HosWidgetStore.getLastSelectedAppId(context)
        val resolvedSize = if (sizeKeyFromIntent != null) fallbackSize else (pending?.second ?: fallbackSize)

        val pinnedWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        val appWidgetManager = AppWidgetManager.getInstance(context)

        if (pinnedWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            // Remove any temporary negative preset for the same app & size now that a real launcher ID is bound
            val tempPreset = HosWidgetStore.getAllRecordsSync(context).firstOrNull {
                it.appWidgetId < 0 && it.appId == resolvedAppId && it.sizeOption == resolvedSize
            }
            if (tempPreset != null) {
                HosWidgetStore.removeWidgetRecord(context, tempPreset.appWidgetId)
            }

            HosWidgetStore.saveWidgetRecord(
                context = context,
                appWidgetId = pinnedWidgetId,
                appId = resolvedAppId,
                sizeOption = resolvedSize
            )
            HosWidgetEngine.updateWidgetInstance(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetId = pinnedWidgetId,
                fallbackSize = resolvedSize
            )
        } else {
            // If the launcher did not populate EXTRA_APPWIDGET_ID in the broadcast, bind the newest active ID
            val providerIds = try {
                appWidgetManager.getAppWidgetIds(
                    HosWidgetEngine.providerComponentForSize(context, resolvedSize)
                )
            } catch (_: Exception) {
                IntArray(0)
            }
            val latestId = providerIds.maxOrNull()
            if (latestId != null) {
                HosWidgetStore.saveWidgetRecord(
                    context = context,
                    appWidgetId = latestId,
                    appId = resolvedAppId,
                    sizeOption = resolvedSize
                )
            }
            HosWidgetEngine.refreshAllActiveWidgets(context)
        }

        val appIcon = HyperOsIconRepository.getIconById(resolvedAppId)
        Toast.makeText(
            context,
            context.getString(R.string.pin_confirmed_toast, appIcon.name, resolvedSize.label),
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        const val ACTION_WIDGET_PINNED = "com.example.widget.ACTION_WIDGET_PINNED"
    }
}

/**
 * Restores and re-renders all configured HyperOS 3 home screen widgets across device reboots
 * and application package updates.
 */
class HosBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            HosWidgetEngine.refreshAllActiveWidgets(context)
        }
    }
}
