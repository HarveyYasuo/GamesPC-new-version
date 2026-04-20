package com.harvey.gamespc.data.repository

import com.harvey.gamespc.data.GameTable
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    /**
     * Obtiene todos los juegos desde Firebase y los devuelve como un flujo (Flow).
     * Esto permite manejar estados de carga y errores de forma reactiva.
     */
    fun getAllGames(): Flow<Result<List<GameTable>>>

    /**
     * Obtiene el tamaño de un archivo dado su URL.
     */
    suspend fun fetchItemFileSize(downloadUrl: String): String?
}

/**
 * Clase sellada para representar el resultado de una operación de datos.
 */
sealed class RepositoryResult<out T> {
    data class Success<out T>(val data: T) : RepositoryResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : RepositoryResult<Nothing>()
    object Loading : RepositoryResult<Nothing>()
}
