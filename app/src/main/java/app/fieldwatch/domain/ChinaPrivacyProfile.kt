package app.fieldwatch.domain

/**
 * China-focused evidence fusion for privacy investigations.
 *
 * This module deliberately scores observations instead of naming an attacker,
 * owner, or "government" device. Radio signatures are hypotheses; ownership
 * needs an external, source-backed record.
 */
enum class ChinaPriority {
    PRIMARY,
    SECONDARY,
    LOW,
}

enum class EvidenceLevel {
    LOW,
    MEDIUM,
    HIGH,
}

enum class OwnershipStatus {
    UNKNOWN,
    SOURCE_BACKED,
}

data class PrivacyAssessment(
    val score: Int,
    val level: EvidenceLevel,
    val reasons: List<String>,
)

data class HiddenCameraEvidence(
    val verifiedCameraSignature: Boolean = false,
    val genericVendorOnly: Boolean = false,
    val strongRadio: Boolean = false,
    val repeatedAtSamePlace: Boolean = false,
    val opticalReflection: Boolean = false,
    val cameraServiceOnLocalNetwork: Boolean = false,
)

data class PublicCameraEvidence(
    val verifiedCameraSignature: Boolean = false,
    val visualCameraShape: Boolean = false,
    val infrastructureMount: Boolean = false,
    val knownCameraMapMatch: Boolean = false,
    val ownershipSourcePresent: Boolean = false,
)

data class CellAnomalyEvidence(
    val newCellAtLearnedPlace: Boolean = false,
    val downgradeTo2gOr3g: Boolean = false,
    val unusuallyStrongSignalJump: Boolean = false,
    val shortLivedCell: Boolean = false,
    val unexpectedPlmn: Boolean = false,
    val platformCellularSecurityAlert: Boolean = false,
)

data class PublicCameraAssessment(
    val assessment: PrivacyAssessment,
    val ownership: OwnershipStatus,
)

object ChinaPrivacyProfile {
    /**
     * Existing Fieldwatch signatures that are especially useful in China.
     * These are priority hints only; they do not change what a signature proves.
     */
    private val primaryFleetIds = setOf(
        "fleet-hikvision",
        "fleet-dahua",
        "fleet-uniview",
        "fleet-ezviz",
        "fleet-tapo",
        "fleet-reolink",
        "fleet-dji",
        "fleet-autel",
        "fleet-remote-id",
        "fleet-hoverair",
        "fleet-airtag",
        "fleet-smarttag",
        "fleet-findhub",
        "fleet-tile",
    )

    /**
     * Mostly US-specific public-safety / roadside families. Keep them available,
     * but do not surface them as the default focus in a China profile.
     */
    private val lowPriorityFleetIds = setOf(
        "fleet-flock-cameras",
        "fleet-raven",
        "fleet-axon",
        "fleet-watchguard-video",
        "fleet-digital-ally",
        "fleet-reveal-media",
        "fleet-wolfcom",
    )

    fun priorityForFleet(fleetId: String): ChinaPriority = when (fleetId) {
        in primaryFleetIds -> ChinaPriority.PRIMARY
        in lowPriorityFleetIds -> ChinaPriority.LOW
        else -> ChinaPriority.SECONDARY
    }

    fun hiddenCamera(e: HiddenCameraEvidence): PrivacyAssessment {
        var score = 0
        val reasons = mutableListOf<String>()

        fun add(points: Int, reason: String) {
            score += points
            reasons += reason
        }

        if (e.verifiedCameraSignature) add(35, "camera-specific radio signature")
        if (e.genericVendorOnly) add(5, "generic vendor match")
        if (e.strongRadio) add(10, "strong nearby radio")
        if (e.repeatedAtSamePlace) add(10, "repeated at the same place")
        if (e.opticalReflection) add(25, "camera-like optical reflection")
        if (e.cameraServiceOnLocalNetwork) add(25, "camera-like service on the current local network")

        return assess(score, reasons)
    }

    fun publicCamera(e: PublicCameraEvidence): PublicCameraAssessment {
        var score = 0
        val reasons = mutableListOf<String>()

        fun add(points: Int, reason: String) {
            score += points
            reasons += reason
        }

        if (e.verifiedCameraSignature) add(30, "camera-specific radio signature")
        if (e.visualCameraShape) add(35, "camera-like visual shape")
        if (e.infrastructureMount) add(10, "fixed infrastructure mounting")
        if (e.knownCameraMapMatch) add(25, "matches a known-camera record")

        return PublicCameraAssessment(
            assessment = assess(score, reasons),
            ownership = if (e.ownershipSourcePresent) {
                OwnershipStatus.SOURCE_BACKED
            } else {
                OwnershipStatus.UNKNOWN
            },
        )
    }

    fun cellAnomaly(e: CellAnomalyEvidence): PrivacyAssessment {
        var score = 0
        val reasons = mutableListOf<String>()

        fun add(points: Int, reason: String) {
            score += points
            reasons += reason
        }

        if (e.newCellAtLearnedPlace) add(25, "new serving cell at a learned place")
        if (e.downgradeTo2gOr3g) add(30, "unexpected downgrade to 2G or 3G")
        if (e.unusuallyStrongSignalJump) add(15, "unusually large signal increase")
        if (e.shortLivedCell) add(10, "cell appeared only briefly")
        if (e.unexpectedPlmn) add(20, "unexpected PLMN")
        if (e.platformCellularSecurityAlert) add(35, "platform cellular-security alert")

        return assess(score, reasons)
    }

    private fun assess(rawScore: Int, reasons: List<String>): PrivacyAssessment {
        val score = rawScore.coerceIn(0, 100)
        val level = when {
            score >= 70 -> EvidenceLevel.HIGH
            score >= 40 -> EvidenceLevel.MEDIUM
            else -> EvidenceLevel.LOW
        }
        return PrivacyAssessment(score, level, reasons)
    }
}
