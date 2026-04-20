package com.harvey.gamespc.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import com.harvey.gamespc.data.GameItem
import com.harvey.gamespc.ui.SoundManager
import com.harvey.gamespc.ui.components.CardVideoPlayer

@Composable
fun ItemsGridScreen(
    items: List<GameItem>,
    isLoading: Boolean,
    onItemClick: (GameItem) -> Unit
) {
    if (isLoading && items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                ItemCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun ItemCard(item: GameItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
                        .clickable {
                SoundManager.playSound("click_items")
                onClick()
            },
        border = BorderStroke(2.dp, Color.Cyan)
    ) {
        Column {
            val imageUrl = item.imageUrl ?: ""
            val isVideo = imageUrl.endsWith(".webm", true) || imageUrl.endsWith(".mp4", true)

            if (isVideo) {
                CardVideoPlayer(
                    videoUrl = imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 5f)
                )
            } else {
                // Coil's AsyncImage handles GIFs automatically if the coil-gif dependency is present
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 5f),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = item.title ?: "No Title",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.description ?: "No Description",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                maxLines = 2
            )
            Text(
                text = item.fileSize ?: "...",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            )
        }
    }
}