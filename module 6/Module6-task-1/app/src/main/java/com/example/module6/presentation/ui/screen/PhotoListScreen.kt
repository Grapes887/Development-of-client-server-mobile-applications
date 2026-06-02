package com.example.module6.presentation.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.module6.domain.model.PhotoItem
import com.example.module6.presentation.viewmodel.PhotoListUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListScreen(
    uiState: PhotoListUiState,
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
    onPhotoClick: (PhotoItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Фотокаталог") }
            )
        }
    ) { innerPadding ->
        val combinedPadding = PaddingValues(
            start = 16.dp,
            top = innerPadding.calculateTopPadding() + contentPadding.calculateTopPadding(),
            end = 16.dp,
            bottom = innerPadding.calculateBottomPadding() + contentPadding.calculateBottomPadding()
        )

        when (uiState) {
            PhotoListUiState.Loading -> LoadingState(combinedPadding)
            is PhotoListUiState.Error -> ErrorState(
                message = uiState.message,
                contentPadding = combinedPadding,
                onRetry = onRetry
            )

            is PhotoListUiState.Success -> PhotoGrid(
                photos = uiState.photos,
                contentPadding = combinedPadding,
                onPhotoClick = onPhotoClick
            )
        }
    }
}

@Composable
private fun LoadingState(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    contentPadding: PaddingValues,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoGrid(
    photos: List<PhotoItem>,
    contentPadding: PaddingValues,
    onPhotoClick: (PhotoItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(photos, key = { photo -> photo.id }) { photo ->
            PhotoCard(
                photo = photo,
                onClick = { onPhotoClick(photo) }
            )
        }
    }
}

@Composable
private fun PhotoCard(
    photo: PhotoItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        AsyncImage(
            model = photo.thumbnailUrl,
            contentDescription = photo.author,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = photo.author,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = photo.readableSize,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
