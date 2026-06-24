package com.example.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun CircleAppLogo(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF03A9F4), // Bright vivid blue matching image 3
    animate: Boolean = true
) {
    var animationTriggered by remember { mutableStateOf(!animate) }
    
    val transition = updateTransition(
        targetState = if (animationTriggered) 1f else 0f,
        label = "LogoAnimation"
    )

    LaunchedEffect(Unit) {
        if (animate) {
            animationTriggered = true
        }
    }

    val overshootEasing = FastOutSlowInEasing

    val infiniteTransition = rememberInfiniteTransition()
    val continuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val globalRotation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1800, easing = FastOutSlowInEasing) },
        label = "GlobalRotation"
    ) { state -> if (state == 1f) 0f else -180f }

    val centralRingProgress by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, easing = FastOutSlowInEasing) },
        label = "CentralRing"
    ) { state -> if (state == 1f) 1f else 0f }

    val smallDotScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 500, delayMillis = 400, easing = overshootEasing) },
        label = "SmallDot"
    ) { state -> if (state == 1f) 1f else 0f }

    val middleRingProgress by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1000, delayMillis = 600, easing = FastOutSlowInEasing) },
        label = "MiddleRing"
    ) { state -> if (state == 1f) 1f else 0f }

    val outerDotsScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 600, delayMillis = 1000, easing = overshootEasing) },
        label = "OuterDots"
    ) { state -> if (state == 1f) 1f else 0f }

    val outerArcsProgress by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1000, delayMillis = 1200, easing = FastOutSlowInEasing) },
        label = "OuterArcs"
    ) { state -> if (state == 1f) 1f else 0f }

    Canvas(modifier = modifier.aspectRatio(1f).graphicsLayer { rotationZ = globalRotation + continuousRotation }) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.width / 2
        
        // Match thickness scaling from Image 3
        val strokeWidth = size.width * 0.065f 
        val centerRingRadius = baseRadius * 0.18f
        val middleRingRadius = baseRadius * 0.48f
        
        // 1. Central Ring
        if (centralRingProgress > 0f) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * centralRingProgress,
                useCenter = false,
                topLeft = Offset(center.x - centerRingRadius, center.y - centerRingRadius),
                size = Size(centerRingRadius * 2, centerRingRadius * 2),
                style = Stroke(width = strokeWidth)
            )
        }

        // 2. Small solid dot (Lens reflection)
        if (smallDotScale > 0f) {
            val angle45 = Math.toRadians(-45.0)
            val dotOrbit = baseRadius * 0.32f
            drawCircle(
                color = color,
                radius = (baseRadius * 0.06f) * smallDotScale,
                center = Offset(
                    center.x + (dotOrbit * Math.cos(angle45)).toFloat(),
                    center.y + (dotOrbit * Math.sin(angle45)).toFloat()
                )
            )
        }

        // 3. Middle Ring
        if (middleRingProgress > 0f) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * middleRingProgress,
                useCenter = false,
                topLeft = Offset(center.x - middleRingRadius, center.y - middleRingRadius),
                size = Size(middleRingRadius * 2, middleRingRadius * 2),
                style = Stroke(width = strokeWidth)
            )
        }

        // 4. Outer Solid Dots
        val outerElementsRadius = baseRadius * 0.82f
        val outerDotRadius = baseRadius * 0.12f
        
        if (outerDotsScale > 0f) {
            // Top Circle
            drawCircle(
                color = color,
                radius = outerDotRadius * outerDotsScale,
                center = Offset(center.x, center.y - outerElementsRadius)
            )
            
            // Bottom Right Circle (30 degrees in normal math, but we need to match positions)
            // Top is at -90deg. Next is 120deg apart -> 30deg. Next is 150deg.
            val angleBR = Math.toRadians(30.0)
            drawCircle(
                color = color,
                radius = outerDotRadius * outerDotsScale,
                center = Offset(
                    center.x + (outerElementsRadius * Math.cos(angleBR)).toFloat(),
                    center.y + (outerElementsRadius * Math.sin(angleBR)).toFloat()
                )
            )

            // Bottom Left circle
            val angleBL = Math.toRadians(150.0)
            drawCircle(
                color = color,
                radius = outerDotRadius * outerDotsScale,
                center = Offset(
                    center.x + (outerElementsRadius * Math.cos(angleBL)).toFloat(),
                    center.y + (outerElementsRadius * Math.sin(angleBL)).toFloat()
                )
            )
        }

        // 5. Outer Arcs
        if (outerArcsProgress > 0f) {
            val arcRectSize = outerElementsRadius * 2
            val arcTopLeft = Offset(center.x - outerElementsRadius, center.y - outerElementsRadius)
            val arcSweep = 76f * outerArcsProgress
            val arcStroke = Stroke(width = strokeWidth + 4f, cap = StrokeCap.Round)

            // Arc 1: Top to Bottom Right. Centered at -30 deg. Start at -68.
            drawArc(
                color = color,
                startAngle = -68f,
                sweepAngle = arcSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = Size(arcRectSize, arcRectSize),
                style = arcStroke
            )
            
            // Arc 2: Bottom Right to Bottom Left. Centered at 90 deg. Start at 52.
            drawArc(
                color = color,
                startAngle = 52f,
                sweepAngle = arcSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = Size(arcRectSize, arcRectSize),
                style = arcStroke
            )

            // Arc 3: Bottom Left to Top. Centered at 210 deg. Start at 172.
            drawArc(
                color = color,
                startAngle = 172f,
                sweepAngle = arcSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = Size(arcRectSize, arcRectSize),
                style = arcStroke
            )
        }
    }
}

