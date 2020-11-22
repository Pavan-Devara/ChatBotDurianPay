package com.sms.durianpay.Data.dataClasses

data class CountryStats(
    val Aged65Older: Double,
    val Aged70Older: Double,
    val Continent: String,
    val Country: String,
    val CountryISO: String,
    val CvdDeathRate: Double,
    val DiabetesPrevalence: Double,
    val ExtremePoverty: Int,
    val FemaleSmokers: Int,
    val GdpPerCapita: Double,
    val HandwashingFacilities: Double,
    val HospitalBedsPerThousand: Double,
    val LifeExpectancy: Double,
    val MaleSmokers: Int,
    val MedianAge: Double,
    val Population: Int,
    val PopulationDensity: Double
)