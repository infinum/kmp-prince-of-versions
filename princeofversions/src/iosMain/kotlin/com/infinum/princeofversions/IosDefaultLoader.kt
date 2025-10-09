package com.infinum.princeofversions

import kotlin.time.Duration
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import platform.Foundation.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal class IosDefaultLoader(
    private val url: String,
    private val username: String?,
    private val password: String?,
    networkTimeout: Duration,
) : Loader {

    private val timeoutSeconds: Double =
        (networkTimeout.inWholeMilliseconds.toDouble() / 1000.0).coerceAtLeast(0.0)

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun load(): String = suspendCancellableCoroutine { cont ->
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            cont.resumeWithException(IoException("Invalid URL: $url"))
            return@suspendCancellableCoroutine
        }

        val request = platform.Foundation.NSMutableURLRequest.requestWithURL(nsUrl).apply {
            setHTTPMethod("GET")
            setTimeoutInterval(timeoutSeconds)

            if (username != null && password != null) {
                val creds = "$username:$password"
                val auth = "Basic " + Base64.encode(creds.encodeToByteArray())
                setValue(auth, forHTTPHeaderField = "Authorization")
            }
        }

        val config = NSURLSessionConfiguration.defaultSessionConfiguration().apply {
            timeoutIntervalForRequest = timeoutSeconds
            timeoutIntervalForResource = timeoutSeconds
        }

        val session = NSURLSession.sessionWithConfiguration(config)
        val task = session.dataTaskWithRequest(request) { data, response, error ->
            try {
                when {
                    error != null -> {
                        cont.resumeWithException(IoException(error.localizedDescription ?: "Network error"))
                    }
                    response !is NSHTTPURLResponse -> {
                        cont.resumeWithException(IoException("No HTTP response"))
                    }
                    response.statusCode.toInt() !in 200..299 -> {
                        val msg = NSHTTPURLResponse.localizedStringForStatusCode(response.statusCode)
                        cont.resumeWithException(IoException("HTTP ${response.statusCode}: $msg"))
                    }
                    else -> {
                        val body = when {
                            data == null || data.length.toLong() == 0L -> ""
                            else -> decodeBody(data, response as? NSHTTPURLResponse)
                        }
                        cont.resume(body)
                    }
                }
            } finally {
                session.finishTasksAndInvalidate()
            }
        }

        cont.invokeOnCancellation {
            task.cancel()
            session.invalidateAndCancel()
        }

        task.resume()

    }
}

private fun decodeBody(
    data: NSData,
    response: NSHTTPURLResponse?
): String {
    NSString.create(data, NSUTF8StringEncoding)?.toString()?.let { return it }

    NSString.create(data, NSISOLatin1StringEncoding)?.toString()?.let { return it }

    val headersDesc = response?.allHeaderFields?.toString() ?: "<no headers>"
    NSLog(
        "PrinceOfVersions: failed to decode body. status=%ld, bytes=%ld, headers=%@",
        response?.statusCode ?: -1L,
        data.length.toLong(),
        headersDesc
    )
    throw IoException("Failed to decode HTTP body (unsupported encoding).")
}

internal actual fun provideDefaultLoader(
    url: String,
    username: String?,
    password: String?,
    networkTimeout: Duration,
): Loader = IosDefaultLoader(
    url = url,
    username = username,
    password = password,
    networkTimeout = networkTimeout,
)
