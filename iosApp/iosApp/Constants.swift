//
//  Constants.swift
//  iosApp
//
//  Created by Filip Stojanovski on 13.11.25.
//

import Foundation

func isKotlin<T>(_ error: Error, _ type: T.Type) -> Bool {
    (error as NSError).kotlinException is T
}

public enum Constants {
    static let commonUsageUrl = "https://pastebin.com/raw/rPZ4iRJB"
    static let updateUrl = "https://pastebin.com/raw/KgAZQUb5"
    static let customCheckerUrl = "https://pastebin.com/raw/kGCTDRYn"
    static let minimumUrl = "https://pastebin.com/raw/k9V8D9UZ"

    static let checkerKey = "requiredNumberOfLetters"
    
    static let networkTimeout: UInt64 = 5_000
}

extension Constants {
    enum JSON {
        static let minimumVersion = """
        {
            "minimum_version": "1.0.0"
        }
        """
    }
}
