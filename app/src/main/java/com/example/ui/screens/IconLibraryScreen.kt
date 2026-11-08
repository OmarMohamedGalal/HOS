package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.HyperOsIconRepository
import com.example.model.HyperOsAppIcon
import com.example.model.WidgetSizeOption
import com.example.ui.theme.HosBlue
import com.example.ui.theme.HosCanvasBlack
import com.example.ui.theme.HosMutedLabel
import com.example.ui.theme.HosOnSurface
import com.example.ui.theme.HosPlaceholderText
import com.example.ui.theme.HosPrimary
import com.example.ui.theme.HosPureWhite
import com.example.ui.theme.HosSearchPillBackground
import com.example.ui.theme.HosSecondaryText
import com.example.ui.theme.HosSelectableTileSurface
import com.example.ui.theme.HosSelectorTileBase
import com.example.ui.theme.HosSubtleBorder
import com.example.ui.theme.HosSurface
import com.example.widget.HosWidgetBitmapRenderer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun IconLibraryScreen(
    icons: List<HyperOsAppIcon>,
    searchQuery: String,
    selectedCategory: String,
    selectedOsVersion: Int,
    configuredWidgetCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onOsVersionSelected: (Int) -> Unit,
    onIconSelected: (HyperOsAppIcon) -> Unit,
    onOpenMyWidgets: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HosSurface, HosCanvasBlack)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
        ) {
            // HyperOS Top Header Bar (56dp height matching Stitch layout)
            HosLibraryHeader(
                selectedOsVersion = selectedOsVersion,
                configuredWidgetCount = configuredWidgetCount,
                onOpenMyWidgets = onOpenMyWidgets,
                onOpenSettings = onOpenSettings
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stitch Pill Search Bar (#181A1F background, 48dp height, full pill radius)
            StitchSearchBar(
                query = searchQuery,
                selectedOsVersion = selectedOsVersion,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Pills
            CategoryFilterRow(
                categories = HyperOsIconRepository.categories,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4-Column Fluid Squircle Icon Grid with smooth transition between HyperOS 2 and 3
            AnimatedContent(
                targetState = selectedOsVersion,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (fadeIn(animationSpec = tween(220)) +
                        slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = 0.72f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialOffsetX = { fullWidth -> direction * (fullWidth / 5) }
                        )).togetherWith(
                        fadeOut(animationSpec = tween(160)) +
                            slideOutHorizontally(
                                animationSpec = tween(180),
                                targetOffsetX = { fullWidth -> -direction * (fullWidth / 6) }
                            )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "os_version_grid_transition"
            ) { _ ->
                if (icons.isEmpty()) {
                    EmptyLibrarySearchState(
                        modifier = Modifier
                            .fillMaxSize()
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 96.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("icon_library_grid")
                    ) {
                        items(
                            items = icons,
                            key = { it.id }
                        ) { appIcon ->
                            HyperOsIconGridTile(
                                appIcon = appIcon,
                                onClick = { onIconSelected(appIcon) }
                            )
                        }
                    }
                }
            }
        }

        // Compact Bottom Nav Bar Slider with categories 2 and 3 + Jelly ("Gilly") Animation
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom = 16.dp)
        ) {
            HosBottomVersionJellySlider(
                selectedOsVersion = selectedOsVersion,
                onOsVersionSelected = onOsVersionSelected
            )
        }
    }
}

/**
 * Small pill-shaped bottom navigation slider with 3 categories: "2", "3", and "4".
 * - Default active category is 3 ("HyperOS 3").
 * - When tapping 2, "HyperOS 2" expands with an elastic jelly ("gilly") spring animation.
 * - When tapping 3, "HyperOS 3" expands with the same elastic jelly ("gilly") spring animation.
 * - When tapping 4, "HyperOS 4" expands with the same elastic jelly ("gilly") spring animation.
 */
