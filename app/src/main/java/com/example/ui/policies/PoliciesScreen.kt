package com.example.ui.policies

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClaimEntity
import com.example.data.model.PolicyEntity
import com.example.data.model.PolicyStatus
import com.example.ui.ParkSureViewModel
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
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.MinimalTextTertiary
import com.example.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PoliciesScreen(
    viewModel: ParkSureViewModel,
    onViewPolicyQr: (PolicyEntity) -> Unit,
    onFileClaim: (PolicyEntity) -> Unit,
    onNewPassClicked: () -> Unit
) {
    val context = LocalContext.current
    val allPolicies by viewModel.allPolicies.collectAsState()
    val claims by viewModel.claims.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active Passes (${allPolicies.count { it.status == PolicyStatus.ACTIVE }})", "Claims (${claims.size})", "History")

    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalBg)
            .testTag("policies_screen_root")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PASSES & CLAIMS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MinimalTextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Active Protection",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MinimalTextPrimary
                )
            }

            IconButton(
                onClick = {
                    try {
                        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=919820000000&text=Hi%20ParkSure%20Support"))
                        context.startActivity(waIntent)
                    } catch (e: Exception) {}
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MinimalPillActive)
                    .border(1.dp, MinimalCardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "WhatsApp Support",
                    tint = MinimalPrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MinimalBg,
            contentColor = MinimalPrimaryGreen,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MinimalPrimaryGreen,
                    height = 2.5.dp
                )
            },
            divider = {
                Divider(color = MinimalNavBorder, thickness = 1.dp)
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selectedTabIndex == index) MinimalTextPrimary else MinimalTextSecondary
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    val activeList = allPolicies.filter { it.status == PolicyStatus.ACTIVE }
                    if (activeList.isEmpty()) {
                        item {
                            EmptyPoliciesCard(onNewPassClicked = onNewPassClicked)
                        }
                    } else {
                        items(activeList) { policy ->
                            ActivePolicyCard(
                                policy = policy,
                                onShowQr = { onViewPolicyQr(policy) },
                                onFileClaim = { onFileClaim(policy) },
                                dateFormat = dateFormat
                            )
                        }
                    }
                }

                1 -> {
                    if (claims.isEmpty()) {
                        item {
                            EmptyClaimsCard()
                        }
                    } else {
                        items(claims) { claim ->
                            ClaimItemCard(claim = claim, dateFormat = dateFormat)
                        }
                    }
                }

                2 -> {
                    if (allPolicies.isEmpty()) {
                        item {
                            EmptyPoliciesCard(onNewPassClicked = onNewPassClicked)
                        }
                    } else {
                        items(allPolicies) { policy ->
                            HistoryPolicyCard(
                                policy = policy,
                                onShowQr = { onViewPolicyQr(policy) },
                                dateFormat = dateFormat
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivePolicyCard(
    policy: PolicyEntity,
    onShowQr: () -> Unit,
    onFileClaim: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    val now = System.currentTimeMillis()
    val remainingMillis = (policy.endTimeMillis - now).coerceAtLeast(0L)
    val remainingHours = remainingMillis / (1000 * 60 * 60)
    val remainingMinutes = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalCardBg),
        border = BorderStroke(1.dp, MinimalCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_policy_card_${policy.policyNumber}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top row with status and live countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MinimalPillActive
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MinimalPrimaryGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ACTIVE PASS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MinimalPrimaryGreen
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MinimalSurfaceAlt
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MinimalTextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${remainingHours}h ${remainingMinutes}m left",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MinimalTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Vehicle number & Plan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = policy.vehicleNumber,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MinimalTextPrimary
                    )
                    Text(
                        text = policy.vehicleModel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MinimalPillActive
                ) {
                    Text(
                        text = policy.planTier.title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MinimalPrimaryGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Station info
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

            // Passive Inspection Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MinimalPrimaryGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${policy.inspectionStatus.label} • Pass ${policy.policyNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MinimalTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MinimalCardBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onShowQr,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp)
                        .testTag("show_qr_pass_button"),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MinimalPrimaryGreen,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("QR Gate Pass", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = onFileClaim,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(44.dp)
                        .testTag("file_claim_card_button"),
                    shape = RoundedCornerShape(percent = 50),
                    border = BorderStroke(1.dp, MinimalCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalTextPrimary)
                ) {
                    Text("File Claim", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium))
                }
            }
        }
    }
}

@Composable
fun ClaimItemCard(
    claim: ClaimEntity,
    dateFormat: SimpleDateFormat
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalCardBg),
        border = BorderStroke(1.dp, MinimalCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = claim.incidentType,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MinimalTextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MinimalPillActive
                ) {
                    Text(
                        text = "₹${claim.claimedAmount}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MinimalPrimaryGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Vehicle: ${claim.vehicleNumber} • ${claim.stationName}",
                style = MaterialTheme.typography.bodySmall,
                color = MinimalTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = claim.incidentDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MinimalTextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WhatsApp: ${claim.whatsappCaseNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MinimalTextSecondary
                )
                Text(
                    text = claim.status,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MinimalPrimaryGreen
                )
            }
        }
    }
}

@Composable
fun HistoryPolicyCard(
    policy: PolicyEntity,
    onShowQr: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalCardBg),
        border = BorderStroke(1.dp, MinimalCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onShowQr)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = policy.vehicleNumber,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MinimalTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = MinimalPillActive
                    ) {
                        Text(
                            text = policy.status.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MinimalPrimaryGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${policy.stationName} • ₹${policy.totalAmount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MinimalTextSecondary
                )
                Text(
                    text = "Valid: ${dateFormat.format(Date(policy.startTimeMillis))} - ${dateFormat.format(Date(policy.endTimeMillis))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MinimalTextSecondary.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = "View QR",
                tint = MinimalPrimaryGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EmptyPoliciesCard(onNewPassClicked: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalCardBg),
        border = BorderStroke(1.dp, MinimalCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MinimalPrimaryGreen,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Active Parking Pass",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MinimalTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Scan the QR code at your Mumbai station parking exit or activate an instant ₹5 pass below.",
                style = MaterialTheme.typography.bodySmall,
                color = MinimalTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNewPassClicked,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MinimalPrimaryGreen,
                    contentColor = PureWhite
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Get ₹5 Micro-Insurance Pass")
            }
        }
    }
}

@Composable
fun EmptyClaimsCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalCardBg),
        border = BorderStroke(1.dp, MinimalCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MinimalPrimaryGreen,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Active Claims",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MinimalTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Your parked two-wheelers are protected. In case of theft or scratch, you can file a 60-second claim with baseline photos.",
                style = MaterialTheme.typography.bodySmall,
                color = MinimalTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

