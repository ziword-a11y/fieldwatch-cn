package app.fieldwatch.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChinaPrivacyProfileTest {
    @Test
    fun chinaPriorityPromotesRelevantExistingSignatures() {
        assertEquals(ChinaPriority.PRIMARY, ChinaPrivacyProfile.priorityForFleet("fleet-hikvision"))
        assertEquals(ChinaPriority.PRIMARY, ChinaPrivacyProfile.priorityForFleet("fleet-dji"))
        assertEquals(ChinaPriority.LOW, ChinaPrivacyProfile.priorityForFleet("fleet-flock-cameras"))
        assertEquals(ChinaPriority.SECONDARY, ChinaPrivacyProfile.priorityForFleet("fleet-gopro"))
    }

    @Test
    fun genericVendorAndStrongSignalDoNotConfirmHiddenCamera() {
        val result = ChinaPrivacyProfile.hiddenCamera(
            HiddenCameraEvidence(
                genericVendorOnly = true,
                strongRadio = true,
            ),
        )
        assertEquals(EvidenceLevel.LOW, result.level)
        assertEquals(15, result.score)
    }

    @Test
    fun multiSensorHiddenCameraEvidenceCanBecomeHigh() {
        val result = ChinaPrivacyProfile.hiddenCamera(
            HiddenCameraEvidence(
                verifiedCameraSignature = true,
                opticalReflection = true,
                cameraServiceOnLocalNetwork = true,
            ),
        )
        assertEquals(EvidenceLevel.HIGH, result.level)
        assertEquals(85, result.score)
    }

    @Test
    fun publicCameraOwnershipIsNeverInferredFromRadioOrAppearance() {
        val result = ChinaPrivacyProfile.publicCamera(
            PublicCameraEvidence(
                verifiedCameraSignature = true,
                visualCameraShape = true,
                infrastructureMount = true,
                knownCameraMapMatch = true,
                ownershipSourcePresent = false,
            ),
        )
        assertEquals(EvidenceLevel.HIGH, result.assessment.level)
        assertEquals(OwnershipStatus.UNKNOWN, result.ownership)
    }

    @Test
    fun ownershipMayBeSourceBackedOnlyWhenARecordExists() {
        val result = ChinaPrivacyProfile.publicCamera(
            PublicCameraEvidence(
                knownCameraMapMatch = true,
                ownershipSourcePresent = true,
            ),
        )
        assertEquals(OwnershipStatus.SOURCE_BACKED, result.ownership)
    }

    @Test
    fun oneNewCellIsOnlyWeakEvidence() {
        val result = ChinaPrivacyProfile.cellAnomaly(
            CellAnomalyEvidence(newCellAtLearnedPlace = true),
        )
        assertEquals(EvidenceLevel.LOW, result.level)
        assertEquals(25, result.score)
    }

    @Test
    fun severalIndependentCellSignalsCanBecomeHigh() {
        val result = ChinaPrivacyProfile.cellAnomaly(
            CellAnomalyEvidence(
                newCellAtLearnedPlace = true,
                downgradeTo2gOr3g = true,
                unusuallyStrongSignalJump = true,
                shortLivedCell = true,
            ),
        )
        assertEquals(EvidenceLevel.HIGH, result.level)
        assertTrue(result.score >= 70)
    }
}
