package com.example.kotlin_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider

import com.example.kotlin_1.di.AppContainer
import com.example.kotlin_1.presentation.map.MaitreCartographe
import com.example.kotlin_1.presentation.map.Screen
import com.example.kotlin_1.ui.theme.Kotlin_1Theme


class Main : ComponentActivity() {

    private lateinit var appContainer: AppContainer
    private lateinit var viewModel: MaitreCartographe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiKey = BuildConfig.MAPS_API_KEY

        appContainer = AppContainer(apiKey = apiKey)

        val viewModelFactory = appContainer.maitreCartographeFactory
        viewModel = ViewModelProvider(this, viewModelFactory).get(MaitreCartographe::class.java)

        setContent {
            Kotlin_1Theme { //
                Screen(viewModel = viewModel)
            }
        }
    }
}