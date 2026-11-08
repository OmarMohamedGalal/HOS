package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.data.HosWidgetStore
import com.example.model.HyperOsAppIcon
import com.example.model.WidgetSizeOption
import com.example.ui.theme.HosBlue
import com.example.ui.theme.HosMutedLabel
import com.example.ui.theme.HosOnPrimary
import com.example.ui.theme.HosOnSurface
import com.example.ui.theme.HosPrimary
import com.example.ui.theme.HosPureWhite
import com.example.ui.theme.HosSecondaryText
import com.example.ui.theme.HosSelectableTileSurface
import com.example.ui.theme.HosSurface
import com.example.widget.HosWidgetBitmapRenderer

@Composable
fun IconCustomizationScreen(
    appIcon: HyperOsAppIcon,
    initialSize: WidgetSizeOption,
    showLabelOn2x2: Boolean,
    manualPinGuide: Pair<HyperOsAppIcon, WidgetSizeOption>?,
    onDismiss: () -> Unit,
    onAddWidgetToHomeScreen: (WidgetSizeOption) -> Unit,
    onTestLaunchApp: () -> Unit,
    onDismissManualPinGuide: () -> Unit
) {
    val context = LocalContext.current
    var selectedSizeKey by rememberSaveable(appIcon.id) {
        mutableStateOf(initialSize.key)
    }
    val selectedSize = WidgetSizeOption.fromKey(selectedSizeKey)

    // Keep HosWidgetStore synchronized with the currently inspected icon & size
    LaunchedEffect(appIcon.id, selectedSize) {
        HosWidgetStore.setLastSelectedAppId(context, appIcon.id)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1013))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Stitch Header (<header> from Stitch reference)
            StitchCustomizationHeader(
                title = appIcon.name,
                onDismiss = onDismiss,
                onConfirm = { onAddWidgetToHomeScreen(selectedSize) }
            )

            // Main Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Central Stage — Expanded Gradient Icon Widget Preview
                FloatingWidgetPreviewStage(
                    appIcon = appIcon,
                    selectedSize = selectedSize,
                    onPreviewClick = onTestLaunchApp,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icon Size Selector Section matching Stitch screenshots
                    WidgetSizeSelectorSection(
                        selectedSize = selectedSize,
                        onSizeSelected = { size -> selectedSizeKey = size.key },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Occupied Slots Card (as shown in the Stitch 2x2 screenshot)
                    OccupiedSlotsCard(
                        selectedSize = selectedSize,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Bottom Primary Action Button + HyperOS Launcher Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { onAddWidgetToHomeScreen(selectedSize) },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0066FF),
                        contentColor = HosPureWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 18.dp,
                            shape = CircleShape,
                            ambientColor = HosBlue,
                            spotColor = HosBlue
                        )
                        .testTag("add_to_home_screen_button")
                ) {
                    if (selectedSize == WidgetSizeOption.SIZE_2X2) {
                        Icon(
                            imageVector = Icons.Outlined.AddCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else if (selectedSize == WidgetSizeOption.SIZE_1X2) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = stringResource(R.string.add_to_home_screen),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Compatible with HyperOS Launcher & Fold styles",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6E737D),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (manualPinGuide != null) {
        val (guideIcon, guideSize) = manualPinGuide
        AlertDialog(
            onDismissRequest = onDismissManualPinGuide,
            containerColor = HosSelectableTileSurface,
            titleContentColor = HosPureWhite,
            textContentColor = HosSecondaryText,
            title = {
                Text(
                    text = stringResource(R.string.pin_unsupported_title),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.pin_unsupported_message,
                        guideSize.label,
                        guideIcon.name
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissManualPinGuide) {
                    Text(
                        text = stringResource(R.string.action_got_it),
                        color = HosBlue,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }
}

@Composable
private fun StitchCustomizationHeader(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(48.dp)
                .testTag("customize_dismiss_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.action_dismiss),
                tint = HosOnSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = HosPureWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .testTag("customize_header_title")
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onConfirm,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("customize_confirm_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.action_confirm),
                    tint = HosOnSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(HosPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = stringResource(R.string.profile_badge),
                    tint = HosOnPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Renders the Floating Preview Stage using the exact same HosWidgetBitmapRenderer
 * that renders the Android Home Screen widget RemoteViews.
 */
@Composable
private fun FloatingWidgetPreviewStage(
    appIcon: HyperOsAppIcon,
    selectedSize: WidgetSizeOption,
    onPreviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val renderedBitmap = remember(appIcon.id, selectedSize) {
        HosWidgetBitmapRenderer.renderWidgetBitmap(
            context = context,
            appIcon = appIcon,
            sizeOption = selectedSize
        ).asImageBitmap()
    }

    val targetWidth by animateDpAsState(
        targetValue = when (selectedSize) {
            WidgetSizeOption.SIZE_1X1 -> 132.dp
            WidgetSizeOption.SIZE_2X1 -> 244.dp
            WidgetSizeOption.SIZE_1X2 -> 122.dp
            WidgetSizeOption.SIZE_2X2 -> 220.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "preview_width"
    )

    val targetHeight by animateDpAsState(
        targetValue = when (selectedSize) {
            WidgetSizeOption.SIZE_1X1 -> 132.dp
            WidgetSizeOption.SIZE_2X1 -> 118.dp
            WidgetSizeOption.SIZE_1X2 -> 248.dp
            WidgetSizeOption.SIZE_2X2 -> 220.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "preview_height"
    )

    val cornerRadius by animateDpAsState(
        targetValue = when (selectedSize) {
            WidgetSizeOption.SIZE_1X1 -> 32.dp
            WidgetSizeOption.SIZE_2X1 -> 34.dp
            WidgetSizeOption.SIZE_1X2 -> 34.dp
            WidgetSizeOption.SIZE_2X2 -> 50.dp
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "preview_corner"
    )

    Column(
        modifier = modifier.padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(258.dp)
                .testTag("widget_live_preview_container"),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = renderedBitmap,
                contentDescription = "${appIcon.name} ${selectedSize.label} widget preview",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .width(targetWidth)
                    .height(targetHeight)
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(cornerRadius),
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
                    .clip(RoundedCornerShape(cornerRadius))
                    .clickable(onClick = onPreviewClick)
                    .testTag("test_launch_app_button")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.TouchApp,
                contentDescription = null,
                tint = HosMutedLabel,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Touch and hold to edit on desktop",
                style = MaterialTheme.typography.bodySmall,
                color = HosMutedLabel
            )
        }
    }
}

@Composable
private fun WidgetSizeSelectorSection(
    selectedSize: WidgetSizeOption,
    onSizeSelected: (WidgetSizeOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF141619))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Icon size",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = HosPureWhite
            )
            Text(
                text = selectedSize.aspectHint,
                style = MaterialTheme.typography.bodySmall,
                color = HosMutedLabel
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WidgetSizeOption.entries.forEach { option ->
                StitchSizeSelectorCard(
                    sizeOption = option,
                    isSelected = option == selectedSize,
                    onClick = { onSizeSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun StitchSizeSelectorCard(
    sizeOption: WidgetSizeOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
            .testTag("size_selector_${sizeOption.key}")
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    if (isSelected && sizeOption == WidgetSizeOption.SIZE_2X2) {
                        Color(0xFF102444)
                    } else {
                        Color(0xFF222428)
                    }
                )
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) Color(0xFF2B84FF) else Color.Transparent,
                    shape = RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            val (innerWidth, innerHeight, innerRadius) = when (sizeOption) {
                WidgetSizeOption.SIZE_1X1 -> Triple(26.dp, 26.dp, 9.dp)
                WidgetSizeOption.SIZE_2X1 -> Triple(42.dp, 22.dp, 11.dp)
                WidgetSizeOption.SIZE_1X2 -> Triple(20.dp, 40.dp, 10.dp)
                WidgetSizeOption.SIZE_2X2 -> Triple(42.dp, 42.dp, 12.dp)
            }

            Box(
                modifier = Modifier
                    .width(innerWidth)
                    .height(innerHeight)
                    .clip(RoundedCornerShape(innerRadius))
                    .background(
                        when {
                            isSelected && sizeOption == WidgetSizeOption.SIZE_2X2 -> Color(0xFF0066FF)
                            isSelected && sizeOption == WidgetSizeOption.SIZE_2X1 -> Color(0xFF3E90FF)
                            isSelected -> Color(0xFF3A3D45)
                            else -> Color(0xFF34373D)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected && sizeOption == WidgetSizeOption.SIZE_2X2) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = HosPureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = sizeOption.label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) Color(0xFF3E90FF) else HosMutedLabel
        )
    }
}

@Composable
private fun OccupiedSlotsCard(
    selectedSize: WidgetSizeOption,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF141619))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22252A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Dashboard,
                    contentDescription = null,
                    tint = Color(0xFFB5BAC4),
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Occupied slots",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = HosPureWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = selectedSize.occupiedSlotsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = HosMutedLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = "READY",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = HosMutedLabel
        )
    }
}
