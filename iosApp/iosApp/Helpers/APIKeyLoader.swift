//
//  APIKeyLoader.swift
//  iosApp
//

import Foundation
import PrinceOfVersions

/// A custom Loader implementation that supports custom HTTP headers.
///
/// This demonstrates how to migrate from the old `PoVRequestOptions` API
/// to the new library by implementing a custom `Loader`.
///
/// Example usage with API key authentication:
/// ```swift
/// let loader = APIKeyLoader(
///     url: URL(string: "https://example.com/config.json")!,
///     headers: ["x-api-key": AppSecrets.Networking.apiKey]
/// )
/// let result = try await pov.checkForUpdates(source: loader)
/// ```
final class APIKeyLoader: NSObject, Loader {
    private let url: URL
    private let headers: [String: String]
    private let timeout: TimeInterval

    /// Creates a new loader with custom headers.
    /// - Parameters:
    ///   - url: The URL to fetch the configuration from.
    ///   - headers: A dictionary of HTTP headers to include in the request.
    ///   - timeout: The request timeout in seconds. Defaults to 60 seconds.
    init(url: URL, headers: [String: String] = [:], timeout: TimeInterval = 60) {
        self.url = url
        self.headers = headers
        self.timeout = timeout
    }

    /// Convenience initializer for API key authentication.
    /// - Parameters:
    ///   - url: The URL to fetch the configuration from.
    ///   - apiKey: The API key to include in the `x-api-key` header.
    ///   - timeout: The request timeout in seconds. Defaults to 60 seconds.
    convenience init(url: URL, apiKey: String, timeout: TimeInterval = 60) {
        self.init(url: url, headers: ["x-api-key": apiKey], timeout: timeout)
    }

    func __load() async throws -> String {
        var request = URLRequest(url: url, timeoutInterval: timeout)
        request.httpMethod = "GET"
        request.cachePolicy = .reloadIgnoringLocalCacheData

        for (key, value) in headers {
            request.setValue(value, forHTTPHeaderField: key)
        }

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw NSError(
                domain: "APIKeyLoader",
                code: -1,
                userInfo: [NSLocalizedDescriptionKey: "Expected HTTP response"]
            )
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            throw NSError(
                domain: "APIKeyLoader",
                code: httpResponse.statusCode,
                userInfo: [
                    NSLocalizedDescriptionKey: "HTTP \(httpResponse.statusCode): \(HTTPURLResponse.localizedString(forStatusCode: httpResponse.statusCode))",
                ]
            )
        }

        return String(data: data, encoding: .utf8) ?? ""
    }
}
