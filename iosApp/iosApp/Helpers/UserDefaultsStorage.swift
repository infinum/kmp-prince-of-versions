//
//  UserDefaultsStorage.swift
//  iosApp
//
//  Created by Filip Stojanovski on 24.11.25.
//

import Foundation
import PrinceOfVersions

final class UserDefaultsStorage: NSObject, BaseStorage {
    private let key = "demo.last_notified_version"

    func __getLastSavedVersion() async throws -> Any? {
        return UserDefaults.standard.string(forKey: key) as NSString?
    }

    func __saveVersion(version: Any?) async throws {
        if let version = version as? NSString {
            UserDefaults.standard.set(version as String, forKey: key)
        } else {
            UserDefaults.standard.removeObject(forKey: key)
        }
    }
}
