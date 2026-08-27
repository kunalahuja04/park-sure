package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PolicyEntity
import com.example.data.model.PolicyStatus
import com.example.data.model.ScannedItemEntity
import com.example.ui.ParkSureViewModel
import com.example.ui.UiEvent
import com.example.ui.components.ClaimFilingBottomSheet
import com.example.ui.components.PolicyQrPassDialog
import com.example.ui.components.ScanResultBottomSheet
import com.example.ui.history.HistoryScreen
import com.example.ui.insurance.InsuranceScreen
import com.example.ui.policies.PoliciesScreen
import com.example.ui.scan.ScanScreen
import com.example.ui.splash.AnimatedSplashScreen
import com.example.ui.stations.StationsScreen
import com.example.ui.theme.MinimalBg
import com.example.ui.theme.MinimalCardBorder
import com.example.ui.theme.MinimalNavBg
import com.example.ui.theme.MinimalNavBorder
import com.example.ui.theme.MinimalPillActive
import com.example.ui.theme.MinimalPrimaryGreen
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.MinimalTextTertiary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.MyApplicationTheme
import com.example.util.BarcodeEngine
import kotlinx.coroutines.launch

enum class ScreenDestination(val route: String, val title: String, val icon: ImageVector) {
    SCAN("scan", "Scanner", Icons.Default.QrCodeScanner),
    INSURANCE("insurance", "Insure ₹5", Icons.Default.Security),
    POLICIES("policies", "My Passes", Icons.Default.VerifiedUser),
    STATIONS("stations", "Stations", Icons.Default.DirectionsTransit),
    HISTORY("history", "History", Icons.Default.History)
}

class MainActivity : ComponentActivity() {

    private val viewModel: ParkSureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppEntry(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppEntry(viewModel: ParkSureViewModel) {
    var showSplash by remember { mutableStateOf(true) }

    Crossfade(
        targetState = showSplash,
        label = "splash_crossfade"
    ) { isSplash ->
        if (isSplash) {
            AnimatedSplashScreen(
                onSplashFinished = { showSplash = false }
            )
        } else {
            ParkSureMainScaffold(viewModel = viewModel)
        }
    }
}

@Composable
fun ParkSureMainScaffold(viewModel: ParkSureViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentDestination by remember { mutableStateOf(ScreenDestination.SCAN) }

    val activeLookupResult by viewModel.activeLookupResult.collectAsState()
    val rawScannedCode by viewModel.rawScannedCode.collectAsState()
    val selectedPolicy by viewModel.selectedPolicy.collectAsState()
    val isTorchOn by viewModel.isTorchEnabled.collectAsState()
    val allPolicies by viewModel.allPolicies.collectAsState()

    var policyForClaim by remember { mutableStateOf<PolicyEntity?>(null) }

    // Listen for UI events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.PolicyCreated -> {
                    snackbarHostState.showSnackbar("Active pass created for ${event.policy.vehicleNumber}")
                }
                is UiEvent.ClaimSubmitted -> {
                    snackbarHostState.showSnackbar("Claim case #${event.claim.whatsappCaseNumber} logged!")
                }
                is UiEvent.ScanProcessed -> {
                    // bottom sheet is automatically triggered by activeLookupResult
                }
            }
        }
    }

    val activePassCount = allPolicies.count { it.status == PolicyStatus.ACTIVE }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MinimalNavBg,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .testTag("bottom_nav_bar")
            ) {
                ScreenDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            if (destination == ScreenDestination.POLICIES && activePassCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = MinimalPrimaryGreen,
                                            contentColor = PureWhite
                                        ) {
                                            Text(activePassCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MinimalTextPrimary,
                            selectedTextColor = MinimalPrimaryGreen,
                            indicatorColor = MinimalPillActive,
                            unselectedIconColor = MinimalTextTertiary,
                            unselectedTextColor = MinimalTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally { width -> if (targetState.ordinal > initialState.ordinal) width / 3 else -width / 3 })
                        .togetherWith(fadeOut() + slideOutHorizontally { width -> if (targetState.ordinal > initialState.ordinal) -width / 3 else width / 3 })
                },
                label = "screen_navigation"
            ) { screen ->
                when (screen) {
                    ScreenDestination.SCAN -> {
                        ScanScreen(
                            isTorchOn = isTorchOn,
                            onToggleTorch = { viewModel.toggleTorch() },
                            onScanDetected = { code, format ->
                                viewModel.onScanCaptured(code, format)
                            },
                            onNavigateToInsurance = { stationCode ->
                                viewModel.selectedStationCode.value = stationCode
                                currentDestination = ScreenDestination.INSURANCE
                            }
                        )
                    }

                    ScreenDestination.INSURANCE -> {
                        InsuranceScreen(
                            viewModel = viewModel,
                            onNavigateToScanner = { currentDestination = ScreenDestination.SCAN },
                            onPolicyCreated = { currentDestination = ScreenDestination.POLICIES }
                        )
                    }

                    ScreenDestination.POLICIES -> {
                        PoliciesScreen(
                            viewModel = viewModel,
                            onViewPolicyQr = { policy -> viewModel.setSelectedPolicy(policy) },
                            onFileClaim = { policy -> policyForClaim = policy },
                            onNewPassClicked = { currentDestination = ScreenDestination.INSURANCE }
                        )
                    }

                    ScreenDestination.STATIONS -> {
                        StationsScreen(
                            viewModel = viewModel,
                            onSelectStationForInsurance = { stationCode ->
                                viewModel.selectedStationCode.value = stationCode
                                currentDestination = ScreenDestination.INSURANCE
                            },
                            onScanStationQr = { stationCode ->
                                viewModel.selectedStationCode.value = stationCode
                                currentDestination = ScreenDestination.SCAN
                            }
                        )
                    }

                    ScreenDestination.HISTORY -> {
                        HistoryScreen(
                            viewModel = viewModel,
                            onItemClicked = { item ->
                                val parsed = BarcodeEngine.parseCode(item.rawCode, item.format)
                                viewModel.onScanCaptured(item.rawCode, item.format)
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet: Scanned Result Details & Actions
    activeLookupResult?.let { result ->
        ScanResultBottomSheet(
            result = result,
            rawCode = rawScannedCode,
            onDismiss = { viewModel.dismissLookupModal() },
            onNavigateToInsurance = { stationCode ->
                viewModel.selectedStationCode.value = stationCode
                currentDestination = ScreenDestination.INSURANCE
            },
            onNavigateToPolicies = {
                currentDestination = ScreenDestination.POLICIES
            }
        )
    }

    // Modal Dialog: Policy Digital QR Pass
    selectedPolicy?.let { policy ->
        PolicyQrPassDialog(
            policy = policy,
            onDismiss = { viewModel.setSelectedPolicy(null) },
            onFileClaim = { pol ->
                viewModel.setSelectedPolicy(null)
                policyForClaim = pol
            }
        )
    }

    // Modal Sheet: Claim Filing Concierge
    policyForClaim?.let { policy ->
        ClaimFilingBottomSheet(
            policy = policy,
            onDismiss = { policyForClaim = null },
            onSubmitClaim = { incidentType, desc, amount ->
                viewModel.fileClaim(policy.policyId, incidentType, desc, amount)
            }
        )
    }
}
