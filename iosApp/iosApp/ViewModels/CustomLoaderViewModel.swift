//
//  CustomLoaderViewModel.swift
//  iosApp
//

import Foundation
import SwiftUI
import PrinceOfVersions

@MainActor
final class CustomLoaderViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var showAlert = false
    @Published var alertMessage = ""
    @Published var lastMessage: String?
    @Published var apiKey: String = "demo-api-key-12345"

    private var task: Task<Void, Never>?
    private lazy var pov: any PrinceOfVersionsBase = IosPrinceOfVersionsKt.createPrinceOfVersions()

    func check() {
        cancel()
        isLoading = true
        lastMessage = nil

        task = Task { [weak self] in
            guard let self else { return }
            do {
                // Create a custom loader with API key header
                let loader = APIKeyLoader(
                    url: URL(string: Constants.commonUsageUrl)!,
                    headers: [
                        "x-api-key": self.apiKey,
                        "X-Custom-Header": "custom-value",
                    ]
                )

                let result = try await pov.checkForUpdates(source: loader)
                let status = String(describing: result.status)
                let version = String(describing: result.version)
                self.show(message: "Status: \(status), version: \(version)")
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
}
