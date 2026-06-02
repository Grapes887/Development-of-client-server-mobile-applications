package com.example.module6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.module6.data.remote.NobelApiService
import com.example.module6.data.repository.NobelRepositoryImpl
import com.example.module6.domain.usecase.GetLaureateDetailUseCase
import com.example.module6.domain.usecase.GetLaureatesUseCase
import com.example.module6.navigation.NobelNavGraph
import com.example.module6.presentation.viewmodel.NobelViewModel
import com.example.module6.ui.theme.Module6Theme
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.gson.gson

class MainActivity : ComponentActivity() {

    private val viewModel: NobelViewModel by viewModels {
        val client = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                gson()
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("KtorClient: $message")
                    }
                }
                level = LogLevel.INFO
            }
            defaultRequest {
                url("https://api.nobelprize.org/2.1/")
            }
        }
        val repository = NobelRepositoryImpl(NobelApiService(client))

        NobelViewModel.Factory(
            getLaureatesUseCase = GetLaureatesUseCase(repository),
            getLaureateDetailUseCase = GetLaureateDetailUseCase(repository)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Module6Theme {
                NobelTheme {
                    NobelNavGraph(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun NobelTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
