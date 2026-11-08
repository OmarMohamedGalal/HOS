package com.example.model

import androidx.annotation.DrawableRes

data class HyperOsAppIcon(
    val id: String,
    val name: String,
    val category: String,
    @DrawableRes val iconResId: Int,
    val bgTopColor: Int,
    val bgBottomColor: Int,
    val bgTopRightColor: Int? = null,
    val bgBottomLeftColor: Int? = null,
    val candidatePackages: List<String>,
    val intentAction: String? = null,
    val intentCategory: String? = null,
    val intentDataUri: String? = null,
    val fallbackSettingsAction: String? = null,
    val osVersion: Int = 3
)
