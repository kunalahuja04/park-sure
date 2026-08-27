package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClaimEntity
import com.example.data.model.PassDuration
import com.example.data.model.PlanTier
import com.example.data.model.PolicyEntity
import com.example.data.model.ScannedItemEntity
import com.example.data.model.StationEntity
import com.example.data.repository.ParkSureRepository
import com.example.util.BarcodeEngine
import com.example.util.ParsedBarcodeResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class PolicyCreated(val policy: PolicyEntity) : UiEvent()
    data class ClaimSubmitted(val claim: ClaimEntity) : UiEvent()
    data class ScanProcessed(val result: ParsedBarcodeResult) : UiEvent()
}

class ParkSureViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ParkSureRepository.getInstance(application)

    val allPolicies: StateFlow<List<PolicyEntity>> = repository.getPolicies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePolicies: StateFlow<List<PolicyEntity>> = repository.getActivePolicies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scanHistory: StateFlow<List<ScannedItemEntity>> = repository.getScans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stations: StateFlow<List<StationEntity>> = repository.getStations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val claims: StateFlow<List<ClaimEntity>> = repository.getClaims()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Scanned Item Modal
    private val _activeLookupResult = MutableStateFlow<ParsedBarcodeResult?>(null)
    val activeLookupResult: StateFlow<ParsedBarcodeResult?> = _activeLookupResult.asStateFlow()

    private val _rawScannedCode = MutableStateFlow<String>("")
    val rawScannedCode: StateFlow<String> = _rawScannedCode.asStateFlow()

    // Selected policy for QR pass viewing
    private val _selectedPolicy = MutableStateFlow<PolicyEntity?>(null)
    val selectedPolicy: StateFlow<PolicyEntity?> = _selectedPolicy.asStateFlow()

    // Insurance Form State
    var vehicleNumberInput = MutableStateFlow("MH02EW9821")
    var vehicleModelInput = MutableStateFlow("Honda Activa 6G")
    var userPhoneInput = MutableStateFlow("+91 98200 87654")
    var selectedStationCode = MutableStateFlow("CSMT")
    var selectedPlanTier = MutableStateFlow(PlanTier.BASIC)
    var selectedDuration = MutableStateFlow(PassDuration.DAILY)
    var isProcessingPolicy = MutableStateFlow(false)

    // Scanner Controls
    var isTorchEnabled = MutableStateFlow(false)
    var isScanningActive = MutableStateFlow(true)

    // Events
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    fun onScanCaptured(code: String, format: String = "QR_CODE") {
        if (code.isBlank()) return
        viewModelScope.launch {
            _rawScannedCode.value = code
            val parsed = BarcodeEngine.parseCode(code, format)
            _activeLookupResult.value = parsed
            repository.saveScannedItem(code, format)
            _events.emit(UiEvent.ScanProcessed(parsed))

            // If it's a station QR code, automatically pre-select that station for insurance
            if (parsed.metadata.containsKey("Station Code")) {
                val stationCode = parsed.metadata["Station Code"] ?: ""
                if (stationCode.isNotBlank()) {
                    selectedStationCode.value = stationCode
                }
            }
        }
    }

    fun dismissLookupModal() {
        _activeLookupResult.value = null
    }

    fun setSelectedPolicy(policy: PolicyEntity?) {
        _selectedPolicy.value = policy
    }

    fun toggleTorch() {
        isTorchEnabled.value = !isTorchEnabled.value
    }

    fun toggleFavorite(item: ScannedItemEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(item)
        }
    }

    fun deleteScan(id: String) {
        viewModelScope.launch {
            repository.deleteScan(id)
            _events.emit(UiEvent.ShowToast("Scan removed from history"))
        }
    }

    fun clearAllScans() {
        viewModelScope.launch {
            repository.clearAllScans()
            _events.emit(UiEvent.ShowToast("Scan history cleared"))
        }
    }

    fun activateInsurance(
        vehicleNo: String = vehicleNumberInput.value,
        model: String = vehicleModelInput.value,
        phone: String = userPhoneInput.value,
        stationCode: String = selectedStationCode.value,
        tier: PlanTier = selectedPlanTier.value,
        duration: PassDuration = selectedDuration.value,
        onSuccess: (PolicyEntity) -> Unit = {}
    ) {
        if (vehicleNo.isBlank()) {
            viewModelScope.launch { _events.emit(UiEvent.ShowToast("Please enter vehicle registration number")) }
            return
        }
        viewModelScope.launch {
            isProcessingPolicy.value = true
            val policy = repository.createPolicy(
                vehicleNumber = vehicleNo,
                vehicleModel = if (model.isBlank()) "Two-Wheeler" else model,
                userPhone = if (phone.isBlank()) "+91 98000 00000" else phone,
                stationCode = stationCode,
                planTier = tier,
                duration = duration
            )
            isProcessingPolicy.value = false
            _selectedPolicy.value = policy
            _events.emit(UiEvent.PolicyCreated(policy))
            _events.emit(UiEvent.ShowToast("ParkSure Pass Generated for ${policy.vehicleNumber}"))
            onSuccess(policy)
        }
    }

    fun fileClaim(
        policyId: String,
        incidentType: String,
        description: String,
        amount: Int,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val claim = repository.fileClaim(
                policyId = policyId,
                incidentType = incidentType,
                incidentDescription = description,
                claimedAmount = amount
            )
            _events.emit(UiEvent.ClaimSubmitted(claim))
            _events.emit(UiEvent.ShowToast("Claim registered! WhatsApp case: ${claim.whatsappCaseNumber}"))
            onSuccess()
        }
    }
}
