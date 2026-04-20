
package com.harvey.gamespc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AppConfig(
    val version: String = "",
    val minRequiredVersionCode: Int = 0
) {
    constructor() : this("", 0) // Default constructor for Firebase
}

class ConfigViewModel : ViewModel() {

    private val _appConfig = MutableStateFlow(AppConfig())
    val appConfig: StateFlow<AppConfig> = _appConfig

    private val database = FirebaseDatabase.getInstance()
    private val configRef = database.getReference("config")

    private val configListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val config = snapshot.getValue(AppConfig::class.java)
            config?.let { _appConfig.value = it }
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
        }
    }

    init {
        configRef.addValueEventListener(configListener)
    }

    override fun onCleared() {
        super.onCleared()
        configRef.removeEventListener(configListener)
    }
}
