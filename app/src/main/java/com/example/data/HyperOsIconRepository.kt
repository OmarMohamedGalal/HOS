package com.example.data

import android.content.Intent
import android.provider.MediaStore
import android.provider.Settings
import com.example.R
import com.example.model.HyperOsAppIcon

object HyperOsIconRepository {

    /**
     * HyperOS 3 system application icons (default page in app):
     * Settings, Camera, Calculator, Files, Themes, Gallery, Notes, Security, Recorder.
     */
    val hyperOs3Icons: List<HyperOsAppIcon> = listOf(
        HyperOsAppIcon(
            id = "settings",
            name = "Settings",
            category = "System",
            iconResId = R.drawable.ic_hos_settings,
            bgTopColor = 0xFFEFF6FE.toInt(),
            bgBottomColor = 0xFFAEB5BD.toInt(),
            candidatePackages = listOf(
                "com.android.settings",
                "com.miui.securitysetting"
            ),
            intentAction = Settings.ACTION_SETTINGS,
            fallbackSettingsAction = Settings.ACTION_SETTINGS,
            osVersion = 3
        ),
        HyperOsAppIcon(
            id = "camera",
            name = "Camera",
            category = "Media",
            iconResId = R.drawable.ic_hos_camera,
            bgTopColor = 0xFFEFF6FF.toInt(),
            bgBottomColor = 0xFFAEB5BD.toInt(),
            candidatePackages = listOf(
                "com.android.camera",
                "com.android.camera2",
                "com.google.android.GoogleCamera",
                "org.codeaurora.snapcam"
            ),
            intentAction = MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA,
            fallbackSettingsAction = Settings.ACTION_APPLICATION_SETTINGS,
            osVersion = 3
        ),
        HyperOsAppIcon(
            id = "calculator",
            name = "Calculator",
            category = "Tools",
            iconResId = R.drawable.ic_hos_calculator,
            bgTopColor = 0xFFFFFFFF.toInt(),
            bgBottomColor = 0xFFFFFFFF.toInt(),
            candidatePackages = listOf(
                "com.miui.calculator",
                "com.google.android.calculator",
                "com.android.calculator2",
                "com.sec.android.app.popupcalculator"
            ),
            intentAction = Intent.ACTION_MAIN,
            intentCategory = Intent.CATEGORY_APP_CALCULATOR,
            fallbackSettingsAction = Settings.ACTION_APPLICATION_SETTINGS,
            osVersion = 3
        ),
        HyperOsAppIcon(
            id = "files",
            name = "Files",
            category = "Tools",
            iconResId = R.drawable.ic_hos_files,
            bgTopColor = 0xFFFFFFFF.toInt(),
            bgBottomColor = 0xFFFFFFFF.toInt(),
            candidatePackages = listOf(
                "com.mi.android.globalFileexplorer",
                "com.android.fileexplorer",
                "com.google.android.apps.nbu.files",
                "com.android.documentsui",
                "com.google.android.documentsui"
            ),
            intentAction = Intent.ACTION_MAIN,
            intentCategory = Intent.CATEGORY_APP_FILES,
            fallbackSettingsAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
            osVersion = 3
        ),
        HyperOsAppIcon(
            id = "themes",
            name = "Themes",
            category = "System",
            iconResId = R.drawable.ic_hos_themes,
            bgTopColor = 0xFFDC4FA1.toInt(),
            bgBottomColor = 0xFFFD951E.toInt(),
            bgTopRightColor = 0xFF9064F2.toInt(),
            bgBottomLeftColor = 0xFFFE5150.toInt(),
            candidatePackages = listOf(
                "com.android.thememanager",
                "com.google.android.apps.wallpaper"
            ),
            intentAction = Intent.ACTION_SET_WALLPAPER,
            fallbackSettingsAction = Settings.ACTION_DISPLAY_SETTINGS,
            osVersion = 3
        ),
        HyperOsAppIcon(
            id = "gallery",
            name = "Gallery",
            category = "Media",
            iconResId = R.drawable.ic_hos_gallery,
            bgTopColor = 0xFFFFFFFF.toInt(),
            bgBottomColor = 0xFFFFFFFF.toInt(),
            candidatePackages = listOf(
                "com.miui.gallery",
                "com.google.android.apps.photos",
                "com.android.gallery3d",
                "com.sec.android.gallery3d"
            ),
            intentAction = Intent.ACTION_MAIN,
            intentCategory = Intent.CATEGORY_APP_GALLERY,
            fallbackSettingsAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
            osVersion = 3
        ),
        HyperOsAppIcon(
            id = "notes",
            name = "Notes",
            category = "Tools",
            iconResId = R.drawable.ic_hos_notes,
            bgTopColor = 0xFFFFD230.toInt(),
            bgBottomColor = 0xFFFFA41A.toInt(),
            candidatePackages = listOf(
                "com.miui.notes",
                "com.google.android.keep",
                "com.samsung.android.app.notes"
            ),
            intentAction = Intent.ACTION_MAIN,
            fallbackSettingsAction = Settings.ACTION_APPLICATION_SETTINGS,
            osVersion = 3
        ),
        HyperOsAppIcon(
            id = "security",
            name = "Security",
            category = "System",
            iconResId = R.drawable.ic_hos_security,
            bgTopColor = 0xFF37E47A.toInt(),
            bgBottomColor = 0xFF01BA51.toInt(),
            candidatePackages = listOf(
                "com.miui.securitycenter",
                "com.google.android.gms.supervision"
            ),
            intentAction = Settings.ACTION_SECURITY_SETTINGS,
            fallbackSettingsAction = Settings.ACTION_SECURITY_SETTINGS,
            osVersion = 3
        ),
        HyperOsAppIcon(
            id = "recorder",
            name = "Recorder",
            category = "Media",
            iconResId = R.drawable.ic_hos_recorder,
            bgTopColor = 0xFF292929.toInt(),
            bgBottomColor = 0xFF151515.toInt(),
            candidatePackages = listOf(
                "com.android.soundrecorder",
                "com.google.android.apps.recorder"
            ),
            intentAction = MediaStore.Audio.Media.RECORD_SOUND_ACTION,
            fallbackSettingsAction = Settings.ACTION_SOUND_SETTINGS,
            osVersion = 3
        )
    )

