package com.harvey.gamespc.ui.version

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.harvey.gamespc.data.repository.ConfigRepository
import com.harvey.gamespc.utils.AppVersionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VersionViewModel @Inject constructor(
    application: Application,
    private val configRepository: ConfigRepository
) : AndroidViewModel(application) {

    private val _versionState = MutableStateFlow<VersionCheckState>(VersionCheckState.Loading)
    val versionState: StateFlow<VersionCheckState> = _versionState

    init {
        checkVersion()
    }

    private fun checkVersion() {
        viewModelScope.launch {
            try {
                val minRequiredVersionCode = configRepository.getMinRequiredVersionCode()
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
}
