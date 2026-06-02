package com.example.module6.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.module6.domain.model.PhotoItem
import com.example.module6.presentation.viewmodel.DownloadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    photo: PhotoItem?,
    downloadState: DownloadState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onDownloadClick: (PhotoItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали фото") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        val combinedModifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                top = innerPadding.calculateTopPadding() + contentPadding.calculateTopPadding(),
                end = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + contentPadding.calculateBottomPadding()
            )

        if (photo == null) {
            Box(
                modifier = combinedModifier,
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Фотография не выбрана")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onBack) {
                        Text("Вернуться к списку")
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = combinedModifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            AsyncImage(
                model = photo.imageUrl,
                contentDescription = photo.author,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(MaterialTheme.shapes.extraLarge),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = photo.author,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Размер: ${photo.readableSize}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ссылка: ${photo.detailUrl}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { onDownloadClick(photo) },
                modifier = Modifier.fillMaxWidth(),
                enabled = downloadState !is DownloadState.InProgress
            ) {
                if (downloadState is DownloadState.InProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Rounded.Download, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (downloadState is DownloadState.InProgress) {
                        "Скачивание..."
                    } else {
                        "Скачать фото"
                    }
                )
            }
        }
    }
}
