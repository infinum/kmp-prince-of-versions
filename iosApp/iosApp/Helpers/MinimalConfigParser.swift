//
//  MinimalConfigParser.swift
//  iosApp
//
//  Created by Filip Stojanovski on 24.11.25.
//

import Foundation
import PrinceOfVersions

final class MinimalConfigParser: NSObject, BaseConfigurationParser {
    func parse(value: String) throws -> BasePrinceOfVersionsConfig<AnyObject> {
        guard
            let data = value.data(using: .utf8),
            let obj  = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else {
            throw NSError(
                domain: "pov.customparser",
                code: 1000,
                userInfo: [NSLocalizedDescriptionKey: "Invalid JSON"]
            )
        }

        let min: String? =
        (obj["minimum_version"] as? String)
        ?? ((obj["ios"] as? [String: Any])?["minimum_version"] as? String)

        guard let minimum = min, !minimum.isEmpty else {
            throw NSError(
                domain: "pov.customparser",
                code: 1001,
                userInfo: [NSLocalizedDescriptionKey: "Invalid custom JSON (expected minimum_version)"]
            )
        }

        return BasePrinceOfVersionsConfig(
            mandatoryVersion: minimum as NSString,
            optionalVersion: (obj["ios"] as? [String: Any])
                .flatMap { $0["latest_version"] as? [String: Any] }?["version"] as? NSString,
            optionalNotificationType: NotificationType.always,
            metadata: (obj["meta"] as? [String: Any]) as? [String: NSString] ?? [:],
            requirements: [:]
        )
    }
}
