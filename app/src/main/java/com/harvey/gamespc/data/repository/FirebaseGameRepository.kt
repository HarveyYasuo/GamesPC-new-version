package com.harvey.gamespc.data.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.harvey.gamespc.data.GameTable
import com.harvey.gamespc.utils.FileSizeFetcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseGameRepository(
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
) : GameRepository {

    /**
     * Obtiene todos los juegos de Firebase "Base".
     * Emito el resultado mediante un flow para manejarlo reactivamente.
     */
    override fun getAllGames(): Flow<Result<List<GameTable>>> = flow {
        try {
            // 1. Fetch de datos crudos desde Firebase
            val snapshot = database.child("Base").get().await()
            val fetchedItems = snapshot.children.mapNotNull { childSnapshot ->
                val type = childSnapshot.child("type").getValue(String::class.java)
                if (type == "table" || type == "database") {
                    childSnapshot.getValue(GameTable::class.java)
                } else {
                    null
                }
            }

            // 2. Emitimos el primer resultado (los datos sin el tamaño de archivo aún)
            // para que la UI los muestre lo antes posible.
            emit(Result.success(fetchedItems))

            // 3. (Opcional) Podemos disparar la actualización de tamaños aquí
            // o dejar que el ViewModel lo gestione para no bloquear esta respuesta.
            // Para mantener el diseño limpio, solo devolvemos los datos básicos.
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Un método adicional para obtener el tamaño de archivo de forma aislada.
     */
    override suspend fun fetchItemFileSize(downloadUrl: String): String? {
        return FileSizeFetcher.getFileSize(downloadUrl)
    }
}
