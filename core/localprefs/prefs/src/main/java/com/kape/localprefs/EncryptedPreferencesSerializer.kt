package com.kape.localprefs

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.emptyPreferences
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import okio.Buffer
import okio.buffer
import okio.source
import java.io.InputStream
import java.io.OutputStream

@Suppress("RestrictedApi")
internal class EncryptedPreferencesSerializer(
    context: Context,
    name: String,
) : Serializer<Preferences> {
    override val defaultValue: Preferences = emptyPreferences()

    private val aead: Aead? =
        try {
            buildAead(context, name)
        } catch (e: Exception) {
            null
        }

    override suspend fun readFrom(input: InputStream): Preferences {
        val bytes = input.readBytes()
        if (bytes.isEmpty() || aead == null) return defaultValue
        return try {
            val decrypted = aead.decrypt(bytes, null)
            PreferencesSerializer.readFrom(decrypted.inputStream().source().buffer())
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(
        t: Preferences,
        output: OutputStream,
    ) {
        val aead = aead ?: return
        val buffer = Buffer()
        PreferencesSerializer.writeTo(t, buffer)
        output.write(aead.encrypt(buffer.readByteArray(), null))
    }

    private companion object {
        fun buildAead(
            context: Context,
            name: String,
        ): Aead {
            AeadConfig.register()
            return AndroidKeysetManager
                .Builder()
                .withSharedPref(context, "tink_keyset_$name", "tink_keysets")
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri("android-keystore://datastore_master_key")
                .build()
                .keysetHandle
                .getPrimitive(Aead::class.java)
        }
    }
}