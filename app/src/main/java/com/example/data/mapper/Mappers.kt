package com.example.data.mapper
import org.json.JSONArray
import org.json.JSONObject

import com.example.data.local.entities.*
import com.example.domain.model.*
import com.example.data.local.db.Converters

fun SubstanceEntity.toDomain() = Substance(
    id = id, name = name, alias = alias,
    category = try { SubstanceCategory.valueOf(category) } catch (e: Exception) { SubstanceCategory.OTHER },
    iconKey = iconKey, colorHex = colorHex, defaultUnit = defaultUnit, active = active, notes = notes,
    archivedAt = archivedAt, createdAt = createdAt, updatedAt = updatedAt
)

fun Substance.toEntity() = SubstanceEntity(
    id = id, name = name, alias = alias, category = category.name, iconKey = iconKey,
    colorHex = colorHex, defaultUnit = defaultUnit, active = active, notes = notes, archivedAt = archivedAt,
    createdAt = createdAt, updatedAt = updatedAt
)

fun CompoundEntity.toDomain(): Compound {
    val curveList = mutableListOf<CurvePoint>()
    try {
        val arr = JSONArray(curve)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            curveList.add(CurvePoint(obj.getInt("t"), obj.getDouble("c").toFloat()))
        }
    } catch(e: Exception) { e.printStackTrace() }
    
    return Compound(
        id = id, substanceId = substanceId, name = name, halfLifeHours = halfLifeHours,
        onsetMin = onsetMin, peakMin = peakMin, durationHours = durationHours,
        thresholdDose = thresholdDose, commonDose = commonDose, strongDose = strongDose,
        molecularWeight = molecularWeight, potencyMultiplier = potencyMultiplier, colorHex = colorHex, active = active,
        useCurve = useCurve, curve = curveList,
        createdAt = createdAt, updatedAt = updatedAt
    )
}

fun Compound.toEntity(): CompoundEntity {
    val arr = JSONArray()
    for (pt in curve) {
        val obj = JSONObject()
        obj.put("t", pt.t)
        obj.put("c", pt.c)
        arr.put(obj)
    }
    return CompoundEntity(
        id = id, substanceId = substanceId, name = name, halfLifeHours = halfLifeHours,
        onsetMin = onsetMin, peakMin = peakMin, durationHours = durationHours,
        thresholdDose = thresholdDose, commonDose = commonDose, strongDose = strongDose,
        molecularWeight = molecularWeight, potencyMultiplier = potencyMultiplier, colorHex = colorHex, active = active,
        useCurve = useCurve, curve = arr.toString(),
        createdAt = createdAt, updatedAt = updatedAt
    )
}

fun VariantEntity.toDomain(): Variant {
    val converters = Converters()
    return Variant(
        id = id, substanceId = substanceId, name = name, colorHex = colorHex,
        pricePerUnit = pricePerUnit, unitLabel = unitLabel,
        ratio = converters.toStringDoubleMap(ratioJson),
        roaDefault = roaDefault, active = active, createdAt = createdAt, updatedAt = updatedAt
    )
}

fun Variant.toEntity(): VariantEntity {
    val converters = Converters()
    return VariantEntity(
        id = id, substanceId = substanceId, name = name, colorHex = colorHex,
        pricePerUnit = pricePerUnit, unitLabel = unitLabel,
        ratioJson = converters.fromStringDoubleMap(ratio),
        roaDefault = roaDefault, active = active, createdAt = createdAt, updatedAt = updatedAt
    )
}

fun DoseEntity.toDomain() = Dose(
    id = id, substanceId = substanceId, variantId = variantId, doseAmount = doseAmount,
    unit = unit, route = route, price = price, timestamp = timestamp, notes = notes,
    createdAt = createdAt, updatedAt = updatedAt
)

fun Dose.toEntity() = DoseEntity(
    id = id, substanceId = substanceId, variantId = variantId, doseAmount = doseAmount,
    unit = unit, route = route, price = price, timestamp = timestamp, notes = notes,
    createdAt = createdAt, updatedAt = updatedAt
)

fun QuickDoseEntity.toDomain() = QuickDose(
    id = id, substanceId = substanceId, variantId = variantId, label = label,
    defaultAmount = defaultAmount, defaultUnit = defaultUnit, defaultRoute = defaultRoute,
    defaultPrice = defaultPrice, pinned = pinned, orderIndex = orderIndex,
    createdAt = createdAt, updatedAt = updatedAt
)

fun QuickDose.toEntity() = QuickDoseEntity(
    id = id, substanceId = substanceId, variantId = variantId, label = label,
    defaultAmount = defaultAmount, defaultUnit = defaultUnit, defaultRoute = defaultRoute,
    defaultPrice = defaultPrice, pinned = pinned, orderIndex = orderIndex,
    createdAt = createdAt, updatedAt = updatedAt
)

fun SettingsEntity.toDomain() = AppSettings(
    id = id, userWeightKg = userWeightKg, userAge = userAge, metabolismFactor = metabolismFactor,
    themeMode = themeMode, accentPalette = accentPalette, privacyMode = privacyMode,
    financeMode = financeMode, warningsEnabled = warningsEnabled, compactMode = compactMode,
    hideFinanceMode = hideFinanceMode, firstDayOfWeek = firstDayOfWeek, currency = currency,
    defaultRoute = defaultRoute, defaultUnit = defaultUnit, createdAt = createdAt, updatedAt = updatedAt
)

fun AppSettings.toEntity() = SettingsEntity(
    id = id, userWeightKg = userWeightKg, userAge = userAge, metabolismFactor = metabolismFactor,
    themeMode = themeMode, accentPalette = accentPalette, privacyMode = privacyMode,
    financeMode = financeMode, warningsEnabled = warningsEnabled, compactMode = compactMode,
    hideFinanceMode = hideFinanceMode, firstDayOfWeek = firstDayOfWeek, currency = currency,
    defaultRoute = defaultRoute, defaultUnit = defaultUnit, createdAt = createdAt, updatedAt = updatedAt
)
