package com.example.mapboxapp

data class MyLocation(
    val uuid: String,
    val droneName: String,
    val pilotName: String = "Vibhu Tripathi",
    val email: String,
    val lat: Double,
    val lon: Double,
    val alt: Double,
    val rth: Boolean
){
}