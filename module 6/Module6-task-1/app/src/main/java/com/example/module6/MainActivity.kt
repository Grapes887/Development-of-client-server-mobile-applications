package com.example.module6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.module6.data.remote.PicsumApiService
import com.example.module6.data.repository.PhotoRepositoryImpl
import com.example.module6.domain.usecase.DownloadPhotoUseCase
import com.example.module6.domain.usecase.GetPhotosUseCase
import com.example.module6.navigation.PhotoCatalogNavGraph
import com.example.module6.presentation.viewmodel.PhotoCatalogViewModel
import com.example.module6.ui.theme.Module6Theme
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    private val viewModel: PhotoCatalogViewModel by viewModels {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        val apiService = Retrofit.Builder()
            .baseUrl("https://picsum.photos/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PicsumApiService::class.java)
        val repository = PhotoRepositoryImpl(apiService)

        PhotoCatalogViewModel.Factory(
            getPhotosUseCase = GetPhotosUseCase(repository),
            downloadPhotoUseCase = DownloadPhotoUseCase(repository)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Module6Theme {
                PhotoCatalogTheme {
                    PhotoCatalogNavGraph(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun PhotoCatalogTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
