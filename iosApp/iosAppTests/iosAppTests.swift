//
//  iosAppTests.swift
//  iosAppTests
//
//  Created by Filip Stojanovski on 5.11.25.
//

import Network
import Testing
import XCTest
import PrinceOfVersions

final class VersionProviderHostTests: XCTestCase {
    func test_versionProvider_shouldReadVersionFromHostInfoPlist() {
        // Read version directly from test app's Info.plist
        guard let shortVersion = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String,
              let buildVersion = Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String else {
            XCTFail("Failed to read version from Info.plist")
            return
        }
        let version = "\(shortVersion)-\(buildVersion)"
        XCTAssertFalse(version.isEmpty)
        XCTAssertTrue(version.contains("-"), "Expected short-build format like 1.2.3-456; got \(version)")
    }
}

final class NetworkTimeoutIntervalTests: XCTestCase {
    // The loader never waits less than 1 second. When the timeout was passed in the wrong unit,
    // every request gave up after that 1 second, so these tests check the request waits ~3 seconds.
    private let networkTimeoutInterval: TimeInterval = 3

    func test_checkForUpdates_shouldTimeOutAfterNetworkTimeoutInterval() async throws {
        let server = try SilentServer()
        let port = try await server.start()
        defer { server.stop() }

        let pov = IosPrinceOfVersionsKt.makePrinceOfVersions()
        let start = Date()

        do {
            _ = try await IosPrinceOfVersionsKt.checkForUpdates(
                pov,
                from: "http://localhost:\(port)/config.json",
                username: nil,
                password: nil,
                networkTimeoutInterval: networkTimeoutInterval
            )
            XCTFail("Expected the request to time out")
        } catch {
            assertTimedOutAfterNetworkTimeoutInterval(since: start)
        }
    }

    func test_checkForUpdatesWithHeaders_shouldTimeOutAfterNetworkTimeoutInterval() async throws {
        let server = try SilentServer()
        let port = try await server.start()
        defer { server.stop() }

        let pov = IosPrinceOfVersionsKt.makePrinceOfVersions()
        let start = Date()

        do {
            _ = try await pov.checkForUpdates(
                from: "http://localhost:\(port)/config.json",
                headers: ["x-api-key": "test-key"],
                username: nil,
                password: nil,
                networkTimeoutInterval: networkTimeoutInterval
            )
            XCTFail("Expected the request to time out")
        } catch {
            assertTimedOutAfterNetworkTimeoutInterval(since: start)
        }
    }

    private func assertTimedOutAfterNetworkTimeoutInterval(
        since start: Date,
        file: StaticString = #filePath,
        line: UInt = #line
    ) {
        let elapsed = Date().timeIntervalSince(start)
        XCTAssertGreaterThanOrEqual(elapsed, networkTimeoutInterval - 0.5, "Timed out after \(elapsed) s", file: file, line: line)
        XCTAssertLessThan(elapsed, 30, "Timed out after \(elapsed) s", file: file, line: line)
    }
}

/// Accepts connections on localhost and never answers, so requests to it time out.
private final class SilentServer {
    private let listener: NWListener
    private var connections: [NWConnection] = []

    init() throws {
        listener = try NWListener(using: .tcp, on: .any)
    }

    func start() async throws -> UInt16 {
        listener.newConnectionHandler = { [weak self] connection in
            connection.start(queue: .main)
            self?.connections.append(connection)
        }

        return try await withCheckedThrowingContinuation { continuation in
            listener.stateUpdateHandler = { [listener] state in
                switch state {
                case .ready:
                    listener.stateUpdateHandler = nil
                    continuation.resume(returning: listener.port?.rawValue ?? 0)
                case .failed(let error):
                    listener.stateUpdateHandler = nil
                    continuation.resume(throwing: error)
                default:
                    break
                }
            }
            listener.start(queue: .main)
        }
    }

    func stop() {
        connections.forEach { $0.cancel() }
        listener.cancel()
    }
}
