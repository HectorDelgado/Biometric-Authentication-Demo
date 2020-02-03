package com.hectordelgado.biometricauthentication

/**
 *  Biometric Authentication
 *  File: CaesarCipher.kt
 *
 *  @author Hector Delgado
 *
 *  Created on February 01, 2020.
 *  Copyright © 2020 Hector Delgado. All rights reserved.
 *
 *  A simple cipher based on the substitution cipher by Julius Caesar.
 */
class CaesarCipher(private val cipherShift: Int) {

    /**
     * Shifts the characters of the unencrypted message to the right.
     *
     * @return the encrypted message.
     */
    fun encryptMessage(plainText: String): String {
        var cipher = ""

        for (c in plainText) {
            cipher += (c + cipherShift).toString()
        }

        return cipher
    }

    /**
     * Shifts the characters of the encrypted message to the left.
     *
     * @return the decrypted message.
     */
    fun decryptMessage(cipher: String): String {
        var plainText = ""

        for (c in cipher) {
            plainText += (c - cipherShift).toString()
        }

        return plainText
    }
}