@Composable
private fun HosBottomVersionJellySlider(
    selectedOsVersion: Int,
    onOsVersionSelected: (Int) -> Unit
) {
    val targetVersionFloat = selectedOsVersion.coerceIn(2, 4).toFloat()

    // Continuous progress 2f (HyperOS 2) <-> 3f (HyperOS 3) <-> 4f (HyperOS 4) driven by an underdamped jelly spring
    val jellyProgress by animateFloatAsState(
        targetValue = targetVersionFloat,
        animationSpec = spring(
            dampingRatio = 0.48f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "jelly_slider_progress"
    )

    // Compute physical jelly squash-and-stretch deformation during flight and overshoot bounce
    val fracFromStop = (abs(jellyProgress - jellyProgress.roundToInt()) * 2f).coerceIn(0f, 1f)
    val inFlight = sin(fracFromStop * PI.toFloat())
    val overshoot = when {
        jellyProgress < 2f -> 2f - jellyProgress
        jellyProgress > 4f -> jellyProgress - 4f
        abs(jellyProgress - targetVersionFloat) < 0.35f &&
            ((jellyProgress > targetVersionFloat && targetVersionFloat >= 3f) ||
                (jellyProgress < targetVersionFloat && targetVersionFloat <= 3f)) ->
            abs(jellyProgress - targetVersionFloat)
        else -> 0f
    }

    val containerScaleX = 1f + (0.08f * inFlight) - (0.06f * overshoot)
    val containerScaleY = 1f - (0.05f * inFlight) + (0.05f * overshoot)

    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = containerScaleX
                scaleY = containerScaleY
            }
            .shadow(
                elevation = 18.dp,
                shape = CircleShape,
                ambientColor = Color.Black,
                spotColor = HosBlue.copy(alpha = 0.45f)
            )
            .clip(CircleShape)
            .background(Color(0xFF151821).copy(alpha = 0.96f))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.14f),
                        HosBlue.copy(alpha = 0.38f),
                        Color.White.copy(alpha = 0.14f)
                    )
                ),
                shape = CircleShape
            )
            .padding(horizontal = 5.dp, vertical = 5.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.52f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .testTag("bottom_version_slider"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        JellySliderCategoryPill(
            versionNumber = 2,
            shortLabel = stringResource(R.string.hos_slider_2_short),
            fullLabel = stringResource(R.string.hos_slider_2_full),
            isSelected = selectedOsVersion == 2,
            inFlightStretch = inFlight,
            overshootBounce = overshoot,
            onClick = { onOsVersionSelected(2) },
            testTag = "slider_category_2"
        )

        JellySliderCategoryPill(
            versionNumber = 3,
            shortLabel = stringResource(R.string.hos_slider_3_short),
            fullLabel = stringResource(R.string.hos_slider_3_full),
            isSelected = selectedOsVersion == 3,
            inFlightStretch = inFlight,
            overshootBounce = overshoot,
            onClick = { onOsVersionSelected(3) },
            testTag = "slider_category_3"
        )

        JellySliderCategoryPill(
            versionNumber = 4,
            shortLabel = stringResource(R.string.hos_slider_4_short),
            fullLabel = stringResource(R.string.hos_slider_4_full),
            isSelected = selectedOsVersion == 4,
            inFlightStretch = inFlight,
            overshootBounce = overshoot,
            onClick = { onOsVersionSelected(4) },
            testTag = "slider_category_4"
        )
    }
}

