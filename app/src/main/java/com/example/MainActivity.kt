package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.PrescriptionRepository
import com.example.ui.PrescriptionViewModel
import com.example.ui.PrescriptionViewModelFactory
import com.example.ui.screens.PrescriptionAppMainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Instantiate local Room database and Repository
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = PrescriptionRepository(database)
        
        // 2. Initialize state ViewModel via factory
        val viewModel: PrescriptionViewModel by viewModels {
            PrescriptionViewModelFactory(application, repository)
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PrescriptionAppMainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

