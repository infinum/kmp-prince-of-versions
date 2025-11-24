//
//  DeveloperBuildVersionComparator.swift
//  iosApp
//
//  Created by Filip Stojanovski on 24.11.25.
//

import Foundation
import PrinceOfVersions

final class DeveloperBuildVersionComparator: BaseVersionComparator {
    func compare(firstVersion: Any?, secondVersion: Any?) -> Int32 {
        guard
            let firstVersion = firstVersion as? String,
            let secondVersion = secondVersion as? String
        else { return 0 }

        // If either fails to parse, don’t trigger updates.
        guard let parsedFirstVersion = parse(firstVersion), let parsedSecondVersion = parse(secondVersion) else { return 0 }

        if parsedFirstVersion.0 != parsedSecondVersion.0 { return parsedFirstVersion.0 > parsedSecondVersion.0 ? 1 : -1 } // major
        if parsedFirstVersion.1 != parsedSecondVersion.1 { return parsedFirstVersion.1 > parsedSecondVersion.1 ? 1 : -1 } // minor
        if parsedFirstVersion.2 != parsedSecondVersion.2 { return parsedFirstVersion.2 > parsedSecondVersion.2 ? 1 : -1 } // patch
        return 0
    }

    // "1.2.3" -> (1,2,3) missing parts become 0
    private func parse(_ version: String) -> (Int, Int, Int)? {
        let parts = version.split(separator: ".").prefix(3).map { Int($0) ?? 0 }
        guard !parts.isEmpty else { return nil }
        return (parts.count > 0 ? parts[0] : 0,
                parts.count > 1 ? parts[1] : 0,
                parts.count > 2 ? parts[2] : 0)
    }
}