    /**
     * HyperOS 2 system application icons (category 2 in the bottom slider):
     * Launches the exact same system apps as HyperOS 3 but with the HyperOS 2 icon style.
     */
    val hyperOs2Icons: List<HyperOsAppIcon> = listOf(
        HyperOsAppIcon(
            id = "hos2_settings",
            name = "Settings",
            category = "System",
            iconResId = R.drawable.ic_hos2_settings,
            bgTopColor = 0xFFA2B0C0.toInt(),
            bgBottomColor = 0xFF708090.toInt(),
            bgTopRightColor = 0xFF919FAF.toInt(),
            bgBottomLeftColor = 0xFF8190A1.toInt(),
            candidatePackages = listOf(
                "com.android.settings",
                "com.miui.securitysetting"
            ),
            intentAction = Settings.ACTION_SETTINGS,
            fallbackSettingsAction = Settings.ACTION_SETTINGS,
            osVersion = 2
        ),
        HyperOsAppIcon(
            id = "hos2_camera",
            name = "Camera",
            category = "Media",
            iconResId = R.drawable.ic_hos2_camera,
            bgTopColor = 0xFFFAFBFD.toInt(),
            bgBottomColor = 0xFFC6D3E1.toInt(),
            bgTopRightColor = 0xFFE9EDF3.toInt(),
            bgBottomLeftColor = 0xFFD8E1EB.toInt(),
            candidatePackages = listOf(
                "com.android.camera",
                "com.android.camera2",
                "com.google.android.GoogleCamera",
                "org.codeaurora.snapcam"
            ),
            intentAction = MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA,
            fallbackSettingsAction = Settings.ACTION_APPLICATION_SETTINGS,
            osVersion = 2
        ),
        HyperOsAppIcon(
            id = "hos2_calculator",
            name = "Calculator",
            category = "Tools",
            iconResId = R.drawable.ic_hos2_calculator,
            bgTopColor = 0xFFDEDDDD.toInt(),
            bgBottomColor = 0xFFDEDEDE.toInt(),
            candidatePackages = listOf(
                "com.miui.calculator",
                "com.google.android.calculator",
                "com.android.calculator2",
                "com.sec.android.app.popupcalculator"
            ),
            intentAction = Intent.ACTION_MAIN,
            intentCategory = Intent.CATEGORY_APP_CALCULATOR,
            fallbackSettingsAction = Settings.ACTION_APPLICATION_SETTINGS,
            osVersion = 2
        ),
        HyperOsAppIcon(
            id = "hos2_files",
            name = "Files",
            category = "Tools",
            iconResId = R.drawable.ic_hos2_files,
            bgTopColor = 0xFFFFFFFF.toInt(),
            bgBottomColor = 0xFFDAE4EB.toInt(),
            bgTopRightColor = 0xFFF2F6F8.toInt(),
            bgBottomLeftColor = 0xFFE6EDF1.toInt(),
            candidatePackages = listOf(
                "com.mi.android.globalFileexplorer",
                "com.android.fileexplorer",
                "com.google.android.apps.nbu.files",
                "com.android.documentsui",
                "com.google.android.documentsui"
            ),
            intentAction = Intent.ACTION_MAIN,
            intentCategory = Intent.CATEGORY_APP_FILES,
            fallbackSettingsAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
            osVersion = 2
        ),
        HyperOsAppIcon(
            id = "hos2_themes",
            name = "Themes",
            category = "System",
            iconResId = R.drawable.ic_hos2_themes,
            bgTopColor = 0xFFFFA341.toInt(),
            bgBottomColor = 0xFFCB51DD.toInt(),
            bgTopRightColor = 0xFFFF7841.toInt(),
            bgBottomLeftColor = 0xFF8055FC.toInt(),
            candidatePackages = listOf(
                "com.android.thememanager",
                "com.google.android.apps.wallpaper"
            ),
            intentAction = Intent.ACTION_SET_WALLPAPER,
            fallbackSettingsAction = Settings.ACTION_DISPLAY_SETTINGS,
            osVersion = 2
        ),
        HyperOsAppIcon(
            id = "hos2_gallery",
            name = "Gallery",
            category = "Media",
            iconResId = R.drawable.ic_hos2_gallery,
            bgTopColor = 0xFF617DFF.toInt(),
            bgBottomColor = 0xFFBEBFFF.toInt(),
            bgTopRightColor = 0xFF7882FF.toInt(),
            bgBottomLeftColor = 0xFFBEBFFF.toInt(),
            candidatePackages = listOf(
                "com.miui.gallery",
                "com.google.android.apps.photos",
                "com.android.gallery3d",
                "com.sec.android.gallery3d"
            ),
            intentAction = Intent.ACTION_MAIN,
            intentCategory = Intent.CATEGORY_APP_GALLERY,
            fallbackSettingsAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
            osVersion = 2
        ),
        HyperOsAppIcon(
            id = "hos2_notes",
            name = "Notes",
            category = "Tools",
            iconResId = R.drawable.ic_hos2_notes,
            bgTopColor = 0xFFFCDA46.toInt(),
            bgBottomColor = 0xFFFFA028.toInt(),
            bgTopRightColor = 0xFFFDC63B.toInt(),
            bgBottomLeftColor = 0xFFFEB432.toInt(),
            candidatePackages = listOf(
                "com.miui.notes",
                "com.google.android.keep",
                "com.samsung.android.app.notes"
            ),
            intentAction = Intent.ACTION_MAIN,
            fallbackSettingsAction = Settings.ACTION_APPLICATION_SETTINGS,
            osVersion = 2
        ),
        HyperOsAppIcon(
            id = "hos2_security",
            name = "Security",
            category = "System",
            iconResId = R.drawable.ic_hos2_security,
            bgTopColor = 0xFF52E97C.toInt(),
            bgBottomColor = 0xFF28BB58.toInt(),
            bgTopRightColor = 0xFF43D970.toInt(),
            bgBottomLeftColor = 0xFF36CB65.toInt(),
            candidatePackages = listOf(
                "com.miui.securitycenter",
                "com.google.android.gms.supervision"
            ),
            intentAction = Settings.ACTION_SECURITY_SETTINGS,
            fallbackSettingsAction = Settings.ACTION_SECURITY_SETTINGS,
            osVersion = 2
        ),
        HyperOsAppIcon(
            id = "hos2_recorder",
            name = "Recorder",
            category = "Media",
            iconResId = R.drawable.ic_hos2_recorder,
            bgTopColor = 0xFF2E2E2E.toInt(),
            bgBottomColor = 0xFF151515.toInt(),
            bgTopRightColor = 0xFF252525.toInt(),
            bgBottomLeftColor = 0xFF1D1D1D.toInt(),
            candidatePackages = listOf(
                "com.android.soundrecorder",
                "com.google.android.apps.recorder"
            ),
            intentAction = MediaStore.Audio.Media.RECORD_SOUND_ACTION,
            fallbackSettingsAction = Settings.ACTION_SOUND_SETTINGS,
            osVersion = 2
        )
    )

