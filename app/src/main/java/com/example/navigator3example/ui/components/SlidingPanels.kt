package com.example.navigator3example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * SlidingPanels - a reusable two-panel layout with a draggable bottom panel that snaps to preset sizes.
 *
 * - The top area shows [topContent].
 * - The bottom area is a card with a draggable top bar that includes a hamburger menu on the right.
 * - Drag up/down to resize; on release it snaps to the nearest size in [snapFractions].
 *
 * snapFractions: fractions (0f..1f) of total height representing the height of the bottom panel.
 */
@Composable
fun SlidingPanels(
    modifier: Modifier = Modifier,
    bottomTitle: String? = null,
    snapFractions: List<Float> = listOf(0.25f, 0.4f, 0.6f, 0.85f),
    initialFraction: Float = 0.4f,
    topScrollable: Boolean = true,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable ColumnScope.() -> Unit
) {
    val sanitizedSnaps = remember(snapFractions) {
        snapFractions.map { it.coerceIn(0.1f, 0.95f) }.distinct().sorted()
    }
    val initial = sanitizedSnaps.minByOrNull { abs(it - initialFraction.coerceIn(0.1f, 0.95f)) } ?: 0.4f

    // Save and animate the bottom panel height fraction
    val fractionState = rememberSaveable(sanitizedSnaps, saver = fractionSaver()) { mutableStateOf(initial) }
    val animFraction = remember { Animatable(fractionState.value) }
    val scope = rememberCoroutineScope()


    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxH = with(LocalDensity.current) { constraints.maxHeight.toFloat() }
        val bottomHeightPx = animFraction.value * maxH
        val topHeightPx = (maxH - bottomHeightPx).coerceAtLeast(0f)

        // TOP PANEL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { topHeightPx.toDp() })
        ) {
            // Use a Surface/Card for visual parity with existing screens
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = (if (topScrollable) {
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    } else {
                        Modifier.fillMaxSize()
                    }).padding(12.dp)
                ) {
                    topContent()
                }
            }
        }

        // BOTTOM PANEL - anchored to bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(with(LocalDensity.current) { bottomHeightPx.toDp() })
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(8.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Draggable Top Bar for the bottom panel
                    BottomPanelTopBar(
                        title = bottomTitle,
                        onDragDelta = { dyPx ->
                            // Positive dy => dragging down => reduce bottom height (collapse). Negative => expand.
                            val deltaFraction = dyPx / maxH
                            val newFraction = (fractionState.value - deltaFraction)
                                .coerceIn(sanitizedSnaps.first(), sanitizedSnaps.last())
                            // Snap rendering to the drag position so the panel stays under the finger
                            fractionState.value = newFraction
                            scope.launch { animFraction.snapTo(newFraction) }
                        },
                        onDragEnd = { velocityY ->
                            // Predictive snap based on current fraction and velocity
                            val current = fractionState.value
                            val predicted = (current - (velocityY / maxH) * 0.25f).coerceIn(sanitizedSnaps.first(), sanitizedSnaps.last())
                            val target = sanitizedSnaps.minByOrNull { abs(it - predicted) } ?: current
                            if (target != current) {
                                scope.launch {
                                    animFraction.animateTo(target, animationSpec = TweenSpec(220))
                                    fractionState.value = target
                                }
                            } else {
                                scope.launch { animFraction.animateTo(current, TweenSpec(180)) }
                            }
                        }
                    )

                    // Content Area (no internal scroll; bottom screens manage their own scrolling)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        bottomContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun TopPanelTopBar(
    title: String?,
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        // Title (optional)
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(3f)
            )
        } else {
            Spacer(modifier = Modifier.weight(3f))
        }
    }
}


@Composable
private fun BottomPanelTopBar(
    title: String?,
    onDragDelta: (deltaY: Float) -> Unit,
    onDragEnd: (velocityY: Float) -> Unit
) {
    val draggableState = rememberDraggableState { delta ->
        onDragDelta(delta)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity -> onDragEnd(velocity) }
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp)
            .semantics { contentDescription = "Bottom panel handle" },
        verticalAlignment = Alignment.CenterVertically
    ) {


        // Title (optional)
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(3f)
            )
        } else {
            Spacer(modifier = Modifier.weight(3f))
        }

        // Handle indicator (center) for affordance
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            // Small grabber line
            HorizontalDivider(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp))
            )
        }
    }
}

private fun fractionSaver(): Saver<MutableState<Float>, Any> {
    return listSaver(
        save = { listOf(it.value) },
        restore = { restored -> mutableStateOf(restored.firstOrNull() as? Float ?: 0.4f) }
    )
}
