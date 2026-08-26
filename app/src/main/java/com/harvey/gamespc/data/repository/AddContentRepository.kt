package com.harvey.gamespc.data.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para agregar contenido nuevo. Replica exactamente la lógica y la
 * estructura de datos de https://harveyyasuo.github.io/add.html:
 * escribe en Base/1/data/<id> con el ID secuencial siguiente y las mismas
 * claves en español que la app ya lee (ver GameItem).
 */
@Singleton
class AddContentRepository @Inject constructor(
    private val databaseReference: DatabaseReference
) {
    private val dataRef
        get() = databaseReference.child("Base/1/data")

    suspend fun addContent(
        titulo: String,
        descripcion: String,
        enlaceSitio: String,
        enlaceImagen: String,
        enlaceVideo: String
    ): Result<Unit> = try {
        // 1. Leer los datos para obtener el siguiente ID (igual que la web)
        val snapshot = dataRef.get().await()
        val nextId = if (snapshot.exists()) snapshot.childrenCount else 0L

        // 2. Crear el nuevo item con las mismas claves que usa la app
        val nuevoItem = mapOf(
            "id" to nextId.toString(),
            "titulo" to titulo,
            "descripcion" to descripcion,
            "enlace_sitio" to enlaceSitio,
            "enlace_imagen" to enlaceImagen,
            "enlace_video" to enlaceVideo,
            "timestamp" to ServerValue.TIMESTAMP
        )

        // 3. Escribir el nuevo item en la ruta del nuevo ID
        dataRef.child(nextId.toString()).setValue(nuevoItem).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
