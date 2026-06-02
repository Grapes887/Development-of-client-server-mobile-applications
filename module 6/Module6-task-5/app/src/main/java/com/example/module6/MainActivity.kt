package com.example.module6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.module6.data.preferences.AuthTokenDataStore
import com.example.module6.data.remote.DummyJsonApiService
import com.example.module6.data.repository.AuthRepositoryImpl
import com.example.module6.domain.usecase.GetSavedTokenUseCase
import com.example.module6.domain.usecase.GetUserDetailUseCase
import com.example.module6.domain.usecase.GetUsersUseCase
import com.example.module6.domain.usecase.LoginUseCase
import com.example.module6.domain.usecase.LogoutUseCase
import com.example.module6.navigation.AuthNavGraph
import com.example.module6.presentation.viewmodel.AuthAppViewModel
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

    private val viewModel: AuthAppViewModel by viewModels {
        val client = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                gson()
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("AuthClient: $message")
                    }
                }
                level = LogLevel.INFO
            }
            defaultRequest {
                url("https://dummyjson.com/")
            }
        }
        val repository = AuthRepositoryImpl(
            apiService = DummyJsonApiService(client),
            tokenStore = AuthTokenDataStore(applicationContext)
        )

        AuthAppViewModel.Factory(
            getSavedTokenUseCase = GetSavedTokenUseCase(repository),
            loginUseCase = LoginUseCase(repository),
            getUsersUseCase = GetUsersUseCase(repository),
            getUserDetailUseCase = GetUserDetailUseCase(repository),
            logoutUseCase = LogoutUseCase(repository)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Module6Theme {
                AuthTheme {
                    AuthNavGraph(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun AuthTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
