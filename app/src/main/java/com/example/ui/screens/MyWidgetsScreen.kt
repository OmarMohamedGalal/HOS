package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ConfiguredWidgetInstance
import com.example.model.HyperOsAppIcon
import com.example.model.WidgetSizeOption
import com.example.ui.theme.HosBlue
import com.example.ui.theme.HosError
import com.example.ui.theme.HosMutedLabel
import com.example.ui.theme.HosOnSurface
import com.example.ui.theme.HosPrimary
import com.example.ui.theme.HosPureWhite
import com.example.ui.theme.HosSecondaryText
import com.example.ui.theme.HosSelectableTileSurface
import com.example.ui.theme.HosSelectorTileBase
import com.example.ui.theme.HosSubtleBorder
import com.example.ui.theme.HosSurface
import com.example.ui.theme.HosTangeloOrange
import com.example.widget.HosWidgetBitmapRenderer

@Composable
fun MyWidgetsScreen(
    widgets: List<ConfiguredWidgetInstance>,
    onBack: () -> Unit,
    onSyncWidgets: () -> Unit,
    onOpenCustomize: (HyperOsAppIcon) -> Unit,
    onLaunchApp: (HyperOsAppIcon) -> Unit,
    onRemoveWidget: (ConfiguredWidgetInstance) -> Unit,
    onBrowseIcons: () -> Unit
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("my_widgets_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = HosOnSurface
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.nav_my_widgets),
                        style = MaterialTheme.typography.headlineSmall,
                        color = HosPureWhite
                    )
                }

                IconButton(
                    onClick = onSyncWidgets,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("my_widgets_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Sync,
                        contentDescription = stringResource(R.string.my_widgets_sync_action),
                        tint = HosPrimary
                    )
                }
            }

            if (widgets.isEmpty()) {
                EmptyWidgetsState(
                    onBrowseIcons = onBrowseIcons,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 28.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("configured_widgets_list")
                ) {
                    items(
                        items = widgets,
                        key = { it.appWidgetId }
                    ) { instance ->
                        ConfiguredWidgetCard(
                            instance = instance,
                            onCustomize = { onOpenCustomize(instance.appIcon) },
                            onLaunchApp = { onLaunchApp(instance.appIcon) },
                            onRemove = { onRemoveWidget(instance) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfiguredWidgetCard(
    instance: ConfiguredWidgetInstance,
    onCustomize: () -> Unit,
    onLaunchApp: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(HosSelectableTileSurface)
            .border(1.dp, HosSubtleBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onCustomize)
            .padding(16.dp)
            .testTag("widget_card_${instance.appWidgetId}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                MiniWidgetPreviewBadge(
                    appIcon = instance.appIcon,
                    sizeOption = instance.sizeOption
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = instance.appIcon.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = HosPureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(HosBlue.copy(alpha = 0.18f))
                                .border(1.dp, HosBlue.copy(alpha = 0.5f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = instance.sizeOption.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = HosPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusDotLabel(
                            text = if (instance.isBoundToLauncher) {
                                stringResource(R.string.widget_status_active)
                            } else {
                                stringResource(R.string.widget_status_pending)
                            },
                            dotColor = if (instance.isBoundToLauncher) Color(0xFF2EE57D) else HosBlue
                        )

                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = HosMutedLabel
                        )

                        StatusDotLabel(
                            text = if (instance.isTargetAppAvailable) {
                                stringResource(R.string.widget_app_available)
                            } else {
                                stringResource(R.string.widget_app_fallback)
                            },
                            dotColor = if (instance.isTargetAppAvailable) HosPrimary else HosTangeloOrange
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(HosBlue)
                    .clickable(onClick = onLaunchApp)
                    .padding(horizontal = 16.dp)
                    .testTag("widget_open_app_${instance.appWidgetId}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = HosPureWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.action_open_app),
                    style = MaterialTheme.typography.labelLarge,
                    color = HosPureWhite
                )
            }

            Row(
                modifier = Modifier
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(HosSelectorTileBase)
                    .border(1.dp, HosSubtleBorder, CircleShape)
                    .clickable(onClick = onRemove)
                    .padding(horizontal = 16.dp)
                    .testTag("widget_remove_${instance.appWidgetId}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.action_remove_widget),
                    tint = HosError,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.action_remove_widget),
                    style = MaterialTheme.typography.labelMedium,
                    color = HosError
                )
            }
        }
    }
}

@Composable
private fun MiniWidgetPreviewBadge(
    appIcon: HyperOsAppIcon,
    sizeOption: WidgetSizeOption
) {
    val context = LocalContext.current
    val bitmap = remember(appIcon.id, sizeOption) {
        HosWidgetBitmapRenderer.renderWidgetBitmap(context, appIcon, sizeOption).asImageBitmap()
    }

    val (badgeW, badgeH) = when (sizeOption) {
        WidgetSizeOption.SIZE_1X1 -> 44.dp to 44.dp
        WidgetSizeOption.SIZE_2X1 -> 58.dp to 30.dp
        WidgetSizeOption.SIZE_1X2 -> 30.dp to 58.dp
        WidgetSizeOption.SIZE_2X2 -> 52.dp to 52.dp
    }

    Box(
        modifier = Modifier
            .size(66.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(HosSelectorTileBase)
            .border(1.dp, HosSubtleBorder, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = appIcon.name,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .width(badgeW)
                .height(badgeH)
        )
    }
}

@Composable
private fun StatusDotLabel(
    text: String,
    dotColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = HosSecondaryText
        )
    }
}

@Composable
private fun EmptyWidgetsState(
    onBrowseIcons: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(HosSelectableTileSurface)
                .border(1.dp, HosSubtleBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Widgets,
                contentDescription = null,
                tint = HosBlue,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.my_widgets_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = HosPureWhite
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.my_widgets_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = HosSecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBrowseIcons,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = HosBlue,
                contentColor = HosPureWhite
            ),
            modifier = Modifier
                .height(48.dp)
                .testTag("empty_widgets_browse_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.my_widgets_browse_icons),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
