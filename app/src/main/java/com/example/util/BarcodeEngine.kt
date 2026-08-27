package com.example.util

import com.example.data.model.ScannedItemEntity
import com.example.data.model.ScannedType
import java.util.UUID

data class ParsedBarcodeResult(
    val type: ScannedType,
    val title: String,
    val subtitle: String,
    val category: String,
    val price: String?,
    val details: String,
    val metadata: Map<String, String>,
    val actionLabel: String,
    val actionPayload: String,
    val isVerified: Boolean = true
)

object BarcodeEngine {

    fun parseCode(raw: String, format: String = "QR_CODE"): ParsedBarcodeResult {
        val trimmed = raw.trim()

        // 1. Station Exit / Parking QR
        if (trimmed.startsWith("PARKSURE:STATION:", ignoreCase = true) ||
            trimmed.contains("parksure.in/lot/", ignoreCase = true) ||
            trimmed.startsWith("STATION_PARKING:", ignoreCase = true)
        ) {
            val stationCode = when {
                trimmed.contains("CSMT", ignoreCase = true) -> "CSMT"
                trimmed.contains("DADAR", ignoreCase = true) -> "DDR"
                trimmed.contains("ANDHERI", ignoreCase = true) -> "ADH"
                trimmed.contains("BANDRA", ignoreCase = true) -> "BA"
                trimmed.contains("THANE", ignoreCase = true) -> "TNA"
                trimmed.contains("BORIVALI", ignoreCase = true) -> "BVI"
                trimmed.contains("GHATKOPAR", ignoreCase = true) -> "GC"
                trimmed.contains("KURLA", ignoreCase = true) -> "CLA"
                else -> "MUM_LOT_01"
            }
            val stationName = when (stationCode) {
                "CSMT" -> "CSMT (Mumbai CST) - West Lot A"
                "DDR" -> "Dadar Central - Platform 1 Exit"
                "ADH" -> "Andheri East - Metro Interchange Lot"
                "BA" -> "Bandra Terminus - East Gate"
                "TNA" -> "Thane Station - Platform 10 West"
                "BVI" -> "Borivali West - Skywalk Bay"
                "GC" -> "Ghatkopar West - Metro Concourse"
                "CLA" -> "Kurla Junction - Nehru Nagar Lot"
                else -> "Mumbai Suburban Station Parking"
            }
            return ParsedBarcodeResult(
                type = ScannedType.PARKING_ZONE_QR,
                title = stationName,
                subtitle = "ParkSure Station QR Gate • Mumbai Suburban",
                category = "Railway Parking Zone",
                price = "₹5 / day micro-insurance",
                details = "Official ParkSure Pay & Park zone. Immediate two-wheeler theft & scratch cover active upon activation.",
                metadata = mapOf(
                    "Station Code" to stationCode,
                    "Line" to if (stationCode in listOf("ADH", "BA", "BVI")) "Western Line" else "Central / Harbour Line",
                    "Daily Parking Fee" to "₹20 (Pay & Park)",
                    "Micro-Insurance Rate" to "₹5/day (Basic) • ₹10/day (Zero-Dep)",
                    "Passive Inspection" to "Agent verified within 60 mins",
                    "QR Exit Tag" to "Authorized Mumbai Railway Gate Post"
                ),
                actionLabel = "Activate ₹5 Insurance",
                actionPayload = stationCode
            )
        }

        // 2. Policy Verification QR Pass
        if (trimmed.startsWith("PARKSURE:POL:", ignoreCase = true) ||
            trimmed.startsWith("POL-", ignoreCase = true) ||
            trimmed.contains("parksure.in/verify/", ignoreCase = true)
        ) {
            val polNum = if (trimmed.startsWith("PARKSURE:POL:")) trimmed.substringAfter("PARKSURE:POL:") else trimmed
            return ParsedBarcodeResult(
                type = ScannedType.POLICY_VERIFY_QR,
                title = "Policy #$polNum",
                subtitle = "ParkSure Micro-Insurance Pass",
                category = "Active Digital Policy",
                price = "Verified Active",
                details = "Two-Wheeler micro-insurance policy is valid and active. Baseline condition photos matched on record.",
                metadata = mapOf(
                    "Policy ID" to polNum,
                    "Status" to "ACTIVE - Theft & Damage Protected",
                    "Claim Limit" to "Up to ₹50,000 Zero-Dep",
                    "Issuer" to "ParkSure Micro-Underwriting Mumbai",
                    "Support" to "Instant WhatsApp Claim 24x7"
                ),
                actionLabel = "View Policy Details",
                actionPayload = polNum
            )
        }

        // 3. Vehicle RC or Fastag Barcode
        if (trimmed.startsWith("RC:", ignoreCase = true) ||
            trimmed.matches(Regex("^[A-Z]{2}[0-9]{2}[A-Z]{1,3}[0-9]{4}$", RegexOption.IGNORE_CASE))
        ) {
            val regNo = trimmed.removePrefix("RC:").uppercase()
            return ParsedBarcodeResult(
                type = ScannedType.VEHICLE_RC_QR,
                title = "Vehicle Reg $regNo",
                subtitle = "Vahan Two-Wheeler Record",
                category = "Vehicle Identity",
                price = "Eligible for ₹5 Micro-Plan",
                details = "Registered two-wheeler in Maharashtra (Mumbai RTO). Valid PUC and standard chassis profile detected.",
                metadata = mapOf(
                    "Reg Number" to regNo,
                    "Vehicle Class" to "M-Cycle / Scooter (2-Wheeler)",
                    "Fuel Type" to "Petrol / EV",
                    "ParkSure Eligibility" to "Instant Approval - No Pre-inspection needed",
                    "RTO Office" to if (regNo.startsWith("MH02")) "MH-02 (Andheri/West)" else "MH-01/03 (Mumbai Central/Wadala)"
                ),
                actionLabel = "Insure This Vehicle",
                actionPayload = regNo
            )
        }

        // 4. Product Barcode Lookups (EAN-13, UPC, Code 128)
        val productMatch = KNOWN_PRODUCT_CATALOG[trimmed]
        if (productMatch != null) {
            return productMatch
        }

        // 5. Generic URL
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            return ParsedBarcodeResult(
                type = ScannedType.URL_LINK,
                title = "Web Link / PWA Portal",
                subtitle = trimmed,
                category = "Web Resource",
                price = null,
                details = "Standard URL payload. Can be opened in web browser or integrated into PWA web onboarding flow.",
                metadata = mapOf(
                    "URL" to trimmed,
                    "Protocol" to if (trimmed.startsWith("https")) "Secure HTTPS" else "HTTP"
                ),
                actionLabel = "Open Web Link",
                actionPayload = trimmed
            )
        }