    /**
     * HyperOS 4 system application icons (category 4 in the bottom slider):
     * Launches the exact same system apps as HyperOS 3 & 2 with the HyperOS 4 icon style.
     */
    val hyperOs4Icons: List<HyperOsAppIcon> = listOf(
        HyperOsAppIcon(
            id = "hos4_settings",
            name = "Settings",
            category = "System",
            iconResId = R.drawable.ic_hos4_settings,
            bgTopColor = 0xFFF0F7FF.toInt(),
            bgBottomColor = 0xFFAEB5BD.toInt(),
            candidatePackages = listOf(
                "com.android.settings",
                "com.miui.securitysetting"
            ),
            intentAction = Settings.ACTION_SETTINGS,
            fallbackSettingsAction = Settings.ACTION_SETTINGS,
            osVersion = 4
        ),
        HyperOsAppIcon(
            id = "hos4_camera",
            name = "Camera",
            category = "Media",
            iconResId = R.drawable.ic_hos4_camera,
            bgTopColor = 0xFFF0F7FF.toInt(),
            bgBottomColor = 0xFFAEB5BD.toInt(),
            candidatePackages = listOf(
                "com.android.camera",
                "com.android.camera2",
                "com.google.android.GoogleCamera",
                "org.codeaurora.snapcam"
            ),
            intentAction = MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA,
            fallbackSettingsAction = Settings.ACTION_APPLICATION_SETTINGS,
            osVersion = 4
        ),
        HyperOsAppIcon(
            id = "hos4_calculator",
            name = "Calculator",
            category = "Tools",
            iconResId = R.drawable.ic_hos4_calculator,
            bgTopColor = 0xFFFFFFFF.toInt(),
            bgBottomColor = 0xFFE9E9E9.toInt(),
            candidatePackages = listOf(
                "com.miui.calculator",
                "com.google.android.calculator",
                "com.android.calculator2",
                "com.sec.android.app.popupcalculator"
            ),
            intentAction = Intent.ACTION_MAIN,
            intentCategory = Intent.CATEGORY_APP_CALCULATOR,
            fallbackSettingsAction = Settings.ACTION_APPLICATION_SETTINGS,
            osVersion = 4
        ),
        HyperOsAppIcon(
            id = "hos4_files",
            name = "Files",
            category = "Tools",
            iconResId = R.drawable.ic_hos4_files,
            bgTopColor = 0xFFFFFFFF.toInt(),
            bgBottomColor = 0xFFE9E9E9.toInt(),
            candidatePackages = listOf(
                "com.mi.android.globalFileexplorer",
                "com.android.fileexplorer",
                "com.google.android.apps.nbu.files",
                "com.android.documentsui",
                "com.google.android.documentsui"
            ),
            intentAction = Intent.ACTION_MAIN,
            intentCategory = Intent.CATEGORY_APP_FILES,
            fallbackSettingsAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
            osVersion = 4
        ),
        HyperOsAppIcon(
            id = "hos4_themes",
            name = "Themes",
            category = "System",
            iconResId = R.drawable.ic_hos4_themes,
            bgTopColor = 0xFFDC4FA2.toInt(),
            bgBottomColor = 0xFFFD961B.toInt(),
            bgTopRightColor = 0xFF8C66F7.toInt(),
            bgBottomLeftColor = 0xFFFF504D.toInt(),
            candidatePackages = listOf(
                "com.android.thememanager",
                "com.google.android.apps.wallpaper"
            ),
            intentAction = Intent.ACTION_SET_WALLPAPER,
            fallbackSettingsAction = Settings.ACTION_DISPLAY_SETTINGS,
            osVersion = 4
        ),
        HyperOsAppIcon(
            id = "hos4_gallery",
            name = "Gallery",
            category = "Media",
            iconResId = R.drawable.ic_hos4_gallery,
            bgTopColor = 0xFFFFFFFF.toInt(),
            bgBottomColor = 0xFFE9E9E9.toInt(),
            candidatePackages = listOf(
                "com.miui.gallery",
                "com.google.android.apps.photos",
                "com.android.gallery3d",
                "com.sec.android.gallery3d"
            ),
            intentAction = Intent.ACTION_MAIN,
            intentCategory = Intent.CATEGORY_APP_GALLERY,
            fallbackSettingsAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
            osVersion = 4
        ),
        HyperOsAppIcon(
            id = "hos4_notes",
            name = "Notes",
            category = "Tools",
            iconResId = R.drawable.ic_hos4_notes,
            bgTopColor = 0xFFFFD230.toInt(),
            bgBottomColor = 0xFFFFA41B.toInt(),
            candidatePackages = listOf(
                "com.miui.notes",
                "com.google.android.keep",
                "com.samsung.android.app.notes"
            ),
            intentAction = Intent.ACTION_MAIN,
            fallbackSettingsAction = Settings.ACTION_APPLICATION_SETTINGS,
            osVersion = 4
        ),
        HyperOsAppIcon(
            id = "hos4_security",
            name = "Security",
            category = "System",
            iconResId = R.drawable.ic_hos4_security,
            bgTopColor = 0xFF39E57B.toInt(),
            bgBottomColor = 0xFF00BA51.toInt(),
            bgTopRightColor = 0xFF36E379.toInt(),
            bgBottomLeftColor = 0xFF04BE54.toInt(),
            candidatePackages = listOf(
                "com.miui.securitycenter",
                "com.google.android.gms.supervision"
            ),
            intentAction = Settings.ACTION_SECURITY_SETTINGS,
            fallbackSettingsAction = Settings.ACTION_SECURITY_SETTINGS,
            osVersion = 4
        ),
        HyperOsAppIcon(
            id = "hos4_recorder",
            name = "Recorder",
            category = "Media",
            iconResId = R.drawable.ic_hos4_recorder,
            bgTopColor = 0xFF292929.toInt(),
            bgBottomColor = 0xFF151515.toInt(),
            candidatePackages = listOf(
                "com.android.soundrecorder",
                "com.google.android.apps.recorder"
            ),
            intentAction = MediaStore.Audio.Media.RECORD_SOUND_ACTION,
            fallbackSettingsAction = Settings.ACTION_SOUND_SETTINGS,
            osVersion = 4
        )
    )

    val allIcons: List<HyperOsAppIcon> = hyperOs3Icons + hyperOs2Icons + hyperOs4Icons

    val categories: List<String> = listOf("All", "System", "Tools", "Media")

    fun getIconsForVersion(osVersion: Int): List<HyperOsAppIcon> {
        return when (osVersion) {
            2 -> hyperOs2Icons
            4 -> hyperOs4Icons
            else -> hyperOs3Icons
        }
    }

    fun getIconById(appId: String?): HyperOsAppIcon {
        if (appId.isNullOrBlank()) return hyperOs3Icons.first()
        return allIcons.firstOrNull { it.id.equals(appId, ignoreCase = true) }
            ?: allIcons.firstOrNull { it.name.equals(appId, ignoreCase = true) }
            ?: hyperOs3Icons.first()
    }
}
