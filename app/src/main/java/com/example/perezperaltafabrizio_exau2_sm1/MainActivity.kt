package com.example.perezperaltafabrizio_exau2_sm1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.perezperaltafabrizio_exau2_sm1.ui.navigation.AppNavigation
import com.example.perezperaltafabrizio_exau2_sm1.ui.theme.PerezPeraltaFabrizio_EXAU2_SM1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PerezPeraltaFabrizio_EXAU2_SM1Theme {
                AppNavigation(application = application)
            }
        }
    }
}
