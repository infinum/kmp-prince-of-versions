package com.infinum.princeofversions

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Duration
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue

internal class IosDefaultLoader(
    private val url: String,
    private val username: String?,
    private val password: String?,
    networkTimeout: Duration,
    private val headers: Map<String, String> = emptyMap(),
) : Loader {

    private val timeoutSeconds: Double =
        (networkTimeout.inWholeMilliseconds.toDouble() / MILLIS_PER_SECOND).coerceAtLeast(MIN_TIMEOUT_SECONDS)

    override suspend fun load(): String = suspendCancellableCoroutine { cont ->
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            cont.resumeWithException(IoException("Invalid URL: $url"))
        } else {
            val request = createRequest(nsUrl)

            val config = NSURLSessionConfiguration.defaultSessionConfiguration().apply {
                timeoutIntervalForRequest = timeoutSeconds
                timeoutIntervalForResource = timeoutSeconds
            }

            val session = NSURLSession.sessionWithConfiguration(config)

            val task = session.dataTaskWithRequest(request) { data, response, error ->
                handleTaskCallback(data, response, error, cont, session)
            }

            cont.invokeOnCancellation {
                task.cancel()
                session.invalidateAndCancel()
            }

            task.resume()
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    internal fun createRequest(nsUrl: NSURL): NSMutableURLRequest =
        NSMutableURLRequest.requestWithURL(nsUrl).apply {
            setHTTPMethod("GET")
            setTimeoutInterval(timeoutSeconds)
            // Bypass local cache for this request
            setCachePolicy(NSURLRequestReloadIgnoringLocalCacheData)
            headers.forEach { (name, value) -> setValue(value, forHTTPHeaderField = name) }
            // Applied after custom headers, so credentials take precedence over an `Authorization` entry.
            if (username != null && password != null) {
                val creds = "$username:$password"
                val auth = "Basic " + Base64.encode(creds.encodeToByteArray())
                setValue(auth, forHTTPHeaderField = "Authorization")
            }
        }

    private fun handleTaskCallback(
        data: NSData?,
        response: NSURLResponse?,
        error: NSError?,
        cont: Continuation<String>,
        session: NSURLSession,
    ) {
        fun cleanupSession(session: NSURLSession) {
            session.finishTasksAndInvalidate()
        }
        fun fail(msg: String) {
            cont.resumeWithException(IoException(msg))
            cleanupSession(session)
        }
        fun succeed(body: String) {
            cont.resume(body)
            cleanupSession(session)
        }

        val failure = buildFailureMessage(error, response)
        if (failure != null) {
            fail(failure)
        } else {
            succeed(decodeBody(data))
        }
    }

    companion object {
        private const val MILLIS_PER_SECOND = 1000.0
        private const val MIN_TIMEOUT_SECONDS = 1.0
        private const val STATUS_CODE_200 = 200
        private const val STATUS_CODE_299 = 299

        /** Returns a human-readable failure message or null if the response is OK (2xx). */
        fun buildFailureMessage(
            error: NSError?,
            response: NSURLResponse?,
        ): String? {
            if (error != null) return error.localizedDescription

            val http = response as? NSHTTPURLResponse ?: return "Expected HTTP URL response, but received a non-HTTP response."
            val code = http.statusCode.toInt()
            if (code !in STATUS_CODE_200..STATUS_CODE_299) {
                val msg = NSHTTPURLResponse.localizedStringForStatusCode(http.statusCode)
                return "HTTP $code: $msg"
            }
            return null
        }

        /** UTF-8 decode, empty string if no body. */
        @OptIn(BetaInteropApi::class)
        fun decodeBody(
            data: NSData?,
        ): String =
            if (data == null || data.length.toLong() == 0L) {
                ""
            } else {
                NSString.create(data, NSUTF8StringEncoding)?.toString().orEmpty()
            }
    }
}

internal actual fun provideDefaultLoader(
    url: String,
    username: String?,
    password: String?,
    networkTimeout: Duration,
    headers: Map<String, String>,
): Loader = IosDefaultLoader(
    url = url,
    username = username,
    password = password,
    networkTimeout = networkTimeout,
    headers = headers,
)
