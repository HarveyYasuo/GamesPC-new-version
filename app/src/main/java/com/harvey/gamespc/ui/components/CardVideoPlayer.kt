package com.harvey.gamespc.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@androidx.media3.common.util.UnstableApi
@Composable
fun CardVideoPlayer(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            if (videoUrl.isNotBlank()) {
                setMediaItem(MediaItem.fromUri(videoUrl))
                prepare()
                playWhenReady = true
                volume = 0f // Mute the video
                repeatMode = Player.REPEAT_MODE_ONE // Loop the video
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false // Hide controls
                controllerAutoShow = false
            }
        },
        modifier = modifier
    )
}
