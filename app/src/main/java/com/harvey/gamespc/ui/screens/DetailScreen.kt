package com.harvey.gamespc.ui.screens

import android.app.Activity
import androidx.annotation.OptIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import com.harvey.gamespc.R
import com.harvey.gamespc.ui.components.CardVideoPlayer
import com.harvey.gamespc.ui.components.VideoPlayer
import com.harvey.gamespc.utils.DownloadStatus

@OptIn(UnstableApi::class)
@Composable
fun DetailScreen(
    detailViewModel: DetailViewModel,
    onPipModeChanged: (Boolean) -> Unit = {}
) {
    val item by detailViewModel.item.collectAsState()
    val adState by detailViewModel.adState.collectAsState()
    val fileSizes by detailViewModel.fileSizes.collectAsState()
    val activeDownloadUrl by detailViewModel.activeDownloadUrl.collectAsState()
    val downloadStates by detailViewModel.downloadStates.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var showAdBlockerDialog by remember { mutableStateOf(false) }

    if (adState is AdState.AdBlockerDetected) {
        showAdBlockerDialog = true
    }

    if (showAdBlockerDialog) {
        AlertDialog(
            onDismissRequest = { showAdBlockerDialog = false },
            title = { Text(text = stringResource(R.string.ad_blocker_detected_title)) },
            text = { Text(text = stringResource(R.string.ad_blocker_detected_message)) },
            confirmButton = {
                Button(onClick = { showAdBlockerDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    val currentItem = item
    if (currentItem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!currentItem.videoUrl.isNullOrBlank()) {
                VideoPlayer(
                    videoUrl = currentItem.videoUrl!!,
                    onPipModeChanged = onPipModeChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Box(modifier = Modifier
                .width(267.dp)
                .height(400.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(12.dp)) // Clip to apply shadow and shine correctly
                .shineEffect()
            ) {
                val imageUrl = currentItem.imageUrl
                val isVideo = imageUrl?.endsWith(".webm", true) == true || imageUrl?.endsWith(".mp4", true) == true

                if (isVideo) {
                    CardVideoPlayer(
                        videoUrl = imageUrl!!,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = currentItem.title,
                        modifier = Modifier.fillMaxSize(), // Fill the Box
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Text(
                text = currentItem.title ?: "No Title",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = currentItem.description ?: "No Description",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val downloadLinks = currentItem.downloadUrl?.split(",").orEmpty().map { it.trim() }.filter { it.isNotBlank() }

            if (downloadLinks.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.downloads_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    downloadLinks.forEach { downloadLink ->
                        val serviceNameResId = getServiceNameResId(downloadLink)
                        val fileSize = fileSizes[downloadLink]
                        val inAppDownloadStatus = downloadStates[downloadLink]
                        val isCurrentLinkLoading = adState is AdState.Loading && activeDownloadUrl == downloadLink
                        val isCurrentLinkReady = adState is AdState.Ready && activeDownloadUrl == downloadLink

                        Button(
                            onClick = {
                                if (activity != null) {
                                    if (isCurrentLinkReady) {
                                        detailViewModel.showAd(activity)
                                    } else {
                                        detailViewModel.loadAdForDownload(downloadLink)
                                    }
                                }
                            },
                            enabled = adState !is AdState.Loading,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            val buttonText = when {
                                isCurrentLinkLoading -> stringResource(R.string.ad_loading)
                                isCurrentLinkReady -> stringResource(R.string.ad_ready_to_show)
                                adState is AdState.Error && activeDownloadUrl == downloadLink -> stringResource(R.string.ad_error_retry)
                                adState is AdState.AdBlockerDetected -> stringResource(R.string.ad_blocker_detected)
                                else -> stringResource(R.string.download_from_prefix) + " " + stringResource(serviceNameResId) + (fileSize?.let { " ($it)" } ?: "")
                            }
                            Text(text = buttonText)
                        }

                        if ("mediafire.com" in downloadLink) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        if (activity != null) {
                                            if (isCurrentLinkReady) {
                                                detailViewModel.showAdForInAppDownload(activity, downloadLink)
                                            } else {
                                                detailViewModel.loadAdForDownload(downloadLink)
                                            }
                                        }
                                    },
                                    enabled = adState !is AdState.Loading && inAppDownloadStatus !is DownloadStatus.Progress,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val buttonText = when (inAppDownloadStatus) {
                                        is DownloadStatus.Progress -> "Downloading... ${inAppDownloadStatus.progress}%"
                                        is DownloadStatus.Success -> "Downloaded"
                                        is DownloadStatus.Error -> "Download Error"
                                        else -> "Download in App"
                                    }
                                    Text(text = buttonText)
                                }
                                if (inAppDownloadStatus is DownloadStatus.Progress) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = { detailViewModel.cancelDownload(downloadLink) }) {
                                        Text("Cancel")
                                    }
                                }
                            }
                            if (inAppDownloadStatus is DownloadStatus.Progress) {
                                LinearProgressIndicator(
                                    progress = { inAppDownloadStatus.progress / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getServiceNameResId(url: String): Int {
    return when {
        "mediafire.com" in url -> R.string.service_name_mediafire
        "1fichier.com" in url -> R.string.service_name_1fichier
        "gofile.io" in url -> R.string.service_name_gofile
        else -> R.string.service_name_direct_link
    }
}

// Custom Shine Effect Modifier based on Claude's code
fun Modifier.shineEffect(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val shinePosition by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    this.drawWithContent {
        drawContent() // Draw the image first

        val cardWidth = size.width
        val cardHeight = size.height

        val shineX = shinePosition * cardWidth

        // Only draw if it's within the visible range
        if (shineX > -100 && shineX < cardWidth + 100) {
            val gradient = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.3f),
                    Color.White.copy(alpha = 0.6f),
                    Color.White.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                start = Offset(shineX - 50, 0f),
                end = Offset(shineX + 50, 0f)
            )

            rotate(degrees = -15f, pivot = Offset(shineX, cardHeight / 2)) {
                drawRect(
                    brush = gradient,
                    topLeft = Offset(shineX - 25, -50f),
                    size = androidx.compose.ui.geometry.Size(50f, cardHeight + 100)
                )
            }
        }
    }
}