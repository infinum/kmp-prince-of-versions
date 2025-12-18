@file:Suppress("unused")

package com.infinum.princeofversions

import kotlin.experimental.ExperimentalObjCName

public fun povExposeappversionforunittests(): String =
    IosApplicationVersionProvider().getVersion()

@OptIn(ExperimentalObjCName::class)
@ObjCName("POVTestHooks", exact = true)
public object TestHooks {
    @ObjCName("exposeAppVersionForUnitTests")
    public fun exposeAppVersionForUnitTests(): String =
        IosApplicationVersionProvider().getVersion()
}
