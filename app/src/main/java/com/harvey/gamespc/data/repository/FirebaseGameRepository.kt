package com.harvey.gamespc.data.repository

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.harvey.gamespc.data.GameItem
import com.harvey.gamespc.data.GameTable
import com.harvey.gamespc.utils.FileSizeFetcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseGameRepository(
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
) : GameRepository {

    private val tag = "FirebaseGameRepository"

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
                    childSnapshot.getValue(GameTable::class.java)?.also { table ->
                        // Clave real de la tabla (p. ej. "1") para poder escribir en su ruta exacta
                        table.key = childSnapshot.key
                        // Clave Firebase real de cada item (puede NO coincidir con el campo "id")
                        val dataSnapshot = childSnapshot.child("data")
                        val keyedItems = dataSnapshot.children.mapNotNull { itemSnapshot ->
                            itemSnapshot.getValue(GameItem::class.java)?.also { it.key = itemSnapshot.key }
                        }
                        table.data = keyedItems
                    }
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

    override fun saveFileSize(tableName: String, itemId: String, size: String) {
        database.child("Base").child(tableName).child("data").child(itemId)
            .child("fileSize").setValue(size)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(tag, "saveFileSize falló en Base/$tableName/data/$itemId: ${task.exception?.message}")
                }
            }
    }

    override fun incrementViews(tableName: String, itemId: String, newCount: String) {
        database.child("Base").child(tableName).child("data").child(itemId)
            .child("views_count").setValue(newCount)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(tag, "views_count actualizado: Base/$tableName/data/$itemId -> $newCount")
                } else {
                    Log.w(tag, "incrementViews falló en Base/$tableName/data/$itemId: ${task.exception?.message}")
                }
            }
    }
}
