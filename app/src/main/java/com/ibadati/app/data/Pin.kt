package com.ibadati.app.data

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** The sins-tab PIN is never stored, only a salted PBKDF2 hash of it. */
object Pin {
    private const val ITERATIONS = 20_000

    private fun derive(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    private fun b64(b: ByteArray) = Base64.encodeToString(b, Base64.NO_WRAP)

    fun hash(pin: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return b64(salt) + ":" + b64(derive(pin, salt))
    }

    fun verify(pin: String, stored: String?): Boolean {
        if (stored == null) return false
        val parts = stored.split(":")
        if (parts.size != 2) return false
        return try {
            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val expected = Base64.decode(parts[1], Base64.NO_WRAP)
            MessageDigest.isEqual(expected, derive(pin, salt))
        } catch (e: Exception) {
            false
        }
    }
}
