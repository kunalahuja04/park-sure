package com.example.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.MinimalAccentMint
import com.example.ui.theme.MinimalBg
import com.example.ui.theme.MinimalCardBg
import com.example.ui.theme.MinimalCardBorder
import com.example.ui.theme.MinimalNavBg
import com.example.ui.theme.MinimalNavBorder
import com.example.ui.theme.MinimalPillActive
import com.example.ui.theme.MinimalPillBg
import com.example.ui.theme.MinimalPrimaryDarkGreen
import com.example.ui.theme.MinimalPrimaryGreen
import com.example.ui.theme.MinimalSurfaceAlt
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.MinimalTextTertiary
import com.example.ui.theme.PureWhite
import com.example.util.BarcodeEngine
import com.example.util.SampleCode
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    isTorchOn: Boolean,
    onToggleTorch: () -> Unit,
    onScanDetected: (code: String, format: String) -> Unit,
    onNavigateToInsurance: (stationCode: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
            if (!isGranted) {
                Toast.makeText(context, "Camera permission required for scanning", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Laser Animation Transition
    val infiniteTransition = rememberInfiniteTransition(label = "laser_anim")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    var showManualInputDialog by remember { mutableStateOf(false) }
    var manualInputCode by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("scan_screen_root")
    ) {
        // Camera Viewfinder or Simulated Camera Visual
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                            camera.cameraControl.enableTorch(isTorchOn)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // High fidelity simulated scanner background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF262C26),
                                MinimalTextPrimary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MinimalAccentMint.copy(alpha = 0.8f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Camera Scanning Active",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Point camera at Station QR Exit poster or Product Barcode, or tap sample chips below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Viewfinder HUD Mask Canvas with animated scanning line
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val frameSize = canvasWidth * 0.72f
            val left = (canvasWidth - frameSize) / 2f
            val top = canvasHeight * 0.22f

            // Frame Corner Brackets
            val cornerLen = 36.dp.toPx()
            val strokeW = 4.dp.toPx()
            val cornerColor = MinimalPrimaryGreen

            // Top-Left
            drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokeW)
            drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLen), strokeW)

            // Top-Right
            drawLine(cornerColor, Offset(left + frameSize, top), Offset(left + frameSize - cornerLen, top), strokeW)
            drawLine(cornerColor, Offset(left + frameSize, top), Offset(left + frameSize, top + cornerLen), strokeW)

            // Bottom-Left
            drawLine(cornerColor, Offset(left, top + frameSize), Offset(left + cornerLen, top + frameSize), strokeW)
            drawLine(cornerColor, Offset(left, top + frameSize), Offset(left, top + frameSize - cornerLen), strokeW)

            // Bottom-Right
            drawLine(cornerColor, Offset(left + frameSize, top + frameSize), Offset(left + frameSize - cornerLen, top + frameSize), strokeW)
            drawLine(cornerColor, Offset(left + frameSize, top + frameSize), Offset(left + frameSize, top + cornerLen), strokeW)

            // Animated Laser Beam
            val currentLaserY = top + (frameSize * laserPosition)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MinimalPrimaryGreen,
                        MinimalAccentMint,
                        MinimalPrimaryGreen,
                        Color.Transparent
                    )
                ),
                start = Offset(left + 8.dp.toPx(), currentLaserY),
                end = Offset(left + frameSize - 8.dp.toPx(), currentLaserY),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Top Control Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 40.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MinimalNavBg.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, MinimalCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MinimalPrimaryGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SCANNER ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MinimalTextPrimary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Torch Toggle
                IconButton(
                    onClick = onToggleTorch,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MinimalNavBg.copy(alpha = 0.95f))
                        .border(1.dp, MinimalCardBorder, CircleShape)
                        .testTag("torch_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Torch",
                        tint = if (isTorchOn) MinimalPrimaryGreen else MinimalTextTertiary
                    )
                }

                // Manual Input
                IconButton(
                    onClick = { showManualInputDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MinimalNavBg.copy(alpha = 0.95f))
                        .border(1.dp, MinimalCardBorder, CircleShape)
                        .testTag("manual_input_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Enter Code Manually",
                        tint = MinimalPrimaryGreen
                    )
                }
            }
        }

        // Bottom Controls & Sample Code Tester Carousel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MinimalBg.copy(alpha = 0.85f),
                            MinimalBg
                        )
                    )
                )
                .padding(bottom = 90.dp, top = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUICK TEST LOOKUPS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MinimalTextSecondary
                )
                Text(
                    text = "Tap to simulate scan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MinimalPrimaryGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Carousel of real-world sample codes
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(BarcodeEngine.SAMPLE_BARCODES) { sample ->
                    SampleBarcodeChip(
                        sample = sample,
                        onClick = {
                            onScanDetected(sample.code, sample.format)
                        }
                    )
                }
            }
        }
    }

    // Manual Code Input Dialog
    if (showManualInputDialog) {
        AlertDialog(
            onDismissRequest = { showManualInputDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MinimalPrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manual Code / Barcode Lookup", color = MinimalTextPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter any Station QR payload, Product EAN Barcode, Vehicle Reg number, or Policy ID:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualInputCode,
                        onValueChange = { manualInputCode = it },
                        placeholder = { Text("e.g. 8904123456789 or CSMT") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_code_input_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualInputCode.isNotBlank()) {
                            showManualInputDialog = false
                            onScanDetected(manualInputCode.trim(), "MANUAL")
                            manualInputCode = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MinimalPrimaryGreen, contentColor = PureWhite),
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier.testTag("submit_manual_code_button")
                ) {
                    Text("Lookup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualInputDialog = false }) {
                    Text("Cancel", color = MinimalTextSecondary)
                }
            }
        )
    }
}

@Composable
fun SampleBarcodeChip(
    sample: SampleCode,
    onClick: () -> Unit
) {
    val isStation = sample.category.contains("Station", ignoreCase = true)
    val isProduct = sample.category.contains("Product", ignoreCase = true)
    val isVehicle = sample.category.contains("Vehicle", ignoreCase = true)

    val chipAccent = when {
        isStation -> MinimalPrimaryGreen
        isProduct -> Color(0xFF9E6B00)
        isVehicle -> Color(0xFF1D6F8A)
        else -> MinimalPrimaryGreen
    }

    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
            .testTag("sample_chip_${sample.format}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalCardBg),
        border = BorderStroke(1.dp, MinimalCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MinimalPillActive
                ) {
                    Text(
                        text = sample.format,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MinimalTextPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Icon(
                    imageVector = when {
                        isStation -> Icons.Default.TwoWheeler
                        isProduct -> Icons.Default.ShoppingBag
                        isVehicle -> Icons.Default.ElectricBike
                        else -> Icons.Default.Security
                    },
                    contentDescription = null,
                    tint = chipAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = sample.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MinimalTextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sample.description,
                style = MaterialTheme.typography.bodySmall,
                color = MinimalTextSecondary,
                maxLines = 2
            )
        }
    }
}
