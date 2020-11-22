package com.sms.durianpay.Data.dataClasses

//kotlin converted data class from json for the callback response from the summary api
data class CovidCasesSummary(
    val Countries: List<Country>,
    val Date: String,
    val Global: Global,
    val Message: String
)