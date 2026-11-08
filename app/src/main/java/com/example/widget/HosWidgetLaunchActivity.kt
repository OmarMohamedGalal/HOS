package com.example.widget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.data.HosWidgetStore
import com.example.system.SystemAppLauncher

/**
 * Zero-UI trampoline activity invoked when a user taps a HyperOS 3 home screen widget.
 * Using an Activity PendingIntent guarantees reliable background launch privileges on Android 10–15.
 */
class HosWidgetLaunchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val widgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val explicitAppId = intent?.getStringExtra(EXTRA_APP_ID)
        val resolvedAppId = when {
            !explicitAppId.isNullOrBlank() -> explicitAppId
            widgetId != AppWidgetManager.INVALID_APPWIDGET_ID -> {
                HosWidgetStore.getRecordForWidgetSync(this, widgetId)?.appId ?: "settings"
            }
            else -> "settings"
        }

        SystemAppLauncher.launchSystemApp(this, resolvedAppId)
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    companion object {
        const val ACTION_LAUNCH_FROM_WIDGET = "com.example.widget.ACTION_LAUNCH_FROM_WIDGET"
        const val EXTRA_APP_ID = "extra_app_id"
    }
}
