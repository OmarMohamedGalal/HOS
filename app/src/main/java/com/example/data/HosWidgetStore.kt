package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.HyperOsAppIcon
import com.example.model.WidgetSizeOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

val Context.hosDataStore: DataStore<Preferences> by preferencesDataStore(name = "hos_widget_preferences")

data class StoredWidgetRecord(
    val appWidgetId: Int,
    val appId: String,
    val sizeKey: String,
    val createdAtMillis: Long
) {
    val appIcon: HyperOsAppIcon
        get() = HyperOsIconRepository.getIconById(appId)

    val sizeOption: WidgetSizeOption
        get() = WidgetSizeOption.fromKey(sizeKey)
}

data class HosUserSettings(
    val showLabelOn2x2: Boolean = false,
    val defaultSize: WidgetSizeOption = WidgetSizeOption.SIZE_2X2
)

object HosWidgetStore {

    private const val SYNC_PREFS_NAME = "hos_widget_sync_cache"
    private const val KEY_RECORDS_JSON = "widget_records_json"
    private const val KEY_SHOW_LABEL_2X2 = "show_label_2x2"
    private const val KEY_DEFAULT_SIZE = "default_size_key"
    private const val KEY_PENDING_APP_ID = "pending_pin_app_id"
    private const val KEY_PENDING_SIZE_KEY = "pending_pin_size_key"
    private const val KEY_PENDING_TIMESTAMP = "pending_pin_timestamp"
    private const val KEY_LAST_SELECTED_APP_ID = "last_selected_app_id"
    private const val KEY_NEXT_VIRTUAL_ID = "next_virtual_widget_id"

    private val dsRecordsKey = stringPreferencesKey(KEY_RECORDS_JSON)
    private val dsShowLabel2x2Key = booleanPreferencesKey(KEY_SHOW_LABEL_2X2)
    private val dsDefaultSizeKey = stringPreferencesKey(KEY_DEFAULT_SIZE)

    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun syncPrefs(context: Context) =
        context.applicationContext.getSharedPreferences(SYNC_PREFS_NAME, Context.MODE_PRIVATE)

    fun observeWidgetRecords(context: Context): Flow<List<StoredWidgetRecord>> {
        val appContext = context.applicationContext
        return appContext.hosDataStore.data.map { prefs ->
            val json = prefs[dsRecordsKey] ?: syncPrefs(appContext).getString(KEY_RECORDS_JSON, "[]") ?: "[]"
            parseRecordsJson(json)
        }
    }

    fun observeUserSettings(context: Context): Flow<HosUserSettings> {
        val appContext = context.applicationContext
        return appContext.hosDataStore.data.map { prefs ->
            val showLabel = prefs[dsShowLabel2x2Key]
                ?: syncPrefs(appContext).getBoolean(KEY_SHOW_LABEL_2X2, false)
            val defaultSizeKey = prefs[dsDefaultSizeKey]
                ?: syncPrefs(appContext).getString(KEY_DEFAULT_SIZE, WidgetSizeOption.SIZE_2X2.key)
            HosUserSettings(
                showLabelOn2x2 = showLabel,
                defaultSize = WidgetSizeOption.fromKey(defaultSizeKey)
            )
        }
    }

    fun getUserSettingsSync(context: Context): HosUserSettings {
        val sp = syncPrefs(context)
        return HosUserSettings(
            showLabelOn2x2 = sp.getBoolean(KEY_SHOW_LABEL_2X2, false),
            defaultSize = WidgetSizeOption.fromKey(
                sp.getString(KEY_DEFAULT_SIZE, WidgetSizeOption.SIZE_2X2.key)
            )
        )
    }

    fun setShowLabelOn2x2(context: Context, enabled: Boolean) {
        val appContext = context.applicationContext
        syncPrefs(appContext).edit().putBoolean(KEY_SHOW_LABEL_2X2, enabled).apply()
        storeScope.launch {
            appContext.hosDataStore.edit { prefs ->
                prefs[dsShowLabel2x2Key] = enabled
            }
        }
    }

    fun setDefaultWidgetSize(context: Context, sizeOption: WidgetSizeOption) {
        val appContext = context.applicationContext
        syncPrefs(appContext).edit().putString(KEY_DEFAULT_SIZE, sizeOption.key).apply()
        storeScope.launch {
            appContext.hosDataStore.edit { prefs ->
                prefs[dsDefaultSizeKey] = sizeOption.key
            }
        }
    }

    fun setLastSelectedAppId(context: Context, appId: String) {
        syncPrefs(context).edit().putString(KEY_LAST_SELECTED_APP_ID, appId).apply()
    }

    fun getLastSelectedAppId(context: Context): String {
        return syncPrefs(context).getString(KEY_LAST_SELECTED_APP_ID, "settings") ?: "settings"
    }

