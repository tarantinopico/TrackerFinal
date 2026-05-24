package com.example.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PharmacokineticEngineTest {

    @Test
    fun `test exact unit conversions`() {
        // mg -> g
        assertEquals(0.001, UnitConverter.convert(1.0, MassUnit.MG, MassUnit.G), 0.000001)
        // g -> mg
        assertEquals(1500.0, UnitConverter.convert(1.5, MassUnit.G, MassUnit.MG), 0.000001)
        // µg -> mg
        assertEquals(0.005, UnitConverter.convert(5.0, MassUnit.MCG, MassUnit.MG), 0.000001)
        // ng -> mg (5000 ng = 5 µg = 0.005 mg)
        assertEquals(0.005, UnitConverter.convert(5000.0, MassUnit.NG, MassUnit.MG), 0.0000001)
    }

    @Test
    fun `test percentage composition with high precision`() {
        val substance = Substance(id = "s1", name = "S", category = SubstanceCategory.SUPPLEMENT)
        val compoundA = Compound(id = "a", substanceId = "s1", name = "A", potencyMultiplier = 1.0)
        val compoundB = Compound(id = "b", substanceId = "s1", name = "B", potencyMultiplier = 0.5)
        
        // 0.001% precision -> means 0.00001 in ratio
        val ratioMap = mapOf(
            "a" to 0.99999, // 99.999%
            "b" to 0.00001  // 0.001%
        )
        
        val variant = Variant(id = "v1", substanceId = "s1", name = "Precision", ratio = ratioMap)
        val dose = Dose(id = "d1", substanceId = "s1", variantId = "v1", doseAmount = 100f, route = "Oral", timestamp = 0L)
        
        // Calculate load exactly at peak (timeNow = 30min default = 0.5h) for compound A
        val loads = PharmacokineticEngine.calculateCurrentLoad(dose, substance, listOf(compoundA, compoundB), variant, 30L * 60 * 1000)
        
        val loadA = loads.find { it.compoundId == "a" }?.concentration ?: 0.0
        val loadB = loads.find { it.compoundId == "b" }?.concentration ?: 0.0
        
        // 100 * 0.99999 * 1.0 = 99.999 -> at peak (0.5 hr) it should be exactly 99.999
        assertEquals(99.999, loadA, 0.0001)
        
        // 100 * 0.00001 * 0.5 potency = 0.0005
        assertEquals(0.0005, loadB, 0.00001)
    }

    @Test
    fun `test empty ratio falls back to equal split`() {
        val substance = Substance(id = "s1", name = "S", category = SubstanceCategory.SUPPLEMENT)
        val compoundA = Compound(id = "a", substanceId = "s1", name = "A", potencyMultiplier = 1.0)
        val compoundB = Compound(id = "b", substanceId = "s1", name = "B", potencyMultiplier = 1.0)
        
        val variantEmptyRatio = Variant(id = "v1", substanceId = "s1", name = "Empty", ratio = emptyMap())
        val dose = Dose(id = "d1", substanceId = "s1", variantId = "v1", doseAmount = 100f, route = "Oral", timestamp = 0L)
        
        val loads = PharmacokineticEngine.calculateCurrentLoad(dose, substance, listOf(compoundA, compoundB), variantEmptyRatio, 30L * 60 * 1000)
        
        // Should split 50/50 -> 50 mg each
        assertEquals(50.0, loads.find { it.compoundId == "a" }?.concentration ?: 0.0, 0.01)
        assertEquals(50.0, loads.find { it.compoundId == "b" }?.concentration ?: 0.0, 0.01)
    }
    
    @Test
    fun `test zero dose edge case`() {
         val substance = Substance(id = "s1", name = "S", category = SubstanceCategory.SUPPLEMENT)
         val compoundA = Compound(id = "a", substanceId = "s1", name = "A")
         val dose = Dose(id = "d1", substanceId = "s1", variantId = "v1", doseAmount = 0f, route = "Oral", timestamp = 0L)
         val loads = PharmacokineticEngine.calculateCurrentLoad(dose, substance, listOf(compoundA), null, 30L * 60 * 1000)
         
         assertEquals(0.0, loads.first().concentration, 0.0)
    }
}
