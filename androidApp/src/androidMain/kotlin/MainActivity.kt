package com.banking.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.banking.app.ui.BankingApp
import com.banking.app.ui.theme.BankingAppTheme
import data.MongoDbAndroid
import viewmodel.TransactionViewModel


class MainActivity : ComponentActivity() {

    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize repository and ViewModel
        val repository = MongoDbAndroid(applicationContext)
        viewModel = TransactionViewModel(repository)

        setContent {
            BankingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BankingApp(viewModel)
                }
            }
        }
    }
}