    fun setPendingPinRequest(context: Context, appId: String, sizeOption: WidgetSizeOption) {
        syncPrefs(context).edit()
            .putString(KEY_LAST_SELECTED_APP_ID, appId)
            .putString(KEY_PENDING_APP_ID, appId)
            .putString(KEY_PENDING_SIZE_KEY, sizeOption.key)
            .putLong(KEY_PENDING_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun consumePendingPinRequest(
        context: Context,
        fallbackSize: WidgetSizeOption
    ): Pair<String, WidgetSizeOption>? {
        val sp = syncPrefs(context)
        val appId = sp.getString(KEY_PENDING_APP_ID, null) ?: return null
        val sizeKey = sp.getString(KEY_PENDING_SIZE_KEY, fallbackSize.key)
        val timestamp = sp.getLong(KEY_PENDING_TIMESTAMP, 0L)
        if (System.currentTimeMillis() - timestamp > 300_000L) {
            clearPendingPinRequest(context)
            return null
        }
        clearPendingPinRequest(context)
        return appId to WidgetSizeOption.fromKey(sizeKey)
    }

    fun clearPendingPinRequest(context: Context) {
        syncPrefs(context).edit()
            .remove(KEY_PENDING_APP_ID)
            .remove(KEY_PENDING_SIZE_KEY)
            .remove(KEY_PENDING_TIMESTAMP)
            .apply()
    }

    @Synchronized
    fun getAllRecordsSync(context: Context): List<StoredWidgetRecord> {
        val json = syncPrefs(context).getString(KEY_RECORDS_JSON, "[]") ?: "[]"
        return parseRecordsJson(json)
    }

    @Synchronized
    fun getRecordForWidgetSync(context: Context, appWidgetId: Int): StoredWidgetRecord? {
        return getAllRecordsSync(context).firstOrNull { it.appWidgetId == appWidgetId }
    }

    @Synchronized
    fun saveWidgetRecord(
        context: Context,
        appWidgetId: Int,
        appId: String,
        sizeOption: WidgetSizeOption
    ): StoredWidgetRecord {
        val existing = getAllRecordsSync(context).toMutableList()
        val existingIdx = existing.indexOfFirst { it.appWidgetId == appWidgetId }
        val record = StoredWidgetRecord(
            appWidgetId = appWidgetId,
            appId = appId,
            sizeKey = sizeOption.key,
            createdAtMillis = if (existingIdx >= 0) existing[existingIdx].createdAtMillis else System.currentTimeMillis()
        )
        if (existingIdx >= 0) {
            existing[existingIdx] = record
        } else {
            existing.add(0, record)
        }
        persistRecords(context, existing)
        return record
    }

    @Synchronized
    fun saveConfiguredPreset(
        context: Context,
        appId: String,
        sizeOption: WidgetSizeOption
    ): StoredWidgetRecord {
        val sp = syncPrefs(context)
        val nextId = sp.getInt(KEY_NEXT_VIRTUAL_ID, -100) - 1
        sp.edit().putInt(KEY_NEXT_VIRTUAL_ID, nextId).apply()
        return saveWidgetRecord(context, nextId, appId, sizeOption)
    }

    @Synchronized
    fun removeWidgetRecord(context: Context, appWidgetId: Int) {
        val updated = getAllRecordsSync(context).filterNot { it.appWidgetId == appWidgetId }
        persistRecords(context, updated)
    }

    @Synchronized
    fun removeWidgetRecords(context: Context, appWidgetIds: IntArray) {
        val idSet = appWidgetIds.toSet()
        val updated = getAllRecordsSync(context).filterNot { it.appWidgetId in idSet }
        persistRecords(context, updated)
    }

    @Synchronized
    fun remapWidgetId(context: Context, oldWidgetId: Int, newWidgetId: Int) {
        val current = getAllRecordsSync(context).toMutableList()
        val idx = current.indexOfFirst { it.appWidgetId == oldWidgetId }
        if (idx >= 0) {
            val old = current[idx]
            current[idx] = old.copy(appWidgetId = newWidgetId)
            persistRecords(context, current)
        }
    }

    private fun persistRecords(context: Context, records: List<StoredWidgetRecord>) {
        val appContext = context.applicationContext
        val json = serializeRecordsJson(records)
        syncPrefs(appContext).edit().putString(KEY_RECORDS_JSON, json).apply()
        storeScope.launch {
            appContext.hosDataStore.edit { prefs ->
                prefs[dsRecordsKey] = json
            }
        }
    }

    private fun parseRecordsJson(json: String): List<StoredWidgetRecord> {
        return try {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    add(
                        StoredWidgetRecord(
                            appWidgetId = obj.optInt("appWidgetId", 0),
                            appId = obj.optString("appId", "settings"),
                            sizeKey = obj.optString("sizeKey", WidgetSizeOption.SIZE_2X2.key),
                            createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeRecordsJson(records: List<StoredWidgetRecord>): String {
        val array = JSONArray()
        for (record in records) {
            val obj = JSONObject().apply {
                put("appWidgetId", record.appWidgetId)
                put("appId", record.appId)
                put("sizeKey", record.sizeKey)
                put("createdAtMillis", record.createdAtMillis)
            }
            array.put(obj)
        }
        return array.toString()
    }
}
