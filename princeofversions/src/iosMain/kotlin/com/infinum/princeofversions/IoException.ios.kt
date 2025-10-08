package com.infinum.princeofversions

public actual class IoException actual constructor(
    message: String?,
    cause: Throwable?
) : Exception(message, cause)