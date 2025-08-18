package com.infinum.princeofversions

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

public sealed class MyResult {
    public data class Success<T>(val value: T) : MyResult()
    public data class Failure(
        val code: Int,
        val reason: String,
        val details: Map<String, String>
    ) : MyResult()
}

public class MyApi {
    public suspend fun fetchSomething(): String {
        delay(1000) // Simulate async
        return "Hello from KMP"
    }

    @Throws(IllegalArgumentException::class)
    public fun mightFail(input: String): String {
        if (input.isBlank()) {
            throw IllegalArgumentException("Input must not be blank")
        }
        return "Valid: $input"
    }

    @Throws(IllegalArgumentException::class, CancellationException::class)
    public suspend fun mightFailWithDelay(input: String): String {
        delay(1000) // Simulate async
        if (input.isBlank()) {
            throw CancellationException("Input must not be blank")
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
            MyResult.Failure(422, "Invalid input", mapOf("field" to "input", "hint" to "Provide a non-empty string"))
        }
    }

    public suspend fun fetchSafe(): MyResult {
        return try {
            MyResult.Success(riskyCall())
        } catch (e: Exception) {
            MyResult.Failure(422, "Invalid input", mapOf("field" to "input", "hint" to "Provide a non-empty string"))
        }
    }

}