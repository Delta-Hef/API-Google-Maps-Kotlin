package com.example.kotlin_1.data.api

import com.google.gson.annotations.SerializedName


data class DirectionsResponseDTO(
    val routes: List<RouteDTO>
)

data class RouteDTO(
    @SerializedName("overview_polyline")
    val overviewPolyline: PolylineDTO,

    val legs: List<LegDTO>
)

data class PolylineDTO(
    val points: String //  chaîne encodée de coordonnées
)

data class LegDTO(
    val steps: List<StepDTO>,
    val distance: ValueTextDTO
)

data class StepDTO(
    val polyline: PolylineDTO,
    @SerializedName("html_instructions")
    val htmlInstructions: String,
    @SerializedName("start_location")
    val startLocation: LocationDTO,
    @SerializedName("end_location")
    val endLocation: LocationDTO
)

data class LocationDTO(
    val lat: Double,
    val lng: Double
)

data class ValueTextDTO(
    val text: String
)