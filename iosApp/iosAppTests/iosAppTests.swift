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
    func testReadsVersionFromHostInfoPlist() {
        let version = TestHooksKt.povExposeappversionforunittests()
        XCTAssertFalse(version.isEmpty)
        XCTAssertTrue(version.contains("-"), "Expected short-build format like 1.2.3; got \(version)")
    }
}
