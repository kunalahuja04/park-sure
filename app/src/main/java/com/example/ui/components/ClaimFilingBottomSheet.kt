package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class IncidentTypeOption(
    val title: String,
    val suggestedAmount: Int,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimFilingBottomSheet(
    policy: PolicyEntity,
    onDismiss: () -> Unit,
    onSubmitClaim: (incidentType: String, description: String, amount: Int) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val incidentOptions = listOf(
        IncidentTypeOption("Rear View Mirror Stolen", 500, "Mirror removed while parked in lot"),
        IncidentTypeOption("Scratch / Minor Body Dent", 1200, "Adjacent bike contact or vandalism"),
        IncidentTypeOption("Locked Helmet Stolen", 1800, "Helmet locked to bike missing"),
        IncidentTypeOption("Broken Lever / Indicator", 750, "Side damage during parking congestion"),
        IncidentTypeOption("Complete Vehicle Theft", 25000, "Vehicle missing from parking bay")
    )

    var selectedIncident by remember { mutableStateOf(incidentOptions[0]) }
    var incidentDescription by remember { mutableStateOf("") }
    var photoAttached by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MinimalCardBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MinimalCardBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .testTag("claim_filing_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "File Micro-Insurance Claim",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MinimalTextPrimary
                    )
                    Text(
                        text = "Policy: ${policy.policyNumber} • ${policy.vehicleNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary
                    )
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

            // Incident Category Selection
            Text(
                text = "1. Select Incident / Damage Type",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MinimalTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                incidentOptions.forEach { option ->
                    val isSelected = selectedIncident.title == option.title
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIncident = option },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MinimalPillActive else MinimalSurfaceAlt
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MinimalPrimaryGreen else MinimalCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MinimalTextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(percent = 50),
                                color = if (isSelected) MinimalPrimaryGreen else MinimalPillBg
                            ) {
                                Text(
                                    text = "₹${option.suggestedAmount}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) PureWhite else MinimalTextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description Box
            Text(
                text = "2. Incident Notes / Parking Spot Details",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MinimalTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = incidentDescription,
                onValueChange = { incidentDescription = it },
                placeholder = { Text("e.g. Parked near Pillar 4 West Gate, noticed scratch on side guard upon return", color = MinimalTextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MinimalPrimaryGreen,
                    unfocusedBorderColor = MinimalCardBorder,
                    focusedTextColor = MinimalTextPrimary,
                    unfocusedTextColor = MinimalTextPrimary,
                    cursorColor = MinimalPrimaryGreen,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Photo / Baseline Condition Match Box
            Text(
                text = "3. Baseline Inspection Photo Proof",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MinimalTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MinimalPillActive,
                border = BorderStroke(1.dp, MinimalCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MinimalAccentMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MinimalPrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Baseline Photos Auto-Matched",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MinimalTextPrimary
                        )
                        Text(
                            text = "Timestamped entry photos verified by ParkSure Station Agent",
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalTextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MinimalPrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Payout Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceAlt),
                border = BorderStroke(1.dp, MinimalCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Estimated Claim Settlement",
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalTextSecondary
                        )
                        Text(
                            text = "₹${selectedIncident.suggestedAmount}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MinimalPrimaryGreen
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = MinimalPillActive,
                        border = BorderStroke(1.dp, MinimalCardBorder)
                    ) {
                        Text(
                            text = "Direct UPI / Instant Payout",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MinimalPrimaryGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    val desc = if (incidentDescription.isBlank()) selectedIncident.description else incidentDescription
                    onSubmitClaim(selectedIncident.title, desc, selectedIncident.suggestedAmount)
                    onDismiss()

                    try {
                        val waText = "Hi ParkSure Team, filing claim for Policy ${policy.policyNumber} (${policy.vehicleNumber}) at ${policy.stationName}. Incident: ${selectedIncident.title} (Est: ₹${selectedIncident.suggestedAmount})."
                        val waIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(waText)}")
                        }
                        context.startActivity(waIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Claim filed successfully in app!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_claim_button"),
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MinimalPrimaryGreen,
                    contentColor = PureWhite
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Submit Claim & Dispatch to WhatsApp",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