@Composable
private fun JellySliderCategoryPill(
    versionNumber: Int,
    shortLabel: String,
    fullLabel: String,
    isSelected: Boolean,
    inFlightStretch: Float,
    overshootBounce: Float,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    val pillScaleX by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.94f,
        animationSpec = spring(
            dampingRatio = 0.46f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pill_scale_x_$versionNumber"
    )

    val activeJellyStretchX = if (isSelected) {
        1f + (0.18f * inFlightStretch) - (0.12f * overshootBounce)
    } else {
        1f - (0.08f * inFlightStretch)
    }
    val activeJellySquashY = if (isSelected) {
        1f - (0.11f * inFlightStretch) + (0.09f * overshootBounce)
    } else {
        1f + (0.04f * inFlightStretch)
    }

    Box(
        modifier = Modifier
            .height(38.dp)
            .graphicsLayer {
                scaleX = pillScaleX * activeJellyStretchX
                scaleY = activeJellySquashY
            }
            .clip(CircleShape)
            .background(
                brush = if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0D84FF),
                            Color(0xFF2B9BFF)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent
                        )
                    )
                }
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.28f),
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = if (isSelected) 18.dp else 14.dp
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.50f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isSelected,
            transitionSpec = {
                (fadeIn(animationSpec = tween(180)) +
                    scaleIn(
                        initialScale = 0.72f,
                        animationSpec = spring(
                            dampingRatio = 0.50f,
                            stiffness = Spring.StiffnessMedium
                        )
                    )).togetherWith(
                    fadeOut(animationSpec = tween(120)) +
                        scaleOut(targetScale = 0.80f, animationSpec = tween(120))
                ).using(
                    SizeTransform(
                        clip = false,
                        sizeAnimationSpec = { _, _ ->
                            spring(
                                dampingRatio = 0.50f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        }
                    )
                )
            },
            label = "pill_label_anim_$versionNumber"
        ) { selected ->
            Text(
                text = if (selected) fullLabel else shortLabel,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = if (selected) 13.5.sp else 13.sp,
                    letterSpacing = if (selected) 0.2.sp else 0.sp
                ),
                color = if (selected) HosPureWhite else HosMutedLabel,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HosLibraryHeader(
    selectedOsVersion: Int,
    configuredWidgetCount: Int,
    onOpenMyWidgets: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_hos_app_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(34.dp)
                    .shadow(4.dp, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
            )
            Text(
                text = stringResource(R.string.hos_brand_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = HosPureWhite
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(HosBlue.copy(alpha = 0.16f))
                    .border(1.dp, HosBlue.copy(alpha = 0.45f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = 0.55f,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
            ) {
                Text(
                    text = when (selectedOsVersion) {
                        2 -> stringResource(R.string.hos_os_badge_v2)
                        4 -> stringResource(R.string.hos_os_badge_v4)
                        else -> stringResource(R.string.hos_os_badge)
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = HosPrimary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // My Widgets Pill Action Button
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(HosSelectableTileSurface)
                    .border(1.dp, HosSubtleBorder, CircleShape)
                    .clickable(onClick = onOpenMyWidgets)
                    .padding(horizontal = 12.dp)
                    .testTag("nav_my_widgets_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Widgets,
                    contentDescription = stringResource(R.string.nav_my_widgets),
                    tint = HosPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.nav_my_widgets),
                    style = MaterialTheme.typography.labelMedium,
                    color = HosOnSurface
                )
                if (configuredWidgetCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(HosBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = configuredWidgetCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = HosPureWhite
                        )
                    }
                }
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(HosSelectableTileSurface)
                    .border(1.dp, HosSubtleBorder, CircleShape)
                    .testTag("nav_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.nav_settings),
                    tint = HosOnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StitchSearchBar(
    query: String,
    selectedOsVersion: Int,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(HosSearchPillBackground)
            .border(1.dp, HosSubtleBorder, CircleShape)
            .padding(horizontal = 16.dp)
            .testTag("icon_search_bar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = HosPlaceholderText,
            modifier = Modifier.size(20.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = when (selectedOsVersion) {
                        2 -> stringResource(R.string.search_placeholder_v2)
                        4 -> stringResource(R.string.search_placeholder_v4)
                        else -> stringResource(R.string.search_placeholder)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = HosPlaceholderText
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = HosPureWhite),
                cursorBrush = SolidColor(HosBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("icon_search_input")
            )
        }
        AnimatedVisibility(
            visible = query.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("clear_search_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.clear_search),
                    tint = HosSecondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category.equals(selectedCategory, ignoreCase = true)
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) HosBlue else HosSelectorTileBase
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) HosBlue else HosSubtleBorder,
                        shape = CircleShape
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp)
                    .testTag("category_chip_${category.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) HosPureWhite else HosMutedLabel
                )
            }
        }
    }
}

@Composable
private fun HyperOsIconGridTile(
    appIcon: HyperOsAppIcon,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val squircleBitmap = remember(appIcon.id) {
        HosWidgetBitmapRenderer.renderWidgetBitmap(
            context = context,
            appIcon = appIcon,
            sizeOption = WidgetSizeOption.SIZE_1X1
        ).asImageBitmap()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag("icon_tile_${appIcon.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(horizontal = 4.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .clip(RoundedCornerShape(22.dp))
                .background(HosSelectableTileSurface)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = squircleBitmap,
                contentDescription = appIcon.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = appIcon.name,
            style = MaterialTheme.typography.labelMedium,
            color = HosOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyLibrarySearchState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(HosSelectableTileSurface)
                .border(1.dp, HosSubtleBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.SearchOff,
                contentDescription = null,
                tint = HosMutedLabel,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.empty_search_title),
            style = MaterialTheme.typography.titleMedium,
            color = HosPureWhite
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.empty_search_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = HosSecondaryText,
            textAlign = TextAlign.Center
        )
    }
}
