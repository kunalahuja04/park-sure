package com.example.ui.stations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.StationEntity
import com.example.ui.ParkSureViewModel
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

@Composable
fun StationsScreen(
    viewModel: ParkSureViewModel,
    onSelectStationForInsurance: (stationCode: String) -> Unit,
    onScanStationQr: (stationCode: String) -> Unit
) {
    val stations by viewModel.stations.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLineFilter by remember { mutableStateOf("All") }

    val lineFilters = listOf("All", "Western Line", "Central Line", "Harbour Line")

    val filteredStations = stations.filter { station ->
        val matchesSearch = station.name.contains(searchQuery, ignoreCase = true) ||
                station.zoneName.contains(searchQuery, ignoreCase = true) ||
                station.code.contains(searchQuery, ignoreCase = true)

        val matchesLine = when (selectedLineFilter) {
            "All" -> true
            "Western Line" -> station.line.contains("Western", ignoreCase = true)
            "Central Line" -> station.line.contains("Central", ignoreCase = true)
            "Harbour Line" -> station.line.contains("Harbour", ignoreCase = true)
            else -> true
        }

        matchesSearch && matchesLine
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalBg)
            .testTag("stations_screen_root")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = "PARKING STATIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MinimalTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Mumbai Rail Parking",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MinimalTextPrimary
            )
            Text(
                text = "Live spot availability & ParkSure QR zones",
                style = MaterialTheme.typography.bodySmall,
                color = MinimalTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search station (e.g. Dadar, Andheri, CSMT)", color = MinimalTextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MinimalPrimaryGreen
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_station_input"),
                singleLine = true,
                shape = RoundedCornerShape(percent = 50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MinimalCardBg,
                    unfocusedContainerColor = MinimalCardBg,
                    focusedBorderColor = MinimalPrimaryGreen,
                    unfocusedBorderColor = MinimalCardBorder,
                    focusedTextColor = MinimalTextPrimary,
                    unfocusedTextColor = MinimalTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Line Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lineFilters) { filter ->
                    val isSelected = selectedLineFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedLineFilter = filter },
                        label = {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        shape = RoundedCornerShape(percent = 50),
                        border = BorderStroke(1.dp, if (isSelected) MinimalPrimaryGreen else MinimalCardBorder),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MinimalPillActive,
                            selectedLabelColor = MinimalPrimaryGreen,
                            containerColor = MinimalCardBg,
                            labelColor = MinimalTextSecondary
                        )
                    )
                }
            }
        }

        // Stations List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredStations) { station ->
                StationItemCard(
                    station = station,
                    onInsureClick = { onSelectStationForInsurance(station.code) },
                    onScanQrClick = { onScanStationQr(station.code) }
                )
            }
        }
    }
}

@Composable
fun StationItemCard(
    station: StationEntity,
    onInsureClick: () -> Unit,
    onScanQrClick: () -> Unit
) {
    val occupancyRatio = (station.totalSpots - station.availableSpots).toFloat() / station.totalSpots.toFloat()
    val isAlmostFull = station.availableSpots < (station.totalSpots * 0.15f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalCardBg),
        border = BorderStroke(1.dp, MinimalCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("station_card_${station.code}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Station Name & Line Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsTransit,
                        contentDescription = null,
                        tint = MinimalPrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MinimalTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MinimalPillActive
                ) {
                    Text(
                        text = station.line,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MinimalPrimaryGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = station.zoneName,
                style = MaterialTheme.typography.bodySmall,
                color = MinimalTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Capacity & Availability Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spots Available: ${station.availableSpots} / ${station.totalSpots}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isAlmostFull) Color(0xFFD9534F) else MinimalPrimaryGreen
                )
                Text(
                    text = "₹${station.twoWheelerDailyRate}/day",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MinimalTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { occupancyRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(percent = 50)),
                color = if (isAlmostFull) Color(0xFFD9534F) else MinimalPrimaryGreen,
                trackColor = MinimalSurfaceAlt
            )

            Spacer(modifier = Modifier.height(10.dp))

            // QR Gate Location & Agent
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MinimalTextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "QR Zone: ${station.qrZoneLocation}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MinimalTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onScanQrClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(percent = 50),
                    border = BorderStroke(1.dp, MinimalCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalTextPrimary)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp), tint = MinimalPrimaryGreen)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan QR", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium))
                }

                Button(
                    onClick = onInsureClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MinimalPrimaryGreen,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Insure for ₹5", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

