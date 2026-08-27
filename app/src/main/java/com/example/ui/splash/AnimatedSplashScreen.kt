package com.example.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MinimalAccentMint
import com.example.ui.theme.MinimalBg
import com.example.ui.theme.MinimalCardBg
import com.example.ui.theme.MinimalCardBorder
import com.example.ui.theme.MinimalPillActive
import com.example.ui.theme.MinimalPillBg
import com.example.ui.theme.MinimalPrimaryDarkGreen
import com.example.ui.theme.MinimalPrimaryGreen
import com.example.ui.theme.MinimalSurfaceAlt
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.MinimalTextTertiary
import com.example.ui.theme.PureWhite
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }
    val checkmarkAnim = remember { Animatable(0f) }
    var showTagline by remember { mutableStateOf(false) }
    var showActionButtons by remember { mutableStateOf(false) }

    // Pulsing background glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    LaunchedEffect(Unit) {
        // Step 1: Scale and fade in logo
        alpha.animateTo(1f, tween(700))
        scale.animateTo(
            targetValue = 1.05f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
        )
        scale.animateTo(1f, tween(200))

        // Step 2: Animate Checkmark
        checkmarkAnim.animateTo(1f, tween(500, easing = FastOutSlowInEasing))

        // Step 3: Reveal Tagline & Action
        showTagline = true
        delay(400)
        showActionButtons = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalBg)
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        // Decorative subtle minimalist background geometric rings
        Canvas(
            modifier = Modifier
                .size(340.dp)
                .rotate(ringRotation)
        ) {
            drawCircle(
                color = MinimalAccentMint.copy(alpha = 0.35f * pulseScale),
                radius = size.width * 0.45f * pulseScale
            )
            drawCircle(
                color = MinimalPrimaryGreen.copy(alpha = 0.15f),
                radius = size.width * 0.42f,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(12f, 20f),
                        0f
                    )
                )
            )
            drawCircle(
                color = MinimalCardBorder,
                radius = size.width * 0.32f,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Logo Emblem
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .alpha(alpha.value),
                contentAlignment = Alignment.Center
            ) {
                // Outer clean circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MinimalPillActive)
                        .border(1.dp, MinimalCardBorder, CircleShape)
                )

                // Shield custom canvas
                Canvas(modifier = Modifier.size(90.dp)) {
                    val w = size.width
                    val h = size.height

                    val shieldPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.14f)
                        lineTo(w * 0.82f, h * 0.26f)
                        lineTo(w * 0.82f, h * 0.56f)
                        cubicTo(w * 0.82f, h * 0.76f, w * 0.5f, h * 0.90f, w * 0.5f, h * 0.90f)
                        cubicTo(w * 0.5f, h * 0.90f, w * 0.18f, h * 0.76f, w * 0.18f, h * 0.56f)
                        lineTo(w * 0.18f, h * 0.26f)
                        close()
                    }

                    drawPath(
                        path = shieldPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MinimalPrimaryGreen,
                                MinimalPrimaryDarkGreen
                            )
                        )
                    )

                    drawPath(
                        path = shieldPath,
                        color = PureWhite.copy(alpha = 0.35f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Centered 2-Wheeler Icon
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = "ParkSure Two-Wheeler Icon",
                    tint = PureWhite,
                    modifier = Modifier.size(38.dp)
                )

                // Verified Badge at top right
                if (checkmarkAnim.value > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp)
                            .scale(checkmarkAnim.value)
                            .clip(CircleShape)
                            .background(MinimalPrimaryGreen)
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Badge",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Brand Name & Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "PARKSURE",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = MinimalTextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MinimalPillActive,
                    border = BorderStroke(1.dp, MinimalCardBorder)
                ) {
                    Text(
                        text = "₹5/DAY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MinimalPrimaryGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle / Product Proposition
            AnimatedVisibility(
                visible = showTagline,
                enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { 30 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "QR-Powered Micro-Insurance for Commuters",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MinimalPrimaryGreen,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Theft & damage protection for two-wheelers parked at 100+ Mumbai Suburban Railway Stations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MinimalTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Action Launchers
            AnimatedVisibility(
                visible = showActionButtons,
                enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.9f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onSplashFinished,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("splash_get_started_button"),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MinimalPrimaryGreen,
                            contentColor = PureWhite
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Scan Station QR / Enter App",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MinimalPrimaryGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Instant 60-Sec Activation • No Paperwork • WhatsApp Claims",
                            style = MaterialTheme.typography.labelSmall,
                            color = MinimalTextSecondary
                        )
                    }
                }
            }
        }

        // Bottom version watermark
        Text(
            text = "PARKSURE • Mumbai Station Micro-Underwriting",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MinimalTextMuted,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}

