package com.example.kotlin_1.presentation.map

import com.example.kotlin_1.domain.model.RouteFinale


// (MVVM State).

sealed class EtatRoute {
    data object Repos : EtatRoute()
    data object Chargement : EtatRoute()
    data class Succès(val route: RouteFinale) : EtatRoute()
    data class Erreur(val message: String) : EtatRoute()
}