//
//  CustomCheckerViewModel.swift
//  iosApp
//
//  Created by Filip Stojanovski on 13.11.25.
//

import Foundation
import PrinceOfVersions

@MainActor
final class CustomCheckerViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var showAlert = false
    @Published var alertMessage = ""
    @Published var lastMessage: String?

    private var task: Task<Void, Never>?
    private var pov = IosPrinceOfVersionsKt.PrinceOfVersions()
    private let checker = SystemVersionRequirementCheckerKt.makeSystemVersionRequirementChecker()

    init() {
        pov = IosPrinceOfVersionsKt.princeOfVersionsWithCustomChecker(
            key: Constants.checkerKey,
            checker: checker,
            keepDefaultCheckers: true
        )
    }

    func check(isSlow: Bool) {
        cancel()
        isLoading = true
        lastMessage = nil

        task = Task { [weak self] in
            guard let self else { return }
            do {
                if isSlow { try await Task.sleep(for: .seconds(5)) }

                let result = try await IosPrinceOfVersionsKt.checkForUpdatesFromUrl(
                    pov,
                    url: Constants.updateUrl,
                    username: nil,
                    password: nil,
                    networkTimeout: Int64(Constants.networkTimeout)
                )
                self.show(message: self.format(result: result))
            } catch is CancellationError {
                self.show(message: "Update check cancelled")
            } catch _ as RequirementsNotSatisfiedException {
                self.show(message: "Requirements not met. (Custom checker failed)")
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
        switch String(describing: result.status) {
        case "MANDATORY": return "Update available (mandatory): \(result.version ?? "-")"
        case "OPTIONAL":  return "Update available (optional): \(result.version ?? "-")"
        default:          return "No update available"
        }
    }
}
