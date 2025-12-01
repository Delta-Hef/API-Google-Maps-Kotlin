package com.example.kotlin_1.domain.usecase

import com.example.kotlin_1.domain.model.RouteFinale
import com.example.kotlin_1.domain.repository.Planificateur

 // point d'entrée pour la logique métier.

class RecupRoute(private val repository: Planificateur) {

    // 'operator fun invoke' permet d'appeler la classe comme une fonction, comrpis !

    suspend operator fun invoke(depart: String, arrivee: String): Result<RouteFinale> {
        //  on délègue directement au Repository.
        return repository.calculerTrajetPiéton(depart, arrivee)
    }
}