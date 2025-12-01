package com.example.kotlin_1.domain.model


/**
 * Décrit un segment de trajet avec ses points de début/fin, l'instruction à suivre
 * et un code couleur pour le rendu personnalisé.
 */
data class SegmentEtape(
    val instruction: String,
    val debut: PointGPS,
    val fin: PointGPS,
    val couleurCode: Int
)