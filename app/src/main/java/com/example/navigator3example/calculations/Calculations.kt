package com.example.navigator3example.calculations

import com.example.navigator3example.navigation.standards.Standard
import kotlin.math.abs

// Neutral calculation with input validation and zero-division protection
fun CalculateRice(dryWeight: Float, wetWeight: Float, calibrationWeight: Float): Float {
    // Validate positive inputs
    if (dryWeight <= 0f) return Float.NaN
    // Denominator: dryWeight - (wetWeight - calibrationWeight)
    val denominator = dryWeight - (wetWeight - calibrationWeight)
    // Avoid division by zero or near-zero and negative/invalid setups
    if (abs(denominator) < 1e-6f) return Float.NaN
    return dryWeight / denominator
}

// Empirical
fun RiceToPCF(rice: Float): Float = rice * 62.4f

// Metric