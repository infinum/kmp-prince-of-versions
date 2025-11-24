//
//  ExampleRequirementsChecker.swift
//  iosApp
//
//  Created by Filip Stojanovski on 24.11.25.
//

import Foundation
import PrinceOfVersions

final class ExampleRequirementsChecker: NSObject, RequirementChecker {

    private let threshold = 5

    func checkRequirements(value: String?) -> Bool {
        let n = Int(value ?? "") ?? 0
        return n >= threshold
    }
}
