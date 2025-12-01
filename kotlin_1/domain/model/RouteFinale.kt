package com.example.kotlin_1.domain.model


data class RouteFinale(
    val pointsDuTracé: List<PointGPS>,
    val étapesDétaillées: List<SegmentEtape>,
    val distanceTotale: String
)