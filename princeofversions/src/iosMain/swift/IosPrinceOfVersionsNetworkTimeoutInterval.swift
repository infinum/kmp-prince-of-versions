import Foundation

// Kotlin's `Duration` reaches Swift as its internal raw value, not as milliseconds or seconds.
// These wrappers take the timeout in seconds, like `URLRequest.timeoutInterval`, and replace the
// Kotlin functions, which are refined for Swift.
extension PrinceOfVersionsBase {

    /// Starts a check for an update, loading the configuration from a URL.
    ///
    /// - Parameters:
    ///   - url: The network URL from which to load the update configuration.
    ///   - username: Optional username for basic authentication.
    ///   - password: Optional password for basic authentication.
    ///   - networkTimeoutInterval: The network timeout, in seconds.
    /// - Returns: The result of the update check.
    public func checkForUpdates(
        from url: String,
        username: String?,
        password: String?,
        networkTimeoutInterval: TimeInterval
    ) async throws -> BaseUpdateResult<NSString> {
        try await __checkForUpdates(
            from: url,
            username: username,
            password: password,
            timeout: __networkTimeout(seconds: networkTimeoutInterval)
        )
    }

    /// Starts a check for an update, sending `headers` with the configuration request.
    ///
    /// If `username` and `password` are provided, their basic authentication `Authorization` header
    /// replaces any `Authorization` entry in `headers`.
    ///
    /// - Parameters:
    ///   - url: The network URL from which to load the update configuration.
    ///   - headers: Additional HTTP headers sent with the request, e.g. `["x-api-key": apiKey]`.
    ///   - username: Optional username for basic authentication.
    ///   - password: Optional password for basic authentication.
    ///   - networkTimeoutInterval: The network timeout, in seconds.
    /// - Returns: The result of the update check.
    public func checkForUpdates(
        from url: String,
        headers: [String: String],
        username: String?,
        password: String?,
        networkTimeoutInterval: TimeInterval
    ) async throws -> BaseUpdateResult<NSString> {
        try await __checkForUpdates(
            from: url,
            headers: headers,
            username: username,
            password: password,
            timeout: __networkTimeout(seconds: networkTimeoutInterval)
        )
    }
}

extension IosPrinceOfVersionsKt {

    /// Starts a check for an update, loading the configuration from a URL.
    ///
    /// - Parameters:
    ///   - princeOfVersions: The instance that runs the check.
    ///   - url: The network URL from which to load the update configuration.
    ///   - username: Optional username for basic authentication.
    ///   - password: Optional password for basic authentication.
    ///   - networkTimeoutInterval: The network timeout, in seconds.
    /// - Returns: The result of the update check.
    public static func checkForUpdates(
        _ princeOfVersions: PrinceOfVersionsBase,
        from url: String,
        username: String?,
        password: String?,
        networkTimeoutInterval: TimeInterval
    ) async throws -> BaseUpdateResult<NSString> {
        try await princeOfVersions.checkForUpdates(
            from: url,
            username: username,
            password: password,
            networkTimeoutInterval: networkTimeoutInterval
        )
    }

    /// Starts a check for an update, sending `headers` with the configuration request.
    ///
    /// If `username` and `password` are provided, their basic authentication `Authorization` header
    /// replaces any `Authorization` entry in `headers`.
    ///
    /// - Parameters:
    ///   - princeOfVersions: The instance that runs the check.
    ///   - url: The network URL from which to load the update configuration.
    ///   - headers: Additional HTTP headers sent with the request, e.g. `["x-api-key": apiKey]`.
    ///   - username: Optional username for basic authentication.
    ///   - password: Optional password for basic authentication.
    ///   - networkTimeoutInterval: The network timeout, in seconds.
    /// - Returns: The result of the update check.
    public static func checkForUpdates(
        _ princeOfVersions: PrinceOfVersionsBase,
        from url: String,
        headers: [String: String],
        username: String?,
        password: String?,
        networkTimeoutInterval: TimeInterval
    ) async throws -> BaseUpdateResult<NSString> {
        try await princeOfVersions.checkForUpdates(
            from: url,
            headers: headers,
            username: username,
            password: password,
            networkTimeoutInterval: networkTimeoutInterval
        )
    }
}
