package com.example.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PolicyEntity
import com.example.ui.theme.MinimalAccentMint
import com.example.ui.theme.MinimalBg
import com.example.ui.theme.MinimalCardBg
import com.example.ui.theme.MinimalCardBorder
import com.example.ui.theme.MinimalPillActive
import com.example.ui.theme.MinimalPillBg
import com.example.ui.theme.MinimalPrimaryGreen
import com.example.ui.theme.MinimalSurfaceAlt
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.MinimalTextTertiary
import com.example.ui.theme.PureWhite
import com.example.util.QRGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PolicyQrPassDialog(
    policy: PolicyEntity,
    onDismiss: () -> Unit,
    onFileClaim: (policy: PolicyEntity) -> Unit
) {
    val context = LocalContext.current

    val qrBitmap: Bitmap? = remember(policy.qrPassCode) {
        QRGenerator.generateQRCodeBitmap(
            content = policy.qrPassCode,
            width = 400,
            height = 400,
            foregroundColor = MinimalTextPrimary.toArgb(),
            backgroundColor = Color.White.toArgb()
        )
    }

    val now = System.currentTimeMillis()
    val isExpired = now > policy.endTimeMillis
    val remainingMillis = (policy.endTimeMillis - now).coerceAtLeast(0L)
    val remainingHours = remainingMillis / (1000 * 60 * 60)
    val remainingMinutes = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60)

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MinimalCardBorder, RoundedCornerShape(24.dp))
                .testTag("policy_qr_pass_dialog"),
            color = MinimalCardBg,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MinimalPillActive,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MinimalPrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PARKSURE PASS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = policy.policyNumber,
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MinimalTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Generated QR Code Frame
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MinimalCardBorder),
                    modifier = Modifier.size(220.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Policy Gate Pass QR",
                                modifier = Modifier.size(190.dp)
                            )
                        } else {
                            Text(
                                text = "QR Pass Generated",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MinimalTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Active Countdown Badge
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = if (isExpired) Color(0xFFFFECEB) else MinimalPillActive,
                    border = BorderStroke(1.dp, if (isExpired) Color(0xFFEF476F) else MinimalPrimaryGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isExpired) Icons.Default.Close else Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (isExpired) Color(0xFFD9534F) else MinimalPrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isExpired) "PASS EXPIRED" else "ACTIVE • ${remainingHours}h ${remainingMinutes}m remaining",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isExpired) Color(0xFFD9534F) else MinimalPrimaryGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pass Details Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MinimalSurfaceAlt),
                    border = BorderStroke(1.dp, MinimalCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Vehicle Reg Number",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MinimalTextSecondary
                                )
                                Text(
                                    text = policy.vehicleNumber,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MinimalTextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Plan Tier",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MinimalTextSecondary
                                )
                                Text(
                                    text = policy.planTier.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MinimalPrimaryGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = MinimalCardBorder)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MinimalPrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = policy.stationName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MinimalTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Valid Until:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalTextSecondary
                            )
                            Text(
                                text = dateFormat.format(Date(policy.endTimeMillis)),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MinimalTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Passive Inspection Status Notice
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MinimalPillActive,
                    border = BorderStroke(1.dp, MinimalCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MinimalPrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Passive Inspection: ${policy.inspectionStatus.label}. Baseline photos linked.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MinimalTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Buttons: File Claim or Share Pass
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "ParkSure Two-Wheeler Insurance Pass\nPolicy: ${policy.policyNumber}\nVehicle: ${policy.vehicleNumber}\nStation: ${policy.stationName}\nValid until: ${dateFormat.format(Date(policy.endTimeMillis))}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Pass"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(percent = 50),
                        border = BorderStroke(1.dp, MinimalCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalTextPrimary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = MinimalPrimaryGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onFileClaim(policy)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MinimalPrimaryGreen,
                            contentColor = PureWhite
                        )
                    ) {
                        Text(
                            text = "File a Claim",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

