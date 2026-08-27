package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Micro-insurance coverage plan options
 */
enum class PlanTier(
    val title: String,
    val pricePerDay: Int,
    val description: String,
    val theftCoverageLimit: String,
    val damageCoverageLimit: String,
    val includesHelmet: Boolean,
    val includesRoadside: Boolean,
    val badge: String
) {
    BASIC(
        title = "Essential Cover",
        pricePerDay = 5,
        description = "Standard theft & parking damage cover",
        theftCoverageLimit = "₹25,000",
        damageCoverageLimit = "₹5,000",
        includesHelmet = false,
        includesRoadside = false,
        badge = "Most Popular"
    ),
    STANDARD(
        title = "Zero-Dep + Helmet",
        pricePerDay = 10,
        description = "Zero depreciation, scratch/mirror protection & locked helmet cover",
        theftCoverageLimit = "₹50,000",
        damageCoverageLimit = "₹12,000",
        includesHelmet = true,
        includesRoadside = false,
        badge = "Recommended"
    ),
    PRO(
        title = "Pro Commuter 360°",
        pricePerDay = 15,
        description = "Full comprehensive cover + 24/7 flat tyre/battery roadside assist",
        theftCoverageLimit = "₹1,00,000",
        damageCoverageLimit = "₹25,000",
        includesHelmet = true,
        includesRoadside = true,
        badge = "Ultimate Safety"
    )
}

enum class PassDuration(val days: Int, val label: String, val discountPercent: Int) {
    DAILY(1, "1 Day Pass", 0),
    WEEKLY(7, "7-Day Weekly Pack", 15),
    MONTHLY(30, "30-Day Monthly Mini", 25)
}

enum class PolicyStatus {
    ACTIVE,
    EXPIRED,
    CLAIM_IN_PROGRESS,
    SETTLED
}

enum class InspectionStatus(val label: String, val colorHex: Long) {
    PENDING("Agent Inspection in Queue (1 hr)", 0xFFFFB703),
    BASELINE_RECORDED("Baseline Photos Captured", 0xFF00D2C4),
    VERIFIED("Verified by Field Agent", 0xFF06D6A0),
    SELF_INSPECTED("Passive Condition Baseline Active", 0xFF0077B6)
}

@Entity(tableName = "policies")
data class PolicyEntity(
    @PrimaryKey val policyId: String,
    val policyNumber: String,
    val vehicleNumber: String,
    val vehicleModel: String,
    val userPhone: String,
    val stationName: String,
    val stationCode: String,
    val parkingLotName: String,
    val planTier: PlanTier,
    val duration: PassDuration,
    val totalAmount: Int,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val status: PolicyStatus = PolicyStatus.ACTIVE,
    val inspectionStatus: InspectionStatus = InspectionStatus.BASELINE_RECORDED,
    val qrPassCode: String,
    val baselinePhotoUrl: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class ScannedType {
    PARKING_ZONE_QR,
    POLICY_VERIFY_QR,
    PRODUCT_BARCODE,
    VEHICLE_RC_QR,
    FASTAG_BARCODE,
    URL_LINK,
    GENERIC_TEXT
}

@Entity(tableName = "scan_history")
data class ScannedItemEntity(
    @PrimaryKey val id: String,
    val rawCode: String,
    val format: String,
    val type: ScannedType,
    val title: String,
    val subtitle: String,
    val category: String,
    val price: String? = null,
    val details: String,
    val metadataJson: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val actionUrl: String? = null
)

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val code: String,
    val name: String,
    val line: String, // Western, Central, Harbour
    val zoneName: String,
    val twoWheelerDailyRate: Int,
    val totalSpots: Int,
    val availableSpots: Int,
    val qrZoneLocation: String,
    val agentName: String,
    val agentContact: String,
    val isParkSureEnabled: Boolean = true
)

@Entity(tableName = "claims")
data class ClaimEntity(
    @PrimaryKey val claimId: String,
    val policyId: String,
    val policyNumber: String,
    val vehicleNumber: String,
    val stationName: String,
    val incidentType: String, // Mirror theft, Scratch/Dent, Helmet stolen, Vehicle Theft, Vandalism
    val incidentDescription: String,
    val incidentTimeMillis: Long,
    val claimedAmount: Int,
    val approvedAmount: Int? = null,
    val status: String = "Submitted (Under Review)",
    val photoProofAttached: Boolean = true,
    val whatsappCaseNumber: String,
    val timestamp: Long = System.currentTimeMillis()
)
