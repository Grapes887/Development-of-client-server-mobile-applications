package com.example.module6.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.module6.presentation.ui.screen.PhotoDetailScreen
import com.example.module6.presentation.ui.screen.PhotoListScreen
import com.example.module6.presentation.viewmodel.DownloadState
import com.example.module6.presentation.viewmodel.PhotoCatalogViewModel

private const val LIST_ROUTE = "photos"
private const val DETAIL_ROUTE = "photo_detail"

@Composable
fun PhotoCatalogNavGraph(
    viewModel: PhotoCatalogViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedPhoto by viewModel.selectedPhoto.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val documentLauncher = rememberLauncherForActivityResult(
        contract = CreateDocument("image/jpeg")
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        viewModel.downloadSelectedPhoto {
            context.contentResolver.openOutputStream(uri)
        }
    }

    LaunchedEffect(downloadState) {
        when (val state = downloadState) {
            DownloadState.Idle -> Unit
            DownloadState.InProgress -> Unit
            is DownloadState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearDownloadState()
            }

            is DownloadState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearDownloadState()
            }
        }
    }

    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LIST_ROUTE,
            modifier = modifier
        ) {
            composable(LIST_ROUTE) {
                PhotoListScreen(
                    uiState = uiState,
                    contentPadding = innerPadding,
                    onRetry = viewModel::loadPhotos,
                    onPhotoClick = { photo ->
                        viewModel.selectPhoto(photo)
                        navController.navigate(DETAIL_ROUTE)
                    }
                )
            }
            composable(DETAIL_ROUTE) {
                PhotoDetailScreen(
                    photo = selectedPhoto,
                    downloadState = downloadState,
                    contentPadding = innerPadding,
                    onBack = { navController.popBackStack() },
                    onDownloadClick = { photo ->
                        viewModel.selectPhoto(photo)
                        documentLauncher.launch(photo.fileName)
                    }
                )
            }
        }
    }
}
