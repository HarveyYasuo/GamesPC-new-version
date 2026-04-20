package com.harvey.gamespc.ui.version

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.harvey.gamespc.utils.AppVersionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VersionViewModel(application: Application) : AndroidViewModel(application) {

    private val _versionState = MutableStateFlow<VersionCheckState>(VersionCheckState.Loading)
    val versionState: StateFlow<VersionCheckState> = _versionState

    init {
        checkVersion()
    }

    private fun checkVersion() {
        viewModelScope.launch {
            try {
                val minRequiredVersionCode = fetchMinVersionFromFirebase()
                val currentVersionCode = AppVersionUtils.getVersionCode(getApplication())

                if (currentVersionCode < minRequiredVersionCode) {
                    val packageName = getApplication<Application>().packageName
                    _versionState.value = VersionCheckState.UpdateRequired(
                        "https://play.google.com/store/apps/details?id=$packageName"
                    )
                } else {
                    _versionState.value = VersionCheckState.Success
                }
            } catch (e: Exception) {
                // Si hay un error (ej. sin internet), permitimos el acceso pero podrías
                // querer un manejo más estricto.
                _versionState.value = VersionCheckState.Success
            }
        }
    }

    private suspend fun fetchMinVersionFromFirebase(): Long {
        val database = FirebaseDatabase.getInstance()
        val versionRef = database.getReference("config/version/minRequiredVersionCode")
        val snapshot = versionRef.get().await()
        return snapshot.getValue(Long::class.java) ?: -1
    }
}
