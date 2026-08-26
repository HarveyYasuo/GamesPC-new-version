package com.harvey.gamespc.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harvey.gamespc.SharedViewModel
import kotlinx.coroutines.delay

/**
 * Pestaña "Agregar" — formulario nativo equivalente a
 * https://harveyyasuo.github.io/add.html. Escribe el contenido en
 * Base/1/data/<id> con la misma estructura que la web.
 */
@Composable
fun AddContentScreen(
    sharedViewModel: SharedViewModel,
    viewModel: AddContentViewModel = hiltViewModel()
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var enlaceSitio by remember { mutableStateOf("") }
    var enlaceImagen by remember { mutableStateOf("") }
    var enlaceVideo by remember { mutableStateOf("") }

    var tituloError by remember { mutableStateOf<String?>(null) }
    var descripcionError by remember { mutableStateOf<String?>(null) }
    var enlaceSitioError by remember { mutableStateOf<String?>(null) }
    var enlaceImagenError by remember { mutableStateOf<String?>(null) }
    var enlaceVideoError by remember { mutableStateOf<String?>(null) }

    val state by viewModel.state.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Al guardar con éxito, limpiar el formulario y volver al estado inicial
    LaunchedEffect(state) {
        if (state == AddContentState.SUCCESS) {
            // Avisar al Home para que recargue el catálogo
            sharedViewModel.notifyContentAdded()
            delay(3000)
            titulo = ""
            descripcion = ""
            enlaceSitio = ""
            enlaceImagen = ""
            enlaceVideo = ""
            tituloError = null
            descripcionError = null
            enlaceSitioError = null
            enlaceImagenError = null
            enlaceVideoError = null
            viewModel.resetToIdle()
        }
    }

    val isSubmitting = state == AddContentState.SUBMITTING

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Agregar Contenido",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Comparte tus juegos y programas favoritos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Título
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it; if (it.isNotBlank()) tituloError = null },
            label = { Text("Título") },
            placeholder = { Text("Ej: Minecraft, Visual Studio Code") },
            leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
            isError = tituloError != null,
            supportingText = { tituloError?.let { Text(it) } },
            singleLine = true,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Descripción
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it; if (it.isNotBlank()) descripcionError = null },
            label = { Text("Descripción") },
            placeholder = { Text("Describe el contenido, características principales...") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            isError = descripcionError != null,
            supportingText = { descripcionError?.let { Text(it) } },
            minLines = 3,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Enlace del Sitio
        OutlinedTextField(
            value = enlaceSitio,
            onValueChange = { enlaceSitio = it; if (it.isNotBlank()) enlaceSitioError = null },
            label = { Text("Enlace del Sitio") },
            placeholder = { Text("https://ejemplo.com/descarga") },
            leadingIcon = { Icon(Icons.Default.Public, contentDescription = null) },
            isError = enlaceSitioError != null,
            supportingText = { enlaceSitioError?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Enlace de Imagen
        OutlinedTextField(
            value = enlaceImagen,
            onValueChange = { enlaceImagen = it; if (it.isNotBlank()) enlaceImagenError = null },
            label = { Text("Enlace de Imagen") },
            placeholder = { Text("https://ejemplo.com/imagen.jpg") },
            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
            isError = enlaceImagenError != null,
            supportingText = { enlaceImagenError?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Enlace de Video
        OutlinedTextField(
            value = enlaceVideo,
            onValueChange = { enlaceVideo = it; if (it.isNotBlank()) enlaceVideoError = null },
            label = { Text("Enlace de Video") },
            placeholder = { Text("https://...mp4, .webm") },
            leadingIcon = { Icon(Icons.Default.Movie, contentDescription = null) },
            isError = enlaceVideoError != null,
            supportingText = { enlaceVideoError?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        // Vista previa (igual que la web)
        if (titulo.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = titulo.trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = descripcion.trim().ifBlank { "Sin descripción" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (enlaceSitio.isNotBlank()) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text(
                                " Enlace de sitio disponible",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (enlaceVideo.isNotBlank()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text(
                                " Enlace de video disponible",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botón de enviar
        Button(
            onClick = {
                tituloError = if (titulo.isBlank()) "Este campo es requerido." else null
                descripcionError = if (descripcion.isBlank()) "Este campo es requerido." else null
                enlaceSitioError = if (enlaceSitio.isBlank()) "Este campo es requerido." else null
                enlaceImagenError = if (enlaceImagen.isBlank()) "Este campo es requerido." else null
                enlaceVideoError = if (enlaceVideo.isBlank()) "Este campo es requerido." else null

                val hasErrors = tituloError != null || descripcionError != null ||
                    enlaceSitioError != null || enlaceImagenError != null || enlaceVideoError != null

                if (!hasErrors) {
                    viewModel.addContent(
                        titulo = titulo.trim(),
                        descripcion = descripcion.trim(),
                        enlaceSitio = enlaceSitio.trim(),
                        enlaceImagen = enlaceImagen.trim(),
                        enlaceVideo = enlaceVideo.trim()
                    )
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            when (state) {
                AddContentState.SUBMITTING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando en Base de Datos...")
                }
                AddContentState.SUCCESS -> Text("¡Agregado con éxito!")
                else -> Text("Agregar Contenido")
            }
        }

        // Mensajes de resultado
        when (state) {
            AddContentState.SUCCESS -> {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "¡Contenido agregado exitosamente!",
                        color = Color(0xFF16A34A),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            AddContentState.ERROR -> {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        errorMessage ?: "Error al guardar el contenido.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
