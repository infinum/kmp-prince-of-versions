//
//  CustomVersionLogicViewModel.swift
//  iosApp
//
//  Created by Filip Stojanovski on 13.11.25.
//

import Foundation
import PrinceOfVersions

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
                let version = (update.version as String?) ?? "nil"
                self.show(message: "✅ status: \(status), version: \(version)\nmeta: \(update.metadata)")
            } catch is CancellationError {
                self.show(message: "🔁 Cancelled")
            } catch {
                if isKotlin(error, RequirementsNotSatisfiedException.self) {
                    self.show(message: "Requirements not met. (Custom checker failed)")
                } else if isKotlin(error, ConfigurationException.self) {
                    self.show(message: "Bad configuration")
                } else if isKotlin(error, IoException.self) {
                    self.show(message: "Network / IO error")
                } else {
                    self.show(message: "Error: \((error as NSError).localizedDescription)")
                }
            }
            self.isLoading = false
        }
    }

    func cancel() { task?.cancel(); task = nil; isLoading = false }

    private func show(message: String) {
        lastMessage = message
        alertMessage = message
        showAlert = true
    }
}
