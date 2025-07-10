package com.infinum.princeofversions

import kotlinx.coroutines.delay

public class MyApi {
    public suspend fun fetchSomething(): String {
        delay(1000) // Simulate async
        return "Hello from KMP"
    }
}