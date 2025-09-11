package com.infinum.princeofversions

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

public sealed class MyResult {
    public data class Success(val data: String) : MyResult()
    public data class Failure(val message: String) : MyResult()
}

public class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

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

    @Throws(ApiException::class, CancellationException::class)
    public suspend fun loadData(): String {
        return try {
            val response = HttpClient().get("https://www.google.com/asdfghjkl")
            if (!response.status.isSuccess()) {
                throw ApiException("Backend error: ${response.status}")
            }
            response.bodyAsText()
        } catch (e: ResponseException) {
            throw ApiException("HTTP error: ${e.response.status}", e)
        } catch (e: IOException) {
            throw ApiException("Network error", e)
        }
    }


    @Throws(IllegalArgumentException::class, CancellationException::class)
    public suspend fun mightFailWithDelay(input: String): String {
        delay(1000) // Simulate async
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