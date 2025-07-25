package com.infinum.princeofversions

import kotlinx.coroutines.CoroutineDispatcher

public actual class PrinceOfVersions {
    public actual fun checkForUpdates(source: ConfigSource, callback: UpdateCallback, dispatcher: CoroutineDispatcher): Cancelable {
        TODO("Not yet implemented")
    }

    public actual fun newCall(source: ConfigSource): PrinceOfVersionsCall {
        TODO("Not yet implemented")
    }
}
