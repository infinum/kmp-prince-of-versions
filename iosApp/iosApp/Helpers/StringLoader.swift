//
//  StringLoader.swift
//  iosApp
//
//  Created by Filip Stojanovski on 24.11.25.
//

import Foundation
import PrinceOfVersions

final class StringLoader: NSObject, Loader {
    let payload: String
    init(payload: String) { self.payload = payload }

    func __load() async throws -> String {
        return payload

    }
}
