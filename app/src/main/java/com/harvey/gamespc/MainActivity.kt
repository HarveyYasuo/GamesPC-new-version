package com.harvey.gamespc

import android.app.PictureInPictureParams
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.unity3d.ads.UnityAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.harvey.gamespc.ui.MainScreen
import com.harvey.gamespc.ui.theme.GamesPCTheme
import com.harvey.gamespc.ui.version.VersionCheckState
import com.harvey.gamespc.ui.version.VersionViewModel
import com.harvey.gamespc.utils.ProvideSafeFocusManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var consentInformation: ConsentInformation
    private var consentForm: ConsentForm? = null
    private val versionViewModel: VersionViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && sharedViewModel.pipModeState.value) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Borde a borde en todas las versiones (Android 15+ lo impone para
        // targetSdk 35+; enableEdgeToEdge da retrocompatibilidad).
        enableEdgeToEdge()

        setContent {
            ProvideSafeFocusManager {
                GamesPCTheme {
                    VersionCheckScreen(versionViewModel)
                }
            }
        }
    }

    private fun setupConsentAndAds() {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                if (consentInformation.isConsentFormAvailable) {
                    loadConsentForm()
                } else {
                    setUnityAdsConsent()
                }
            },
            { _ ->
                // Handle the error
                setUnityAdsConsent() // Initialize ads even if consent check fails
            }
        )
    }

    private fun loadConsentForm() {
        UserMessagingPlatform.loadConsentForm(
            this,
            { form ->
                this.consentForm = form
                if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    consentForm?.show(this) {
                        setUnityAdsConsent()
                    }
                } else {
                    setUnityAdsConsent()
                }
            },
            {
                setUnityAdsConsent()
            }
        )
    }

    private fun setUnityAdsConsent() {
        val canRequestAds = consentInformation.canRequestAds()
        
        // Use the new Unity Ads Privacy API instead of deprecated MetaData
        UnityAds.userConsent = canRequestAds
        UnityAds.userOptOut = !canRequestAds
        UnityAds.nonBehavioral = true

        // Initialize Mobile Ads after setting Unity Ads metadata
        MobileAds.initialize(this) {}
    }

    @Composable
    fun VersionCheckScreen(viewModel: VersionViewModel) {
        val state by viewModel.versionState.collectAsState()

        when (val currentState = state) {
            is VersionCheckState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is VersionCheckState.UpdateRequired -> {
                UpdateDialog(updateUrl = currentState.updateUrl)
            }
            is VersionCheckState.Success -> {
                // Version is OK, proceed with normal app flow
                LaunchedEffect(Unit) {
                    val bundle = Bundle()
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "main_screen")
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
                    MyApplication.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
                }
                setupConsentAndAds()
                MainScreen(sharedViewModel)
            }
            is VersionCheckState.Error -> {
                // For simplicity, we'll also proceed on error.
                // You could show a toast or a specific error screen here.
                setupConsentAndAds()
                MainScreen(sharedViewModel)
            }
        }
    }

    @Composable
    fun UpdateDialog(updateUrl: String) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { /* Do nothing, it's a mandatory update */ },
            title = { Text(stringResource(id = R.string.update_dialog_title)) },
            text = { Text(stringResource(id = R.string.update_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, updateUrl.toUri())
                    context.startActivity(intent)
                }) {
                    Text(stringResource(id = R.string.update_button))
                }
            }
        )
    }
}