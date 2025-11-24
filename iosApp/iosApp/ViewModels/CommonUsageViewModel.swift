//
//  CommonUsageViewModel.swift
//  iosApp
//
//  Created by Filip Stojanovski on 3.11.25.
//

import Foundation
import SwiftUI
import PrinceOfVersions

private let delayInMilliseconds: UInt64 = 5_000

@MainActor
final class CommonUsageViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var showAlert = false
    @Published var alertMessage = ""
    @Published var lastMessage: String?

    private var task: Task<Void, Never>?
    private lazy var pov: any PrinceOfVersionsBase = IosPrinceOfVersionsKt.PrinceOfVersions()

    func check(url: String, slow: Bool) {
        cancel()
        isLoading = true
        lastMessage = nil

        task = Task { [weak self] in
            guard let self else { return }
            do {
                if slow {
                    try await Task.sleep(for: .seconds(5))
                }

                let result = try await IosPrinceOfVersionsKt.checkForUpdatesFromUrl(
                    pov,
                    url: url,
                    username: nil,
                    password: nil,
                    networkTimeout: Int64(delayInMilliseconds)
                )
                self.show(message: self.format(result: result))
            } catch is CancellationError {
                self.show(message: "Update check cancelled")
            } catch {
                self.show(message: "Error: \(error.localizedDescription)")
            }
            self.isLoading = false
        }
    }

    func cancel() {
        task?.cancel()
        task = nil
        isLoading = false
    }

    private func show(message: String) {
        lastMessage = message
        alertMessage = message
        showAlert = true
    }

    private func format(result: BaseUpdateResult<NSString>) -> String {
        switch result.status {
        case .mandatory: return "Update available (mandatory): \(result.version ?? "-")"
        case .optional: return "Update available (optional): \(result.version ?? "-")"
        default:return "No update available"
        }
    }
}
