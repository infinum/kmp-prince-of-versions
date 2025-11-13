//
//  Constants.swift
//  iosApp
//
//  Created by Filip Stojanovski on 13.11.25.
//

import Foundation

public enum Constants {
    static let updateUrl = "https://pastebin.com/raw/KgAZQUb5"
    static let customParserUrl = "https://pastebin.com/raw/9mJALk0n"

    static let checkerKey = "requiredNumberOfLetters"
    
    static let networkTimeout: UInt64 = 5_000
}

extension Constants {
    enum JSON {
        static let demoIos2JSON = """
        {
          "meta": { "channel": "prod" },
          "ios2": {
            "required_version": "1.2.3",
            "last_version_available": "1.3.0",
            "notify_last_version_frequency": "ALWAYS",
            "requirements": {},
            "meta": { "notes": "flat ios2 example" }
          }
        }
        """

        static let demoNestedIOSJSON = """
        {
          "meta": { "channel": "prod" },
          "ios": {
            "minimum_version": "1.0.0",
            "latest_version": {
              "version": "1.1.0",
              "notification_type": "ALWAYS"
            }
          }
        }
        """
    }
}
