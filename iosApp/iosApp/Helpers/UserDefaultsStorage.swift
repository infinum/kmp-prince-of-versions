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

    func getLastSavedVersion(completionHandler: @escaping (Any?, Error?) -> Void) {
        let value = UserDefaults.standard.string(forKey: key) as NSString?
        completionHandler(value, nil)
    }

    func saveVersion(version: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let version = version as? NSString {
            UserDefaults.standard.set(version as String, forKey: key)
        } else {
            UserDefaults.standard.removeObject(forKey: key)
        }
        completionHandler(nil)
    }
}
