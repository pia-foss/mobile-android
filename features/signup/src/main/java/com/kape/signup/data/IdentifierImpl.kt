package com.kape.signup.data

import android.content.Context
import android.provider.Settings

internal class IdentifierImpl(
    private val context: Context,
) : Identifier {

    // region Identifier
    override fun invoke(): Result<String> =
        Result.success(
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID,
            ),
        )
    // endregion
}