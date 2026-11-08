package com.example.ui

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.HosUserSettings
import com.example.data.HosWidgetStore
import com.example.data.HyperOsIconRepository
import com.example.model.ConfiguredWidgetInstance
import com.example.model.HyperOsAppIcon
import com.example.model.WidgetSizeOption
import com.example.system.SystemAppLauncher
import com.example.widget.HosWidgetEngine
import com.example.widget.WidgetPinResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HosViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext: Context
        get() = getApplication<Application>().applicationContext

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Default page in app is 3 (HyperOS 3); tapping 2 switches to HyperOS 2 icons
    private val _selectedOsVersion = MutableStateFlow(3)
    val selectedOsVersion: StateFlow<Int> = _selectedOsVersion.asStateFlow()

    private val _manualPinGuide = MutableStateFlow<Pair<HyperOsAppIcon, WidgetSizeOption>?>(null)
    val manualPinGuide: StateFlow<Pair<HyperOsAppIcon, WidgetSizeOption>?> = _manualPinGuide.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0L)

    val userSettings: StateFlow<HosUserSettings> = HosWidgetStore
        .observeUserSettings(appContext)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HosWidgetStore.getUserSettingsSync(appContext)
        )

    val filteredIcons: StateFlow<List<HyperOsAppIcon>> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedOsVersion
    ) { query, category, osVersion ->
        val trimmed = query.trim()
        HyperOsIconRepository.getIconsForVersion(osVersion).filter { icon ->
            val matchesCategory = category == "All" || icon.category.equals(category, ignoreCase = true)
            val matchesQuery = trimmed.isEmpty() ||
                icon.name.contains(trimmed, ignoreCase = true) ||
                icon.category.contains(trimmed, ignoreCase = true) ||
                icon.id.contains(trimmed, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HyperOsIconRepository.hyperOs3Icons
    )

    val configuredWidgets: StateFlow<List<ConfiguredWidgetInstance>> = combine(
        HosWidgetStore.observeWidgetRecords(appContext),
        _refreshTrigger
    ) { _, _ ->
        HosWidgetEngine.getSynchronizedWidgets(appContext)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HosWidgetEngine.getSynchronizedWidgets(appContext)
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectOsVersion(osVersion: Int) {
        if (osVersion == 2 || osVersion == 3 || osVersion == 4) {
            _selectedOsVersion.value = osVersion
        }
    }

    fun refreshWidgets() {
        viewModelScope.launch {
            HosWidgetEngine.refreshAllActiveWidgets(appContext)
            _refreshTrigger.value = System.currentTimeMillis()
        }
    }

    fun requestAddWidgetToHomeScreen(
        context: Context,
        appIcon: HyperOsAppIcon,
        sizeOption: WidgetSizeOption
    ) {
        val result = HosWidgetEngine.requestPinWidget(context, appIcon, sizeOption)
        when (result) {
            WidgetPinResult.PIN_DIALOG_REQUESTED -> {
                val activeCount = HosWidgetEngine.getActiveLauncherWidgetIds(context).size
                if (activeCount == 0 && HosWidgetStore.getAllRecordsSync(context).none {
                        it.appId == appIcon.id && it.sizeOption == sizeOption
                    }
                ) {
                    HosWidgetStore.saveConfiguredPreset(context, appIcon.id, sizeOption)
                }
                _refreshTrigger.value = System.currentTimeMillis()
                Toast.makeText(
                    context,
                    context.getString(R.string.pin_request_sent, appIcon.name, sizeOption.label),
                    Toast.LENGTH_SHORT
                ).show()
            }
            WidgetPinResult.UNSUPPORTED_LAUNCHER -> {
                HosWidgetStore.saveConfiguredPreset(context, appIcon.id, sizeOption)
                _refreshTrigger.value = System.currentTimeMillis()
                _manualPinGuide.value = appIcon to sizeOption
            }
        }
    }

    fun dismissManualPinGuide() {
        _manualPinGuide.value = null
    }

    fun removeWidgetConfiguration(context: Context, appWidgetId: Int, appName: String) {
        HosWidgetStore.removeWidgetRecord(context, appWidgetId)
        _refreshTrigger.value = System.currentTimeMillis()
        Toast.makeText(
            context,
            context.getString(R.string.widget_removed_toast, appName),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun pruneStaleWidgets(context: Context) {
        HosWidgetEngine.pruneUnboundSystemWidgetIds(context)
        HosWidgetEngine.refreshAllActiveWidgets(context)
        _refreshTrigger.value = System.currentTimeMillis()
        Toast.makeText(
            context,
            context.getString(R.string.widgets_synced_toast),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun setShowLabelOn2x2(context: Context, enabled: Boolean) {
        HosWidgetStore.setShowLabelOn2x2(context, enabled)
        HosWidgetEngine.refreshAllActiveWidgets(context)
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun setDefaultWidgetSize(context: Context, sizeOption: WidgetSizeOption) {
        HosWidgetStore.setDefaultWidgetSize(context, sizeOption)
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun launchApp(context: Context, appIcon: HyperOsAppIcon) {
        SystemAppLauncher.launchSystemApp(context, appIcon)
    }

    fun isPinSupported(context: Context): Boolean {
        return HosWidgetEngine.isPinSupported(context)
    }
}
