package com.example.ui.insurance

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PassDuration
import com.example.data.model.PlanTier
import com.example.data.model.StationEntity
import com.example.ui.ParkSureViewModel
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

@Composable
fun InsuranceScreen(
    viewModel: ParkSureViewModel,
    onNavigateToScanner: () -> Unit,
    onPolicyCreated: () -> Unit
) {
    val stations by viewModel.stations.collectAsState()
    val selectedStationCode by viewModel.selectedStationCode.collectAsState()
    val selectedPlanTier by viewModel.selectedPlanTier.collectAsState()
    val selectedDuration by viewModel.selectedDuration.collectAsState()

    val vehicleNumber by viewModel.vehicleNumberInput.collectAsState()
    val vehicleModel by viewModel.vehicleModelInput.collectAsState()
    val userPhone by viewModel.userPhoneInput.collectAsState()
    val isProcessing by viewModel.isProcessingPolicy.collectAsState()

    val selectedStation = stations.find { it.code == selectedStationCode } ?: stations.firstOrNull()
    var isStationDropdownExpanded by remember { mutableStateOf(false) }

    val rawTotal = selectedPlanTier.pricePerDay * selectedDuration.days
    val discount = if (selectedDuration.discountPercent > 0) (rawTotal * selectedDuration.discountPercent / 100) else 0
    val finalTotal = rawTotal - discount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp, bottom = 100.dp)
            .testTag("insurance_screen_root")
    ) {
        // Hero Header Banner (Clean Minimalism Card)
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MinimalCardBg),
            border = BorderStroke(1.dp, MinimalCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MUMBAI TWO-WHEELER TRANSIT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MinimalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Instant Parking Pass",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MinimalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Protect vehicle & helmet for ₹5/day at suburban lots.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary
                    )
                }

                IconButton(
                    onClick = onNavigateToScanner,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MinimalPillActive)
                        .border(1.dp, MinimalCardBorder, CircleShape)
                        .testTag("scan_qr_shortcut_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Station QR",
                        tint = MinimalTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 1: Select Mumbai Station Lot
        Text(
            text = "1. SELECT PARKING LOT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MinimalTextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isStationDropdownExpanded = true }
                    .testTag("station_selector_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalCardBg),
                border = BorderStroke(1.dp, MinimalCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MinimalPillActive,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MinimalPrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = selectedStation?.name ?: "Select Station",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "${selectedStation?.line} • ${selectedStation?.zoneName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalTextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MinimalTextSecondary
                    )
                }
            }

            DropdownMenu(
                expanded = isStationDropdownExpanded,
                onDismissRequest = { isStationDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                stations.forEach { station ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "${station.name} (${station.line})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = station.zoneName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MinimalTextSecondary
                                )
                            }
                        },
                        onClick = {
                            viewModel.selectedStationCode.value = station.code
                            isStationDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 2: Choose Duration
        Text(
            text = "2. PASS DURATION",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MinimalTextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PassDuration.values().forEach { duration ->
                val isSelected = selectedDuration == duration
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectedDuration.value = duration }
                        .testTag("duration_chip_${duration.name}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MinimalPillActive else MinimalCardBg
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MinimalPrimaryGreen else MinimalCardBorder
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = duration.label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MinimalTextPrimary,
                            maxLines = 1
                        )
                        if (duration.discountPercent > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(percent = 50),
                                color = MinimalPrimaryGreen
                            ) {
                                Text(
                                    text = "SAVE ${duration.discountPercent}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = PureWhite,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 3: Choose Plan Tier
        Text(
            text = "3. SELECT PLAN TIER",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MinimalTextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PlanTier.values().forEach { plan ->
                val isSelected = selectedPlanTier == plan
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectedPlanTier.value = plan }
                        .testTag("plan_card_${plan.name}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MinimalSurfaceAlt else MinimalCardBg
                    ),
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) MinimalPrimaryGreen else MinimalCardBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MinimalPrimaryGreen else Color.Transparent)
                                        .border(1.5.dp, if (isSelected) MinimalPrimaryGreen else MinimalCardBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = plan.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MinimalTextPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(percent = 50),
                                        color = MinimalPillActive
                                    ) {
                                        Text(
                                            text = plan.badge,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MinimalPrimaryGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${plan.pricePerDay}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = "per day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MinimalTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = plan.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Theft Limit: ${plan.theftCoverageLimit}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MinimalPrimaryGreen
                            )
                            Text(
                                text = "Damage: ${plan.damageCoverageLimit}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MinimalTextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 4: Vehicle & Contact Info
        Text(
            text = "4. VEHICLE & CONTACT DETAILS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MinimalTextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MinimalCardBg),
            border = BorderStroke(1.dp, MinimalCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { viewModel.vehicleNumberInput.value = it.uppercase() },
                    label = { Text("Vehicle Registration No.") },
                    placeholder = { Text("e.g. MH02EW9821") },
                    leadingIcon = { Icon(Icons.Default.ElectricBike, contentDescription = null, tint = MinimalPrimaryGreen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vehicle_number"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = vehicleModel,
                    onValueChange = { viewModel.vehicleModelInput.value = it },
                    label = { Text("Two-Wheeler Model") },
                    placeholder = { Text("e.g. Honda Activa 6G / TVS Jupiter") },
                    leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = MinimalTextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vehicle_model"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = userPhone,
                    onValueChange = { viewModel.userPhoneInput.value = it },
                    label = { Text("Mobile Number (for WhatsApp Pass)") },
                    placeholder = { Text("+91 98200 87654") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MinimalPrimaryGreen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_user_phone"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Price Summary & Action (Inspired by Clean Minimalism Mint Feature Card)
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MinimalAccentMint),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL MICRO-PASS FEE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = MinimalPrimaryDarkGreen.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "₹$finalTotal",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MinimalPrimaryDarkGreen
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${selectedDuration.label}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MinimalPrimaryDarkGreen
                        )
                        Text(
                            text = "Instant QR Activation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MinimalPrimaryDarkGreen.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.activateInsurance { policy ->
                            onPolicyCreated()
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("activate_insurance_button"),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MinimalPrimaryDarkGreen,
                        contentColor = PureWhite
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Activate Pass for ₹$finalTotal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
