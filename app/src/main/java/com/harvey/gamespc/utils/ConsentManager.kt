package com.harvey.gamespc.utils

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentInformation.ConsentStatus
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

object ConsentManager {

    private const val TAG = "ConsentManager"
    private lateinit var consentInformation: ConsentInformation

    fun requestConsent(activity: Activity, onConsentGathered: (Boolean) -> Unit) {
        // Create a ConsentRequestParameters object.
        val params = ConsentRequestParameters
            .Builder()
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {   // Consent info update success.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    activity,
                    fun(formError: FormError?) {
                        if (formError != null) {
                            // Consent form error.
                            Log.e(TAG, String.format("Error showing consent form: %s", formError.message))
                            onConsentGathered(false)
                        } else {
                            // Consent form success.
                            Log.d(TAG, "Consent form shown successfully.")
                            onConsentGathered(consentInformation.canRequestAds())
                        }
                    }
                )
            },
            {   // Consent info update error.
                Log.e(TAG, String.format("Error updating consent info: %s", it.message))
                onConsentGathered(false)
            }
        )
    }

    fun canRequestAds(): Boolean {
        return consentInformation.canRequestAds()
    }

    fun getConsentStatus(): Int {
        return consentInformation.consentStatus
    }
}
