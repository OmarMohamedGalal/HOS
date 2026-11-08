package com.example.navigation

object HosDestinations {
    const val ROUTE_ICON_LIBRARY = "icon_library"
    const val ROUTE_CUSTOMIZE_BASE = "customize"
    const val ARG_APP_ID = "appId"
    const val ROUTE_CUSTOMIZE = "$ROUTE_CUSTOMIZE_BASE/{$ARG_APP_ID}"
    const val ROUTE_MY_WIDGETS = "my_widgets"
    const val ROUTE_SETTINGS = "settings"

    fun customizeRoute(appId: String): String = "$ROUTE_CUSTOMIZE_BASE/$appId"
}
