package com.harvey.gamespc.ui.version

sealed class VersionCheckState {
    data object Loading : VersionCheckState()
    data class UpdateRequired(val updateUrl: String) : VersionCheckState()
    data object Success : VersionCheckState()
    data class Error(val message: String) : VersionCheckState()
}
