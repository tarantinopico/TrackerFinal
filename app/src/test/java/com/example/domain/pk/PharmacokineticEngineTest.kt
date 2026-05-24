package com.example.domain.pk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PharmacokineticEngineTest {

    @Test
    fun testEliminationConstantCalculation() {
        // ke = ln(2) / 6.0
        val ke = PharmacokineticEngine.calculateEliminationConstant(6.0)
        assertEquals(0.115524, ke, 0.0001)

        val keZero = PharmacokineticEngine.calculateEliminationConstant(0.0)
        assertEquals(0.0, keZero, 0.0001)
    }

    @Test
    fun testTMaxCalculation() {
        val ka = 1.5
        val ke = 0.1155
        val tmax = PharmacokineticEngine.calculateTMax(ka, ke)
        
        // ln(1.5 / 0.1155) / (1.5 - 0.1155) = ln(12.98) / 1.3845 = 2.56 / 1.3845 = 1.85 
        assertEquals(1.85, tmax, 0.05)
    }
    
    @Test
    fun testTMaxEdgeCaseKaEqualsKe() {
        // L'Hopital's limit test
        val ka = 0.5
        val ke = 0.5
        val tmax = PharmacokineticEngine.calculateTMax(ka, ke)
        
        assertEquals(2.0, tmax, 0.01)
    }

    @Test
    fun testRouteModifiersLogic() {
        val oral = getRouteCorrection(PkRoute.ORAL)
        val inhal = getRouteCorrection(PkRoute.INHALATION)
        val iv = getRouteCorrection(PkRoute.INTRAVENOUS)

        assertTrue("Inhalation absorbs faster than oral", oral.ka < inhal.ka)
        assertTrue("IV has perfect bioavailability", iv.bioavailabilityF == 1.0)
        assertTrue("Oral has lower bioavailability than IV", oral.bioavailabilityF < iv.bioavailabilityF)
    }

    @Test
    fun testConcentrationCurve() {
        val ka = 1.5
        val ke = 0.1155
        
        val params = PkCompoundParams(
            compoundId = "cmp_test",
            doseAmount = 100.0,
            ka = ka,
            ke = ke,
            bioavailability = 0.5,
            timestampMs = 0L // t=0
        )
        val userData = PkUserData(weightKg = 70.0, age = 30, metabolismFactor = 1.0)
        
        val cAt0 = PharmacokineticEngine.calculateConcentrationAtTime(0L, params, userData)
        assertEquals("Concentration at ingestion is 0", 0.0, cAt0, 0.001)
        
        val tMaxMs = (1.85 * 3600000).toLong()
        val cAtTmax = PharmacokineticEngine.calculateConcentrationAtTime(tMaxMs, params, userData)
        
        assertTrue("Concentration at Tmax must be positive", cAtTmax > 0.0)
        
        // 12 hours later
        val t12Ms = 12 * 3600000L
        val cAt12h = PharmacokineticEngine.calculateConcentrationAtTime(t12Ms, params, userData)
        
        assertTrue("Concentration drops over time", cAt12h < cAtTmax)
        assertTrue("Concentration remains positive", cAt12h > 0.0)
    }

    @Test
    fun testTimeToClearEstimator() {
        val halfLife = 6.0
        val ke = PharmacokineticEngine.calculateEliminationConstant(halfLife)
        
        val params = PkCompoundParams(
            compoundId = "cmp_test",
            doseAmount = 100.0,
            ka = 1.5,
            ke = ke,
            bioavailability = 0.5,
            timestampMs = 0L
        )
        val userData = PkUserData(weightKg = 70.0, age = 30, metabolismFactor = 1.0)
        val ttc = TimeToClearCalculator.estimateTimeToClear(params, userData, 0.01)
        
        // Roughly ~6 half lives to clear 99%
        assertTrue("Estimated TTC should be approx 30-45h for 6h half-life", ttc in 30.0..45.0)
    }
}
