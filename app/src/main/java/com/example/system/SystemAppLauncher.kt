package com.example.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import com.example.R
import com.example.data.HyperOsIconRepository
import com.example.model.HyperOsAppIcon

object SystemAppLauncher {

    /**
     * Checks whether the device has a direct package or primary intent handler
     * for the given HyperOS 3 application icon.
     */
    fun isPrimaryAppAvailable(context: Context, appIcon: HyperOsAppIcon): Boolean {
        return resolvePrimaryIntent(context, appIcon) != null
    }

    /**
     * Launches the corresponding default Android system application for the given appId.
     * Returns true if an activity was started successfully.
     */
    fun launchSystemApp(context: Context, appId: String): Boolean {
        val appIcon = HyperOsIconRepository.getIconById(appId)
        return launchSystemApp(context, appIcon)
    }

    fun launchSystemApp(context: Context, appIcon: HyperOsAppIcon): Boolean {
        val intentToLaunch = resolvePrimaryIntent(context, appIcon)
            ?: resolveFallbackIntent(context, appIcon)

        if (intentToLaunch != null) {
            intentToLaunch.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            )
            try {
                context.startActivity(intentToLaunch)
                return true
            } catch (_: Exception) {
                // Continue to final settings fallback
            }
        }

        // Last resort: open top-level Android Settings so tapping a widget never fails silently
        return try {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(settingsIntent)
            Toast.makeText(
                context,
                context.getString(R.string.launch_error_no_app, appIcon.name),
                Toast.LENGTH_SHORT
            ).show()
            false
        } catch (_: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.launch_error_no_app, appIcon.name),
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    private fun resolvePrimaryIntent(context: Context, appIcon: HyperOsAppIcon): Intent? {
        val pm = context.packageManager

        // 1. Check installed candidate Xiaomi HyperOS / Google / Android system packages
        for (pkg in appIcon.candidatePackages) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                return launchIntent
            }
        }

        // 2. Check standard Android category selector (e.g. CATEGORY_APP_CALCULATOR, CATEGORY_APP_GALLERY)
        if (appIcon.intentAction == Intent.ACTION_MAIN && !appIcon.intentCategory.isNullOrBlank()) {
            val selectorIntent = Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN,
                appIcon.intentCategory
            )
            if (selectorIntent.resolveActivity(pm) != null) {
                return selectorIntent
            }
        }

        // 3. Check specific Intent Action (e.g. Settings.ACTION_SETTINGS, ACTION_DIAL, ACTION_SHOW_ALARMS)
        if (!appIcon.intentAction.isNullOrBlank() && appIcon.intentAction != Intent.ACTION_MAIN) {
            val actionIntent = Intent(appIcon.intentAction)
            if (!appIcon.intentDataUri.isNullOrBlank()) {
                actionIntent.data = Uri.parse(appIcon.intentDataUri)
            }
            if (actionIntent.resolveActivity(pm) != null) {
                return actionIntent
            }
        }

        // 4. Special handling for Gallery / Files / Messaging / Browser / Camera / Calendar URIs
        when (appIcon.id) {
            "gallery" -> {
                val galleryView = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                }
                if (galleryView.resolveActivity(pm) != null) return galleryView
            }
            "camera" -> {
                val camIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (camIntent.resolveActivity(pm) != null) return camIntent
            }
            "browser" -> {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                if (webIntent.resolveActivity(pm) != null) return webIntent
            }
            "messages" -> {
                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
                if (smsIntent.resolveActivity(pm) != null) return smsIntent
            }
            "calendar" -> {
                val calIntent = Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.calendar/time"))
                if (calIntent.resolveActivity(pm) != null) return calIntent
            }
        }

        return null
    }

    private fun resolveFallbackIntent(context: Context, appIcon: HyperOsAppIcon): Intent? {
        val pm = context.packageManager
        val fallbackAction = appIcon.fallbackSettingsAction
        if (!fallbackAction.isNullOrBlank()) {
            val intent = Intent(fallbackAction)
            if (intent.resolveActivity(pm) != null) {
                return intent
            }
        }
        return null
    }
}
