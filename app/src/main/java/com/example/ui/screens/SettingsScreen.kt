package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.HosUserSettings
import com.example.model.WidgetSizeOption
import com.example.ui.theme.HosBlue
import com.example.ui.theme.HosMutedLabel
import com.example.ui.theme.HosOnSurface
import com.example.ui.theme.HosPrimary
import com.example.ui.theme.HosPureWhite
import com.example.ui.theme.HosSecondaryText
import com.example.ui.theme.HosSelectableTileSurface
import com.example.ui.theme.HosSelectorTileBase
import com.example.ui.theme.HosSubtleBorder
import com.example.ui.theme.HosSurface
import com.example.ui.theme.HosSwitchTrackInactive

@Composable
fun SettingsScreen(
    userSettings: HosUserSettings,
    isPinSupported: Boolean,
    onBack: () -> Unit,
    onToggleShowLabelOn2x2: (Boolean) -> Unit,
    onDefaultSizeSelected: (WidgetSizeOption) -> Unit,
    onRefreshAllWidgets: () -> Unit,
    onCleanUpStaleWidgets: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HosSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = HosOnSurface
                    )
                }
                Text(
                    text = stringResource(R.string.nav_settings),
                    style = MaterialTheme.typography.headlineSmall,
                    color = HosPureWhite,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_section_widget_display),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = HosMutedLabel,
                    modifier = Modifier.padding(start = 4.dp)
                )

                // Show Label on 2x2 Widgets Switch Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(HosSelectableTileSurface)
                        .border(1.dp, HosSubtleBorder, RoundedCornerShape(24.dp))
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_show_2x2_label_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = HosPureWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.settings_show_2x2_label_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = HosSecondaryText
                        )
                    }

                    Switch(
                        checked = userSettings.showLabelOn2x2,
                        onCheckedChange = onToggleShowLabelOn2x2,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = HosPureWhite,
                            checkedTrackColor = HosBlue,
                            uncheckedThumbColor = HosPureWhite,
                            uncheckedTrackColor = HosSwitchTrackInactive,
                            uncheckedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("settings_show_2x2_label_switch")
                    )
                }

                // Default Widget Size Selector Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(HosSelectableTileSurface)
                        .border(1.dp, HosSubtleBorder, RoundedCornerShape(24.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_default_size_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = HosPureWhite
                    )
                    Text(
                        text = stringResource(R.string.settings_default_size_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = HosSecondaryText
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WidgetSizeOption.entries.forEach { option ->
                            val isSelected = option == userSettings.defaultSize
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) HosBlue else HosSelectorTileBase)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) HosBlue else HosSubtleBorder,
                                        shape = CircleShape
                                    )
                                    .clickable { onDefaultSizeSelected(option) }
                                    .testTag("settings_default_size_${option.key}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) HosPureWhite else HosMutedLabel
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.settings_section_system),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = HosMutedLabel,
                    modifier = Modifier.padding(start = 4.dp)
                )

                // Direct Widget Pinning Status Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(HosSelectableTileSurface)
                        .border(1.dp, HosSubtleBorder, RoundedCornerShape(24.dp))
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(HosSelectorTileBase),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = null,
                            tint = HosPrimary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_pin_support_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = HosPureWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPinSupported) {
                                stringResource(R.string.settings_pin_supported)
                            } else {
                                stringResource(R.string.settings_pin_unsupported)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPinSupported) Color(0xFF2EE57D) else HosSecondaryText
                        )
                    }
                }

                // Refresh All Home Screen Widgets Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(HosSelectableTileSurface)
                        .border(1.dp, HosSubtleBorder, RoundedCornerShape(24.dp))
                        .clickable(onClick = onRefreshAllWidgets)
                        .padding(18.dp)
                        .testTag("settings_refresh_widgets_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(HosSelectorTileBase),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null,
                            tint = HosBlue
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_refresh_widgets_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = HosPureWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.settings_refresh_widgets_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = HosSecondaryText
                        )
                    }
                }

                // Clean Up Stale Configurations Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(HosSelectableTileSurface)
                        .border(1.dp, HosSubtleBorder, RoundedCornerShape(24.dp))
                        .clickable(onClick = onCleanUpStaleWidgets)
                        .padding(18.dp)
                        .testTag("settings_clean_stale_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(HosSelectorTileBase),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CleaningServices,
                            contentDescription = null,
                            tint = HosPrimary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_clear_stale_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = HosPureWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.settings_clear_stale_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = HosSecondaryText
                        )
                    }
                }

                // About Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(HosSelectorTileBase)
                        .border(1.dp, HosSubtleBorder, RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_about_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = HosPureWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_about_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = HosMutedLabel
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
