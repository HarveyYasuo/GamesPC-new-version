package com.harvey.gamespc.data

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class GameTable(
    val type: String? = null,
    val name: String? = null,
    var data: List<GameItem>? = null,
    // Clave del nodo en Firebase (p. ej. "1" en Base/1); no se persiste
    var key: String? = null
)

@IgnoreExtraProperties
data class GameItem(
    val id: String? = null,
    @get:PropertyName("titulo") @set:PropertyName("titulo")
    var title: String? = null,
    @get:PropertyName("descripcion") @set:PropertyName("descripcion")
    var description: String? = null,
    @get:PropertyName("enlace_imagen") @set:PropertyName("enlace_imagen")
    var imageUrl: String? = null,
    @get:PropertyName("enlace_sitio") @set:PropertyName("enlace_sitio")
    var downloadUrl: String? = null,
    @get:PropertyName("nombre_creador") @set:PropertyName("nombre_creador")
    var authorName: String? = null,
    @get:PropertyName("views_count") @set:PropertyName("views_count")
    var viewsCount: String? = null,
    @get:PropertyName("fecha_creacion") @set:PropertyName("fecha_creacion")
    var createdAt: String? = null,
    @get:PropertyName("enlace_video") @set:PropertyName("enlace_video")
    var videoUrl: String? = null,
    var fileSize: String? = null,
    // Clave Firebase real del nodo (p. ej. -Nxxxx o el id numérico); no se persiste
    var key: String? = null
)
