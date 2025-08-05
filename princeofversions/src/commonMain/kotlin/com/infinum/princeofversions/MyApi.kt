package com.infinum.princeofversions

import kotlinx.coroutines.delay

public sealed class MyResult {
    public data class Success(val data: String) : MyResult()
    public data class Failure(val message: String) : MyResult()
}

public class MyApi {
    public suspend fun fetchSomething(): String {
        delay(1000) // Simulate async
        return "Hello from KMP"
    }

    public suspend fun mightFail(input: String): String {
        if (input.isBlank()) {
            throw IllegalArgumentException("Input must not be blank")
        }
        return "Valid: $input"
    }

    public suspend fun safeMightFail(input: String): Result<String> {
        return try {
            if (input.isBlank()) throw IllegalArgumentException("Input must not be blank")
            Result.success("Valid: $input")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    public suspend fun safeMightFailWithSealedClass(input: String): MyResult {
        return try {
            if (input.isBlank()) throw IllegalArgumentException("Input must not be blank")
            MyResult.Success("Valid: $input")
        } catch (e: Exception) {
            MyResult.Failure(e.message ?: "Unknown error")
        }
    }

    public suspend fun fetchSafe(): MyResult {
        return try {
            MyResult.Success(riskyCall())
        } catch (e: Exception) {
            MyResult.Failure("Caught error: ${e.message}")
        }
    }

}