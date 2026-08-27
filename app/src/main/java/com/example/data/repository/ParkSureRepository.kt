package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.model.ClaimEntity
import com.example.data.model.InspectionStatus
import com.example.data.model.PassDuration
import com.example.data.model.PlanTier
import com.example.data.model.PolicyEntity
import com.example.data.model.PolicyStatus
import com.example.data.model.ScannedItemEntity
import com.example.data.model.ScannedType
import com.example.data.model.StationEntity
import com.example.util.BarcodeEngine
import com.example.util.ParsedBarcodeResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class ParkSureRepository private constructor(context: Context) {

    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "parksure_app.db"
    ).fallbackToDestructiveMigration().build()

    private val policyDao = db.policyDao()
    private val scanDao = db.scanDao()
    private val stationDao = db.stationDao()
    private val claimDao = db.claimDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    // Flows
    fun getPolicies(): Flow<List<PolicyEntity>> = policyDao.getAllPolicies()
    fun getActivePolicies(): Flow<List<PolicyEntity>> = policyDao.getActivePolicies()
    fun getScans(): Flow<List<ScannedItemEntity>> = scanDao.getAllScans()
    fun getFavoriteScans(): Flow<List<ScannedItemEntity>> = scanDao.getFavoriteScans()
    fun getStations(): Flow<List<StationEntity>> = stationDao.getAllStations()
    fun getClaims(): Flow<List<ClaimEntity>> = claimDao.getAllClaims()

    suspend fun getPolicyById(id: String): PolicyEntity? = policyDao.getPolicyById(id)
    suspend fun getStationByCode(code: String): StationEntity? = stationDao.getStationByCode(code)

    suspend fun createPolicy(
        vehicleNumber: String,
        vehicleModel: String,
        userPhone: String,
        stationCode: String,
        planTier: PlanTier,
        duration: PassDuration
    ): PolicyEntity {
        val station = stationDao.getStationByCode(stationCode)
        val stationName = station?.name ?: "Mumbai Suburban Pay & Park"
        val lotName = station?.zoneName ?: "East Gate Two-Wheeler Lot"

        val policyNumber = "PS-MUM-" + (100000..999999).random()
        val policyId = UUID.randomUUID().toString()

        val dailyBase = planTier.pricePerDay
        val rawTotal = dailyBase * duration.days
        val totalAmount = if (duration.discountPercent > 0) {
            rawTotal - (rawTotal * duration.discountPercent / 100)
        } else {
            rawTotal
        }

        val startTime = System.currentTimeMillis()
        val endTime = startTime + (duration.days * 24L * 60L * 60L * 1000L)
        val qrPassCode = "PARKSURE:POL:$policyNumber:$vehicleNumber:$stationCode"

        val policy = PolicyEntity(
            policyId = policyId,
            policyNumber = policyNumber,
            vehicleNumber = vehicleNumber.uppercase(),
            vehicleModel = vehicleModel,
            userPhone = userPhone,
            stationName = stationName,
            stationCode = stationCode,
            parkingLotName = lotName,
            planTier = planTier,
            duration = duration,
            totalAmount = totalAmount,
            startTimeMillis = startTime,
            endTimeMillis = endTime,
            status = PolicyStatus.ACTIVE,
            inspectionStatus = InspectionStatus.BASELINE_RECORDED,
            qrPassCode = qrPassCode,
            createdAtMillis = startTime
        )

        policyDao.insertPolicy(policy)

        // Also record this in scan history as a generated verified pass
        val scanEntity = ScannedItemEntity(
            id = UUID.randomUUID().toString(),
            rawCode = qrPassCode,
            format = "QR_CODE",
            type = ScannedType.POLICY_VERIFY_QR,
            title = "$stationName Pass",
            subtitle = "Active Policy #$policyNumber • $vehicleNumber",
            category = "My Micro-Insurance Pass",
            price = "₹$totalAmount (${duration.label})",
            details = "Active coverage: ${planTier.title}. Theft limit: ${planTier.theftCoverageLimit}, Damage limit: ${planTier.damageCoverageLimit}",
            metadataJson = "Policy ID:$policyNumber;Vehicle:$vehicleNumber;Station:$stationName;Duration:${duration.label};Valid Until:${formatTimestamp(endTime)}",
            timestamp = startTime
        )
        scanDao.insertScan(scanEntity)

        return policy
    }

    suspend fun saveScannedItem(rawCode: String, format: String): ScannedItemEntity {
        val parsed = BarcodeEngine.parseCode(rawCode, format)
        val entity = BarcodeEngine.toEntity(parsed, rawCode, format)
        scanDao.insertScan(entity)
        return entity
    }

    suspend fun deleteScan(id: String) = scanDao.deleteScan(id)
    suspend fun clearAllScans() = scanDao.clearHistory()

    suspend fun toggleFavorite(item: ScannedItemEntity) {
        scanDao.updateScan(item.copy(isFavorite = !item.isFavorite))
    }

    suspend fun fileClaim(
        policyId: String,
        incidentType: String,
        incidentDescription: String,
        claimedAmount: Int
    ): ClaimEntity {
        val policy = policyDao.getPolicyById(policyId)
        val claimId = "CLM-PS-" + (10000..99999).random()
        val whatsappCaseNumber = "WA-CASE-" + (20000..89999).random()

        val claim = ClaimEntity(
            claimId = claimId,
            policyId = policyId,
            policyNumber = policy?.policyNumber ?: "PS-MUM-892144",
            vehicleNumber = policy?.vehicleNumber ?: "MH-02-EW-9821",
            stationName = policy?.stationName ?: "CSMT West Lot A",
            incidentType = incidentType,
            incidentDescription = incidentDescription,
            incidentTimeMillis = System.currentTimeMillis(),
            claimedAmount = claimedAmount,
            approvedAmount = claimedAmount,
            status = "Approved for Quick Settle (WhatsApp)",
            photoProofAttached = true,
            whatsappCaseNumber = whatsappCaseNumber,
            timestamp = System.currentTimeMillis()
        )

        claimDao.insertClaim(claim)
        if (policy != null) {
            policyDao.updatePolicy(policy.copy(status = PolicyStatus.CLAIM_IN_PROGRESS))
        }

        return claim
    }

    private suspend fun seedInitialDataIfNeeded() {
        val mumbaiStations = listOf(
            StationEntity(
                code = "CSMT",
                name = "CSMT (Mumbai CST)",
                line = "Central Line",
                zoneName = "West Gate Lot A (Opp. BMC HQ)",
                twoWheelerDailyRate = 20,
                totalSpots = 850,
                availableSpots = 142,
                qrZoneLocation = "Platform 1 & Main Exit Gate Post #4",
                agentName = "Ramesh Gaikwad",
                agentContact = "+91 98201 44521"
            ),
            StationEntity(
                code = "DDR",
                name = "Dadar Junction",
                line = "Central & Western Line",
                zoneName = "Parel/Kabutar Khana West Lot",
                twoWheelerDailyRate = 25,
                totalSpots = 1200,
                availableSpots = 98,
                qrZoneLocation = "Foot Overbridge 3 Exit & Senapati Bapat Marg",
                agentName = "Pradeep Sawant",
                agentContact = "+91 98332 11094"
            ),
            StationEntity(
                code = "ADH",
                name = "Andheri",
                line = "Western Line",
                zoneName = "East Metro Concourse & Railway Lot",
                twoWheelerDailyRate = 20,
                totalSpots = 1400,
                availableSpots = 310,
                qrZoneLocation = "Auto Stand Exit & Station Deck Pillar 12",
                agentName = "Sanjay Patil",
                agentContact = "+91 97022 55891"
            ),
            StationEntity(
                code = "BA",
                name = "Bandra Terminus & Station",
                line = "Western Line",
                zoneName = "East Terminus Skywalk Bay",
                twoWheelerDailyRate = 20,
                totalSpots = 950,
                availableSpots = 220,
                qrZoneLocation = "East Gate 2 Ticket Counter Post",
                agentName = "Vinod Sharma",
                agentContact = "+91 98199 44320"
            ),
            StationEntity(
                code = "BVI",
                name = "Borivali",
                line = "Western Line",
                zoneName = "West SV Road Multilevel Lot",
                twoWheelerDailyRate = 20,
                totalSpots = 1600,
                availableSpots = 415,
                qrZoneLocation = "Platform 1 North Concourse QR Board",
                agentName = "Anil More",
                agentContact = "+91 98690 77112"
            ),
            StationEntity(
                code = "TNA",
                name = "Thane",
                line = "Central Line",
                zoneName = "West SATIS Bus Deck Lot",
                twoWheelerDailyRate = 25,
                totalSpots = 1800,
                availableSpots = 380,
                qrZoneLocation = "Platform 10 West Gate QR Pillar",
                agentName = "Santosh Kadam",
                agentContact = "+91 98213 66490"
            ),
            StationEntity(
                code = "GC",
                name = "Ghatkopar",
                line = "Central & Metro Line",
                zoneName = "West Metro Skywalk Two-Wheeler Bay",
                twoWheelerDailyRate = 20,
                totalSpots = 1100,
                availableSpots = 195,
                qrZoneLocation = "Metro Gate 3 & Platform 1 Exit",
                agentName = "Mahesh Shinde",
                agentContact = "+91 97690 33811"
            ),
            StationEntity(
                code = "CLA",
                name = "Kurla Junction",
                line = "Harbour & Central Line",
                zoneName = "East Nehru Nagar Pay & Park",
                twoWheelerDailyRate = 15,
                totalSpots = 750,
                availableSpots = 110,
                qrZoneLocation = "East Booking Office Gate Post",
                agentName = "Rajendra Yadav",
                agentContact = "+91 98920 88204"
            ),
            StationEntity(
                code = "VSH",
                name = "Vashi (Navi Mumbai)",
                line = "Harbour Line",
                zoneName = "Station Complex Plaza Lot",
                twoWheelerDailyRate = 20,
                totalSpots = 1300,
                availableSpots = 520,
                qrZoneLocation = "Sector 30 Plaza South Exit",
                agentName = "Sachin Jadhav",
                agentContact = "+91 98205 11987"
            ),
            StationEntity(
                code = "KYN",
                name = "Kalyan Junction",
                line = "Central Line",
                zoneName = "West Platform 1 Main Bay",
                twoWheelerDailyRate = 15,
                totalSpots = 1500,
                availableSpots = 290,
                qrZoneLocation = "West Footbridge 2 Exit Gate",
                agentName = "Vikas Mhatre",
                agentContact = "+91 98670 99412"
            )
        )
        stationDao.insertStations(mumbaiStations)

        // Seed an active demo policy if none exists
        val existingPolicy = policyDao.getPolicyById("demo-policy-1")
        if (existingPolicy == null) {
            val now = System.currentTimeMillis()
            val samplePolicy = PolicyEntity(
                policyId = "demo-policy-1",
                policyNumber = "PS-MUM-482910",
                vehicleNumber = "MH-02-EW-9821",
                vehicleModel = "Honda Activa 6G (Pearl White)",
                userPhone = "+91 98200 87654",
                stationName = "Andheri - East Metro Interchange Lot",
                stationCode = "ADH",
                parkingLotName = "East Metro Concourse & Railway Lot",
                planTier = PlanTier.STANDARD,
                duration = PassDuration.WEEKLY,
                totalAmount = 60,
                startTimeMillis = now - (2L * 24L * 60L * 60L * 1000L),
                endTimeMillis = now + (5L * 24L * 60L * 60L * 1000L),
                status = PolicyStatus.ACTIVE,
                inspectionStatus = InspectionStatus.VERIFIED,
                qrPassCode = "PARKSURE:POL:PS-MUM-482910:MH02EW9821:ADH",
                baselinePhotoUrl = "sample_inspection_andheri.jpg",
                createdAtMillis = now - (2L * 24L * 60L * 60L * 1000L)
            )
            policyDao.insertPolicy(samplePolicy)

            // Seed initial scan items
            val sampleScans = listOf(
                ScannedItemEntity(
                    id = "scan-seed-1",
                    rawCode = "PARKSURE:STATION:CSMT_WEST:LOT_A",
                    format = "QR_CODE",
                    type = ScannedType.PARKING_ZONE_QR,
                    title = "CSMT (Mumbai CST) - West Lot A",
                    subtitle = "ParkSure Station QR Gate • Mumbai Suburban",
                    category = "Railway Parking Zone",
                    price = "₹5 / day micro-insurance",
                    details = "Official ParkSure Pay & Park zone. Immediate two-wheeler theft & scratch cover active upon activation.",
                    metadataJson = "Station Code:CSMT;Line:Central Line;Daily Parking Fee:₹20;Micro-Insurance:₹5/day;Passive Inspection:Agent verified",
                    timestamp = now - 3600000L,
                    isFavorite = true
                ),
                ScannedItemEntity(
                    id = "scan-seed-2",
                    rawCode = "8904123456789",
                    format = "EAN_13",
                    type = ScannedType.PRODUCT_BARCODE,
                    title = "Steelbird SBA-7 7Wings ISI Helmet",
                    subtitle = "Genuine Certified Two-Wheeler Safety Gear",
                    category = "Helmets & Safety",
                    price = "₹1,849 (MRP ₹2,199)",
                    details = "High impact ABS shell, quick release buckle, scratch resistant visor. Eligible for ParkSure ₹10 Standard Plan helmet theft replacement coverage.",
                    metadataJson = "Brand:Steelbird Hi-Tech;Certification:ISI IS 4151;ParkSure Protection:₹1,800 helmet cover;Warranty:1 Year",
                    timestamp = now - 7200000L,
                    isFavorite = true
                )
            )
            sampleScans.forEach { scanDao.insertScan(it) }
        }
    }

    private fun formatTimestamp(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.ENGLISH)
        return sdf.format(java.util.Date(millis))
    }

    companion object {
        @Volatile
        private var INSTANCE: ParkSureRepository? = null

        fun getInstance(context: Context): ParkSureRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ParkSureRepository(context).also { INSTANCE = it }
            }
        }
    }
}
