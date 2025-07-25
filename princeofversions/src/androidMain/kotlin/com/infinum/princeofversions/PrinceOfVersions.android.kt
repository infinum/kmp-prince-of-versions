package com.infinum.princeofversions

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher

// This suppression should be removed once the 'context' property is used in the implementation.
@Suppress("UnusedPrivateProperty")
public actual class PrinceOfVersions(context: Context) {
    public actual fun checkForUpdates(
        source: ConfigSource, callback: UpdateCallback, dispatcher: CoroutineDispatcher
    ): Cancelable {
        TODO("Not yet implemented")
    }

    public actual fun newCall(source: ConfigSource): PrinceOfVersionsCall {
        TODO("Not yet implemented")
    }
}
