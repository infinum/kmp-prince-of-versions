//
//  PinnedCertificateLoader.swift
//  iosApp
//

import Foundation
import PrinceOfVersions
import Security

/// A custom Loader implementation that pins the server's certificate.
///
/// This replaces `PoVRequestOptions.shouldPinCertificates` from the native iOS library.
/// Like that option, it reads every `.cer`, `.crt` and `.der` file in the bundle and only
/// accepts the server if its certificate has the same public key as one of them.
/// Unlike that option, the server's certificate must also pass the system's normal trust
/// checks (valid chain, not expired, matching host name).
///
/// If pinning fails, the request fails with `URLError.cancelled`.
///
/// Example usage:
/// ```swift
/// let loader = PinnedCertificateLoader(
///     url: URL(string: "https://example.com/config.json")!
/// )
/// let result = try await pov.checkForUpdates(source: loader)
/// ```
final class PinnedCertificateLoader: NSObject, Loader {
    private let url: URL
    private let timeout: TimeInterval
    private let pinnedKeys: Set<Data>

    /// Creates a new loader that pins the certificates found in a bundle.
    /// - Parameters:
    ///   - url: The URL to fetch the configuration from.
    ///   - bundle: The bundle that contains the pinned certificates. Defaults to the main bundle.
    ///   - timeout: The request timeout in seconds. Defaults to 60 seconds.
    init(url: URL, bundle: Bundle = .main, timeout: TimeInterval = 60) {
        self.url = url
        self.timeout = timeout
        self.pinnedKeys = Self.publicKeys(ofCertificatesIn: bundle)
    }

    func __load() async throws -> String {
        guard !pinnedKeys.isEmpty else {
            throw NSError(
                domain: "PinnedCertificateLoader",
                code: -1,
                userInfo: [NSLocalizedDescriptionKey: "No .cer, .crt or .der certificates found to pin"]
            )
        }

        let session = URLSession(
            configuration: .ephemeral,
            delegate: PinningDelegate(pinnedKeys: pinnedKeys),
            delegateQueue: nil
        )
        // A session keeps a strong reference to its delegate until it is invalidated.
        defer { session.finishTasksAndInvalidate() }

        let request = URLRequest(url: url, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: timeout)
        let (data, response) = try await session.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw NSError(
                domain: "PinnedCertificateLoader",
                code: -1,
                userInfo: [NSLocalizedDescriptionKey: "Expected HTTP response"]
            )
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            throw NSError(
                domain: "PinnedCertificateLoader",
                code: httpResponse.statusCode,
                userInfo: [
                    NSLocalizedDescriptionKey: "HTTP \(httpResponse.statusCode): \(HTTPURLResponse.localizedString(forStatusCode: httpResponse.statusCode))",
                ]
            )
        }

        return String(data: data, encoding: .utf8) ?? ""
    }

    private static func publicKeys(ofCertificatesIn bundle: Bundle) -> Set<Data> {
        let paths = ["cer", "CER", "crt", "CRT", "der", "DER"]
            .flatMap { bundle.paths(forResourcesOfType: $0, inDirectory: nil) }

        return Set(
            Set(paths)
                .compactMap { FileManager.default.contents(atPath: $0) }
                .compactMap { SecCertificateCreateWithData(nil, $0 as CFData) }
                .compactMap { publicKeyData(of: $0) }
        )
    }

    fileprivate static func publicKeyData(of certificate: SecCertificate) -> Data? {
        guard let key = SecCertificateCopyKey(certificate) else { return nil }
        return SecKeyCopyExternalRepresentation(key, nil) as Data?
    }
}

/// Accepts the server only if its certificate passes the system's trust checks
/// and its public key matches one of the pinned keys.
private final class PinningDelegate: NSObject, URLSessionDelegate {
    private let pinnedKeys: Set<Data>

    init(pinnedKeys: Set<Data>) {
        self.pinnedKeys = pinnedKeys
    }

    func urlSession(
        _ session: URLSession,
        didReceive challenge: URLAuthenticationChallenge
    ) async -> (URLSession.AuthChallengeDisposition, URLCredential?) {
        guard
            challenge.protectionSpace.authenticationMethod == NSURLAuthenticationMethodServerTrust,
            let trust = challenge.protectionSpace.serverTrust
        else {
            return (.performDefaultHandling, nil)
        }

        guard
            SecTrustEvaluateWithError(trust, nil),
            let serverCertificate = (SecTrustCopyCertificateChain(trust) as? [SecCertificate])?.first,
            let serverKey = PinnedCertificateLoader.publicKeyData(of: serverCertificate),
            pinnedKeys.contains(serverKey)
        else {
            return (.cancelAuthenticationChallenge, nil)
        }

        return (.useCredential, URLCredential(trust: trust))
    }
}
