package com.example.kotlin_1.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_1.domain.usecase.RecupRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.Result

/**
 * Le ViewModel qui gère la logique de l'écran de la carte.
 * Il expose l'état (EtatRoute) à la vue (MapScreen).
 */

class MaitreCartographe(private val recupererRoute: RecupRoute) : ViewModel() {

    private val _etatDeRoute = MutableStateFlow<EtatRoute>(EtatRoute.Repos)

    val etatDeRoute: StateFlow<EtatRoute> = _etatDeRoute


    fun chercherItineraire(depart: String, arrivee: String) {
        // Lance une coroutine pour les opérations asynchrones
        viewModelScope.launch {
            _etatDeRoute.value = EtatRoute.Chargement

            // Appel au Use Case du Domaine
            val result = recupererRoute(depart, arrivee)
            if (result.isSuccess) {
                _etatDeRoute.value = EtatRoute.Succès(result.getOrThrow())
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Erreur réseau inconnue."
                _etatDeRoute.value = EtatRoute.Erreur(errorMessage)
            }
        }
    }
}