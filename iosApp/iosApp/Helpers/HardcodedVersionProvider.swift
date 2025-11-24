//
//  HardcodedVersionProvider.swift
//  iosApp
//
//  Created by Filip Stojanovski on 24.11.25.
//

import Foundation
import PrinceOfVersions

final class HardcodedVersionProvider: BaseApplicationVersionProvider {
    private let currentAppVersion = "0.9.0"

    func getVersion() -> Any? {
        return currentAppVersion
    }
}