        // 6. Default Fallback
        return ParsedBarcodeResult(
            type = ScannedType.GENERIC_TEXT,
            title = if (trimmed.length > 24) trimmed.take(24) + "..." else trimmed,
            subtitle = "Barcode Format: $format",
            category = "Scanned Data",
            price = null,
            details = "Scanned code decoded successfully. Raw data: $trimmed",
            metadata = mapOf(
                "Raw Data" to trimmed,
                "Format" to format,
                "Characters" to trimmed.length.toString()
            ),
            actionLabel = "Copy Scanned Data",
            actionPayload = trimmed
        )
    }

    fun toEntity(result: ParsedBarcodeResult, raw: String, format: String): ScannedItemEntity {
        return ScannedItemEntity(
            id = UUID.randomUUID().toString(),
            rawCode = raw,
            format = format,
            type = result.type,
            title = result.title,
            subtitle = result.subtitle,
            category = result.category,
            price = result.price,
            details = result.details,
            metadataJson = result.metadata.entries.joinToString(";") { "${it.key}:${it.value}" },
            timestamp = System.currentTimeMillis()
        )
    }

    // Pre-seeded interactive sample barcodes for instant simulation
    val SAMPLE_BARCODES = listOf(
        SampleCode(
            title = "CSMT West Station Exit QR",
            code = "PARKSURE:STATION:CSMT_WEST:LOT_A",
            format = "QR_CODE",
            category = "Station Parking QR",
            description = "Scan at Mumbai CST station parking exit to activate ₹5 insurance pass"
        ),
        SampleCode(
            title = "Dadar Central Platform 1 QR",
            code = "PARKSURE:STATION:DADAR_CENTRAL:PF1",
            format = "QR_CODE",
            category = "Station Parking QR",
            description = "High commuter density parking lot QR poster"
        ),
        SampleCode(
            title = "Andheri Metro Interchange QR",
            code = "PARKSURE:STATION:ANDHERI_EAST:METRO",
            format = "QR_CODE",
            category = "Station Parking QR",
            description = "Andheri East 2-wheeler pay & park QR gate"
        ),
        SampleCode(
            title = "Steelbird SBA-7 ISI Helmet Barcode",
            code = "8904123456789",
            format = "EAN_13",
            category = "Product Barcode",
            description = "Standard two-wheeler safety gear with ₹500 ParkSure replacement cover"
        ),
        SampleCode(
            title = "Vega Bolt Full-Face Helmet",
            code = "8907654321098",
            format = "EAN_13",
            category = "Product Barcode",
            description = "DOT & ISI certified helmet with lock clip verification"
        ),
        SampleCode(
            title = "Godrej Heavy Heavy-Duty Disc Brake Lock",
            code = "8901234987654",
            format = "CODE_128",
            category = "Anti-Theft Device",
            description = "Tamper-proof disc lock (qualifies for 15% ParkSure claim rebate)"
        ),
        SampleCode(
            title = "Vehicle RC: MH-02-EW-9821 (Activa 6G)",
            code = "RC:MH02EW9821:HONDA_ACTIVA_6G",
            format = "QR_CODE",
            category = "Vehicle Registration",
            description = "Two-wheeler registration barcode for rapid 1-tap micro-insurance"
        ),
        SampleCode(
            title = "Motul C2 Chain Lube Road 400ml",
            code = "3374650239081",
            format = "EAN_13",
            category = "Bike Maintenance",
            description = "High performance chain spray for commuter bikes"
        )
    )

    private val KNOWN_PRODUCT_CATALOG = mapOf(
        "8904123456789" to ParsedBarcodeResult(
            type = ScannedType.PRODUCT_BARCODE,
            title = "Steelbird SBA-7 7Wings ISI Helmet",
            subtitle = "Genuine Certified Two-Wheeler Safety Gear",
            category = "Helmets & Safety",
            price = "₹1,849 (MRP ₹2,199)",
            details = "High impact ABS shell, quick release buckle, scratch resistant visor. Eligible for ParkSure ₹10 Standard Plan helmet theft replacement coverage.",
            metadata = mapOf(
                "Brand" to "Steelbird Hi-Tech",
                "Certification" to "ISI / BIS IS 4151:2015",
                "ParkSure Protection" to "Full ₹1,800 replacement covered in ₹10/day plan",
                "Warranty" to "1 Year Manufacturer Warranty",
                "Authenticity" to "100% Genuine Barcode Verified"
            ),
            actionLabel = "Protect in ₹10 Plan",
            actionPayload = "HELMET:STEELBIRD_SBA7"
        ),
        "8907654321098" to ParsedBarcodeResult(
            type = ScannedType.PRODUCT_BARCODE,
            title = "Vega Bolt Bunny Full Face Helmet",
            subtitle = "ISI & DOT Certified Commuter Helmet",
            category = "Helmets & Safety",
            price = "₹2,050 (MRP ₹2,350)",
            details = "Dual visor mechanism, optical polycarbonate shield, multi-density EPS liner. Complete coverage under ParkSure Zero-Dep plans.",
            metadata = mapOf(
                "Brand" to "Vega Auto",
                "Weight" to "1350 ± 50g",
                "ParkSure Status" to "Eligible for Zero-Dep Addon",
                "Origin" to "Made in India"
            ),
            actionLabel = "Add to Insured Gear",
            actionPayload = "HELMET:VEGA_BOLT"
        ),
        "8901234987654" to ParsedBarcodeResult(
            type = ScannedType.PRODUCT_BARCODE,
            title = "Godrej Nav-Tal Heavy Motorcycle Disc Lock",
            subtitle = "Hardened Steel Anti-Drill Anti-Theft Lock",
            category = "Security & Hardware",
            price = "₹799 (MRP ₹950)",
            details = "10mm hardened steel locking pin, corrosion resistant alloy, anti-pick cylinder. Gives 15% discount on ParkSure annual mini-packs.",
            metadata = mapOf(
                "Brand" to "Godrej Security Solutions",
                "Pin Size" to "10mm Universal fit",
                "Anti-Theft Rating" to "Grade 4 Commuter Protection",
                "Benefit" to "Reduces claim processing time to 15 mins"
            ),
            actionLabel = "Register Device",
            actionPayload = "LOCK:GODREJ_DISC"
        ),
        "3374650239081" to ParsedBarcodeResult(
            type = ScannedType.PRODUCT_BARCODE,
            title = "Motul C2 Chain Lube Road Spray (400ml)",
            subtitle = "Synthetic Motorcycle Chain Maintenance",
            category = "Bike Maintenance",
            price = "₹530 (MRP ₹580)",
            details = "Colorless, highly adhesive spray formulated for O-ring and X-ring chains. Extends chain sprocket life against Mumbai monsoon rust.",
            metadata = mapOf(
                "Brand" to "Motul France / India",
                "Volume" to "400 ml",
                "Application" to "Street, Commuter & Touring 2-Wheelers",
                "Authenticity" to "Original Importer Seal Verified"
            ),
            actionLabel = "Save to Gear List",
            actionPayload = "LUBE:MOTUL_C2"
        )
    )
}

data class SampleCode(
    val title: String,
    val code: String,
    val format: String,
    val category: String,
    val description: String
)
