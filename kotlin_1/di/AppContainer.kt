package com.example.kotlin_1.di


import com.example.kotlin_1.data.api.RetrofitClient
import com.example.kotlin_1.data.repository.ImplPlanificateur
import com.example.kotlin_1.domain.repository.Planificateur
import com.example.kotlin_1.domain.usecase.RecupRoute
import com.example.kotlin_1.presentation.map.MaitreCartographe
import androidx.lifecycle.ViewModelProvider


class AppContainer(private val apiKey: String) {

    private val planificateurRepository: Planificateur by lazy {
        ImplPlanificateur(
            apiService = RetrofitClient.directionsService,
            apiKey = apiKey //
        )
    }


    // Use Case (Logique Métier)
    private val recupererRouteUseCase: RecupRoute by lazy {
        RecupRoute(repository = planificateurRepository)
    }


    //  ViewModel (Maitre Cartographe)
    val maitreCartographeFactory: MaitreCartographeFactory by lazy {
        MaitreCartographeFactory(recupererRouteUseCase)
    }
}

// Classe Factory pour créer le ViewModel
class MaitreCartographeFactory(
    private val recupererRoute: RecupRoute
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaitreCartographe::class.java)) {
            return MaitreCartographe(recupererRoute) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}