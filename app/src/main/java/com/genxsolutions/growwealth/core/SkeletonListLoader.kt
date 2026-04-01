package com.genxsolutions.growwealth.core

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonListLoader(
    modifier: Modifier = Modifier,
    itemCount: Int = 6,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
    verticalSpacing: Dp = 10.dp,
    cardShape: Shape = RoundedCornerShape(16.dp),
    cardColor: Color = Color(0xFF111111),
    titleWidthStart: Float = 0.52f,
    titleWidthStep: Float = 0.06f,
    titleWidthMax: Float = 0.9f,
    titleHeight: Dp = 14.dp,
    metaHeights: Dp = 10.dp,
    metaBlockWidths: List<Dp> = listOf(72.dp, 88.dp),
    placeholderColorPrimary: Color = Color(0xFFCFDBE9),
    placeholderColorSecondary: Color = Color(0xFFDEE7F1),
    pulseMinAlpha: Float = 0.4f,
    pulseMaxAlpha: Float = 0.95f,
    pulseDurationMs: Int = 900
) {
    val pulse = rememberInfiniteTransition(label = "skeleton-loader")
    val animatedAlpha = pulse.animateFloat(
        initialValue = pulseMinAlpha,
        targetValue = pulseMaxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton-loader-alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        repeat(itemCount) { index ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((titleWidthStart + index * titleWidthStep).coerceAtMost(titleWidthMax))
                            .height(titleHeight)
                            .background(
                                placeholderColorPrimary.copy(alpha = animatedAlpha.value),
                                RoundedCornerShape(7.dp)
                            )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        metaBlockWidths.forEach { blockWidth ->
                            Box(
                                modifier = Modifier
                                    .width(blockWidth)
                                    .height(metaHeights)
                                    .background(
                                        placeholderColorSecondary.copy(alpha = animatedAlpha.value),
                                        RoundedCornerShape(7.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
