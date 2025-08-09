package com.example.navigator3example.calculations

//Neutral calculation
fun CalculateRice(dryWeight: Float, wetWeight: Float, calibrationWeight: Float): Float = dryWeight / (dryWeight- (wetWeight - calibrationWeight))


//Empirical
fun RiceToPCF(rice: Float): Float = rice * 62.4f

//Metric