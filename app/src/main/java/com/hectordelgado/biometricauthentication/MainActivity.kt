package com.hectordelgado.biometricauthentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

/**
 *  Biometric Authentication
 *  File: MainActivity.kt
 *
 *  @author Hector Delgado
 *
 *  Created on February 01, 2020.
 *  Copyright © 2020 Hector Delgado. All rights reserved.
 *
 *  An activity that displays a destructive message when
 *  a user successfully authenticates via their devices biometrics.
 */
class MainActivity : AppCompatActivity() {

    private val cipher = CaesarCipher(3)

    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        biometricPrompt = createBiometricPrompt()
        messageTextView.text = cipher.encryptMessage(getString(R.string.secret_message))
    }

    override fun onStart() {
        super.onStart()

        authenticateBtn.isEnabled = hasBiometricSupport()
    }

    fun authenticateUser(view: View) {
        biometricPrompt.authenticate(generatePromptInfo())

    }

    /**
     * Checks if biometric authentication is available on the device.
     *
     * @return biometric authentication support
     */
    private fun hasBiometricSupport(): Boolean {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                return true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                notifyUser(getString(R.string.biometric_manager_nohardware))
                return false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                notifyUser(getString(R.string.biometric_manager_unavailable))
                return false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                notifyUser(getString(R.string.biometric_manager_notenrolled))
                return false
            }
            else ->
                return false
        }
    }

    /**
     * Creates a new instance of BiometricPrompt with an AuthenticationCallback.
     *
     * @return An instance of BiometricPrompt
     */
    private fun createBiometricPrompt(): BiometricPrompt {
        val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                // Display the error that caused authentication to fail.
                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        notifyUser(getString(R.string.authentication_cancelled))
                    }
                    BiometricPrompt.ERROR_CANCELED -> {
                        notifyUser(getString(R.string.authentication_failed))
                    }
                    else -> {
                        notifyUser(getString(R.string.biometric_authentication_error) + ": $errString")
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                notifyUser(getString(R.string.biometric_authentication_fail))

                biometricPrompt.cancelAuthentication()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                notifyUser(getString(R.string.biometric_authentication_success))
                authenticateBtn.isEnabled = false

                val messagePrefix = cipher.decryptMessage(messageTextView.text.toString()) +
                        "\n\n" + getString(R.string.self_destruct_message)
                var counter = 3

                // Show the secret message for 3 seconds then reset to
                // the encrypted message
                val mainTimer = Timer()
                mainTimer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        if (counter < 0) {
                            runOnUiThread {
                                authenticateBtn.isEnabled = true
                                messageTextView.text = cipher.encryptMessage(getString(R.string.secret_message))
                            }
                            mainTimer.cancel()
                        } else {
                            val message = "$messagePrefix $counter"
                            runOnUiThread {
                                messageTextView.text = message
                            }
                            counter--
                        }
                    }
                }, 0, 1000)
            }
        }

        return BiometricPrompt(this, mainExecutor, authenticationCallback)
    }

    /**
     * Creates a new instance of PromptInfo for the BiometricPrompt.
     *
     * @return An instance of BiometricPrompt.PromptInfo
     */
    private fun generatePromptInfo() =
         BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_promptinfo_title))
            .setSubtitle(getString(R.string.biometric_promptinfo_subtitle))
            .setDescription(getString(R.string.biometric_promptinfo_description))
            .setNegativeButtonText(getString(R.string.biometric_promptinfo_negativebutton))
            .build()

    /**
     * Convenience method that displays a simple Snackbar message.
     */
    private fun notifyUser(message: String) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show()
    }
}
