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

            // Basic auth header, if provided
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
                // ✅ Tidy up the per-request session
                session.finishTasksAndInvalidate()
            }
        }

        cont.invokeOnCancellation {
            // ✅ Cancel the task and tear down the session immediately
            task.cancel()
            session.invalidateAndCancel()
        }

        task.resume()

    }
}

/** NSData -> UTF-8 string (empty if decoding fails). */
//private fun NSData.toUtf8String(): String =
//    NSString.create(this, NSUTF8StringEncoding)?.toString() ?: ""

private fun decodeBody(
    data: NSData,
    response: NSHTTPURLResponse?
): String {
    // 1) Try UTF-8
    NSString.create(data, NSUTF8StringEncoding)?.toString()?.let { return it }

    // 2) Fallback: ISO-8859-1
    NSString.create(data, NSISOLatin1StringEncoding)?.toString()?.let { return it }

    // 3) Log and fail clearly
    val headersDesc = response?.allHeaderFields?.toString() ?: "<no headers>"
    NSLog(
        "PrinceOfVersions: failed to decode body. status=%ld, bytes=%ld, headers=%@",
        response?.statusCode ?: -1L,
        data.length.toLong(),
        headersDesc
    )
    throw IoException("Failed to decode HTTP body (unsupported encoding).")
}

/** iOS actual for factory */
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
