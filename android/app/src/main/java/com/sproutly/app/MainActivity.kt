package com.sproutly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sproutly.app.auth.AuthViewModel
import com.sproutly.app.core.design.SproutlyTheme
import com.sproutly.app.navigation.AppNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SproutlyTheme {
                val authVm: AuthViewModel = viewModel()
                val state by authVm.state.collectAsState()
                AppNavGraph(authState = state, authViewModel = authVm)
            }
        }
    }
}
