package com.example.kotlin_1.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun Kotlin_1Theme(content: @Composable () -> Unit) {
    // Dans un vrai projet, vous définissez ici les couleurs sombres/claires.
    // Pour l'instant, on utilise le MaterialTheme par défaut.
    MaterialTheme(
        content = content
    )
}