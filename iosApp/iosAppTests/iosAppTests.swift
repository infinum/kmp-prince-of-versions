//
//  iosAppTests.swift
//  iosAppTests
//
//  Created by Filip Stojanovski on 5.11.25.
//

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
