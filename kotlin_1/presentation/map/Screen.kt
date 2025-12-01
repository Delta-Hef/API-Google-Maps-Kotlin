package com.example.kotlin_1.presentation.map
import android.speech.tts.TextToSpeech
import java.util.Locale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import com.example.kotlin_1.domain.model.PointGPS
import com.example.kotlin_1.domain.model.RouteFinale

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

fun PointGPS.toGoogleLatLng() = LatLng(this.latitude, this.longitude)

@Composable
fun Screen(viewModel: MaitreCartographe) {

    val etat by viewModel.etatDeRoute.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    DisposableEffect(context) {
        val destination = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.FRENCH //
            }
        }
        tts = destination
        onDispose { tts?.shutdown() } // On coupe le moteur quand on quitte l'écran? A verifier
    }

    // Pour la Snackbar "Go Champion"
    val snackbarHostState = remember { SnackbarHostState() }

    // États pour les champs de texte
    var departAddress by remember { mutableStateOf("Tour Eiffel, Paris") }
    var arriveeAddress by remember { mutableStateOf("Musée du Louvre, Paris") }

    // Visibilité de la recherche
    var isSearchVisible by remember { mutableStateOf(true) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(48.8566, 2.3522), 11f)
    }

    // Gestion de la Caméra et des Messages
    LaunchedEffect(etat) {
        if (etat is EtatRoute.Succès) {
            val route = (etat as EtatRoute.Succès).route
            if (route.pointsDuTracé.isNotEmpty()) {

                // petit zoom sympa
                val builder = LatLngBounds.Builder()
                route.pointsDuTracé.forEach { builder.include(it.toGoogleLatLng()) }
                try {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngBounds(builder.build(), 200),
                        durationMs = 1000
                    )
                } catch (e: Exception) {}

                isSearchVisible = false

                launch {
                    snackbarHostState.showSnackbar(
                        message = "Itinéraire prêt. Go Champion ! 🚀",
                        duration = SnackbarDuration.Short
                    )
                }
                tts?.speak(
                    "Itinéraire trouvé. Distance : ${route.distanceTotale}. Safe to Go  !",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }
        } else if (etat is EtatRoute.Erreur) { // AVEC ACCENT
            launch {
                snackbarHostState.showSnackbar(
                    message = "Erreur : ${(etat as EtatRoute.Erreur).message}"
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                // 1. LA CARTE (Fond)
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = false)
                ) {
                    if (etat is EtatRoute.Succès) { // AVEC ACCENT
                        RouteRenderer((etat as EtatRoute.Succès).route)
                    }
                }

                // SUPER BARRE DE RECHERCHE !!!!
                AnimatedVisibility(
                    visible = isSearchVisible,
                    modifier = Modifier.align(Alignment.TopCenter),
                    enter = slideInVertically(),
                    exit = slideOutVertically { -it }
                ) {
                    SearchCard(
                        depart = departAddress,
                        arrivee = arriveeAddress,
                        onDepartChange = { departAddress = it },
                        onArriveeChange = { arriveeAddress = it },
                        onSwap = {
                            val temp = departAddress
                            departAddress = arriveeAddress
                            arriveeAddress = temp
                        },
                        onSearch = {
                            keyboardController?.hide()

                            viewModel.chercherItineraire(departAddress, arriveeAddress)
                        },
                        isLoading = etat is EtatRoute.Chargement
                    )
                }


                AnimatedVisibility(
                    visible = !isSearchVisible && etat is EtatRoute.Succès,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    if (etat is EtatRoute.Succès) {
                        ResultCard(
                            route = (etat as EtatRoute.Succès).route,
                            onNewSearch = { isSearchVisible = true }
                        )
                    }
                }

                // Bouton retour
                if (!isSearchVisible && etat !is EtatRoute.Succès) {
                    FloatingActionButton(
                        onClick = { isSearchVisible = true },
                        modifier = Modifier.align(Alignment.TopStart).padding(16.dp).padding(top=32.dp),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Recherche")
                    }
                }
            }
        }
    )
}


@Composable
fun SearchCard(
    depart: String,
    arrivee: String,
    onDepartChange: (String) -> Unit,
    onArriveeChange: (String) -> Unit,
    onSwap: () -> Unit,
    onSearch: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 32.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Ligne Départ
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, "Départ", tint = Color.Green, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = depart,
                    onValueChange = onDepartChange,
                    label = { Text("Départ", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Bouton Swap centré enfin !
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onSwap,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-20).dp)
                        .size(32.dp)
                        .background(Color.LightGray.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.SwapVert, "Inverser", tint = Color.DarkGray)
                }
            }

            // Ligne Arrivée
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, "Arrivée", tint = Color.Red, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = arrivee,
                    onValueChange = onArriveeChange,
                    label = { Text("Arrivée", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Rechercher l'itinéraire", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ResultCard(route: RouteFinale, onNewSearch: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).background(Color.Blue.copy(alpha=0.1f), CircleShape).padding(4.dp),
                    tint = Color.Blue
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = route.distanceTotale, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Temps estimé (Piéton)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onNewSearch,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0), contentColor = Color.Black)
            ) {
                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Nouvelle recherche")
            }
        }
    }
}

@Composable
fun RouteRenderer(route: RouteFinale) {

    // jolie Fond Gris
    if (route.pointsDuTracé.isNotEmpty()) {
        Polyline(
            points = route.pointsDuTracé.map { it.toGoogleLatLng() },
            color = Color.DarkGray,
            width = 12f
        )
    }

    // Segments n Pins
    route.étapesDétaillées.forEach { step ->

        val pinColor = when {
            step.instruction.contains("gauche", true) || step.instruction.contains("left", true) -> BitmapDescriptorFactory.HUE_YELLOW
            step.instruction.contains("droite", true) || step.instruction.contains("right", true) -> BitmapDescriptorFactory.HUE_ORANGE
            step == route.étapesDétaillées.first() -> BitmapDescriptorFactory.HUE_GREEN
            else -> BitmapDescriptorFactory.HUE_AZURE
        }

        Marker(
            state = rememberMarkerState(position = step.debut.toGoogleLatLng()),
            title = step.instruction,
            icon = BitmapDescriptorFactory.defaultMarker(pinColor)
        )

        val startIndex = route.pointsDuTracé.indexOf(step.debut)
        val endIndex = route.pointsDuTracé.indexOf(step.fin)

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            val segmentPoints = route.pointsDuTracé
                .subList(startIndex, endIndex + 1)
                .map { it.toGoogleLatLng() }

            Polyline(
                points = segmentPoints,
                color = Color(step.couleurCode),
                width = 10f,
                zIndex = 2f
            )
        }
    }

    // Pin Arrivée
    route.étapesDétaillées.lastOrNull()?.fin?.let { finalPoint ->
        Marker(
            state = rememberMarkerState(position = finalPoint.toGoogleLatLng()),
            title = "Arrivée",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        )
    }
}