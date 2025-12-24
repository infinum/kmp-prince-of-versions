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

    func load(completionHandler: @escaping (String?, Error?) -> Void) {
        completionHandler(payload, nil)
    }
}
