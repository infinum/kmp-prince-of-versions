//
//  CustomVersionLogicViewModel.swift
//  iosApp
//
//  Created by Filip Stojanovski on 13.11.25.
//

import Foundation
import PrinceOfVersions

final class HardcodedVersionProvider: BaseApplicationVersionProvider {
    private let currentAppVersion = "1.2.3"

    func getVersion() -> Any? {
        return currentAppVersion
    }
}

final class DeveloperBuildVersionComparator: BaseVersionComparator {
    func compare(firstVersion: Any?, secondVersion: Any?) -> Int32 {
        guard
            let firstStr = firstVersion as? String,
            let secondStr = secondVersion as? String,
            let first = Int(firstStr),
            let second = Int(secondStr)
        else {
            return 0
        }

        // Custom rule: never show an update for "dev builds" (versions ending in 0)
        if second % 10 == 0 {
            return -1 // treat as: no update available
        }

        // Standard: compare second vs first
        if second > first { return 1 }
        if second < first { return -1 }
        return 0
    }
}


@MainActor
final class CustomVersionLogicViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var lastMessage: String?
    @Published var showAlert = false
    @Published var alertMessage = ""

    private var task: Task<Void, Never>?
    private lazy var pov: any PrinceOfVersionsBase = {
        let provider = HardcodedVersionProvider()
        let comparator = DeveloperBuildVersionComparator()
        return IosDefaultVersionComparatorKt.princeOfVersionsWithCustomVersionLogic(
            provider: provider,
            comparator: comparator
        )
    }()


    func check(isSlow: Bool) {
        cancel()
        isLoading = true
        lastMessage = nil

        task = Task { [weak self] in
            guard let self else { return }
            do {
                if isSlow { try await Task.sleep(for: .seconds(5)) }

                let update = try await IosPrinceOfVersionsKt.checkForUpdatesFromUrl(
                    pov,
                    url: Constants.updateUrl,
                    username: nil,
                    password: nil,
                    networkTimeout: Int64(Constants.networkTimeout)
                )

                let status = String(describing: update.status)
                let version = update.version ?? "(nil)"
                self.show("✅ status: \(status), version: \(version)\nmeta: \(update.metadata)")
            } catch is CancellationError {
                self.show("🔁 Cancelled")
            } catch _ as RequirementsNotSatisfiedException {
                self.show("❌ Requirements not satisfied")
            } catch _ as ConfigurationException {
                self.show("❌ Bad configuration")
            } catch _ as IoException {
                self.show("🌐 IO error")
            } catch {
                self.show("❌ Error: \(error.localizedDescription)")
            }
            self.isLoading = false
        }
    }

    func cancel() { task?.cancel(); task = nil; isLoading = false }

    private func show(_ text: String) {
        lastMessage = text; alertMessage = text; showAlert = true
    }
}
