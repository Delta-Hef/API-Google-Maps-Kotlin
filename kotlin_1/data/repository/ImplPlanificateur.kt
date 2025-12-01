package com.example.kotlin_1.data.repository

import com.example.kotlin_1.data.api.*
import com.example.kotlin_1.domain.model.PointGPS
import com.example.kotlin_1.domain.model.RouteFinale
import com.example.kotlin_1.domain.model.SegmentEtape
import com.example.kotlin_1.domain.repository.Planificateur // Attention : On importe votre interface Planificateur
import com.google.maps.android.PolyUtil


// Mappe la LocationDTO de l'API vers notre PointGPS du Domaine
fun LocationDTO.toPointGPS(): PointGPS {
    return PointGPS(latitude = this.lat, longitude = this.lng)
}

//couleur du segment selon l'instruction

private fun getSegmentColorCode(instruction: String): Int {
    return when {
        instruction.contains("destination", ignoreCase = true) -> android.graphics.Color.RED
        instruction.contains("Turn left", ignoreCase = true) || instruction.contains("Turn right", ignoreCase = true) -> android.graphics.Color.BLUE
        else -> android.graphics.Color.CYAN
    }
}



class ImplPlanificateur(
    private val apiService: ServiceDirections,
    private val apiKey: String
) : Planificateur { // interface impl

    override suspend fun calculerTrajetPiéton(
        depart: String,
        arrivee: String
    ): Result<RouteFinale> {
        return try {
            val response = apiService.getDirections(depart, arrivee, apiKey = apiKey)

            if (response.isSuccessful && response.body()?.routes?.isNotEmpty() == true) {

                //  données brutes
                val routeDto = response.body()!!.routes.first()
                val legDto = routeDto.legs.first()

                //  DÉCODAGE CRUCIAL (Séparation affichage/calcul)
                val encodedPath = routeDto.overviewPolyline.points
                val googleLatLngs = PolyUtil.decode(encodedPath)

                //  CONVERSION vers les entités du Domaine (PointGPS)
                val customPathPoints = googleLatLngs.map { PointGPS(it.latitude, it.longitude) }

                // FIOUUU MAPPING des étapes détaillées
                val detailedSteps = legDto.steps.map { stepDto ->
                    SegmentEtape(
                        instruction = stepDto.htmlInstructions,
                        debut = stepDto.startLocation.toPointGPS(),
                        fin = stepDto.endLocation.toPointGPS(),
                        couleurCode = getSegmentColorCode(stepDto.htmlInstructions)
                    )
                }

                val routeFinale = RouteFinale(
                    pointsDuTracé = customPathPoints,
                    étapesDétaillées = detailedSteps,
                    distanceTotale = legDto.distance.text
                )

                return Result.success(routeFinale)

            } else {
                return Result.failure(Exception("Aucun itinéraire trouvé ou erreur API: ${response.code()}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}