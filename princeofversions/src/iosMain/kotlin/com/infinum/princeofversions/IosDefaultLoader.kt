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
) : Loader {

    private val timeoutSeconds: Double =
        networkTimeout.inWholeSeconds.toDouble()

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun load(): String = suspendCancellableCoroutine { cont ->
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            cont.resumeWithException(IoException("Invalid URL: $url"))
        } else {
            val request = NSMutableURLRequest.requestWithURL(nsUrl).apply {
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
                handleTaskCallback(data, response, error, cont, session)
            }

            cont.invokeOnCancellation {
                task.cancel()
                session.invalidateAndCancel()
            }

            task.resume()
        }
    }

    private fun handleTaskCallback(
        data: NSData?,
        response: NSURLResponse?,
        error: NSError?,
        cont: Continuation<String>,
        session: NSURLSession,
    ) {
        fun fail(msg: String) {
            cont.resumeWithException(IoException(msg))
            session.finishTasksAndInvalidate()
        }
        fun succeed(body: String) {
            cont.resume(body)
            session.finishTasksAndInvalidate()
        }

        val failure = buildFailureMessage(error, response)
        if (failure != null) {
            fail(failure)
        } else {
            succeed(decodeBody(data))
        }
    }

    /** Returns a human-readable failure message or null if the response is OK (2xx). */
    private fun buildFailureMessage(
        error: NSError?,
        response: NSURLResponse?,
    ): String? {
        if (error != null) return error.localizedDescription

        val http = response as? NSHTTPURLResponse ?: return "No HTTP response"
        val code = http.statusCode.toInt()
        if (code !in STATUS_CODE_200..STATUS_CODE_299) {
            val msg = NSHTTPURLResponse.localizedStringForStatusCode(http.statusCode)
            return "HTTP $code: $msg"
        }
        return null
    }

    /** UTF-8 decode, empty string if no body. */
    @OptIn(BetaInteropApi::class)
    private fun decodeBody(
        data: NSData?,
    ): String =
        if (data == null || data.length.toLong() == 0L) {
            ""
        } else {
            NSString.create(data, NSUTF8StringEncoding)?.toString().orEmpty()
        }

    companion object {
        private const val STATUS_CODE_200 = 200
        private const val STATUS_CODE_299 = 299
    }
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
