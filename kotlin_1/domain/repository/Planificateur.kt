package com.example.kotlin_1.domain.repository

import com.example.kotlin_1.domain.model.RouteFinale

/**
 * Interface Repository : Définit le contrat pour récupérer un itinéraire.
 * Le reste de l'application ne s'occupe pas de SAVOIR comment c'est fait (API, Cache),
 * seulement de ce que cette fonction promet de fournir.
 */
interface Planificateur {

    suspend fun calculerTrajetPiéton(
        depart: String,
        arrivee: String
    ): Result<RouteFinale>
}