//
//  MainMenuView.swift
//  iosApp
//
//  Created by Filip Stojanovski on 6.11.25.
//

import SwiftUI

enum DemoRoute: String, CaseIterable, Identifiable {
    case commonUsage = "Common Usage"
    case customParser = "Custom Configuration Parser"
    case customChecker = "Custom Requirement Checker"
    case customStorage = "Custom Storage"
    case customVersionLogic = "Custom Version Logic"

    var id: String { rawValue }
}

struct MainMenuView: View {
    var body: some View {
        NavigationStack {
            List(DemoRoute.allCases) { route in
                NavigationLink(route.rawValue, value: route)
            }
            .navigationTitle("PrinceOfVersions")
            .navigationDestination(for: DemoRoute.self) { route in
                switch route {
                case .commonUsage:        CommonUsageView()
                case .customParser:       CustomParserView()
                case .customChecker:      CustomCheckerView()
                case .customStorage:      CustomStorageView()
                case .customVersionLogic: CustomVersionLogicView()
                }
            }
        }
    }
}
