//
//  CustomCheckerViewModel.swift
//  iosApp
//
//  Created by Filip Stojanovski on 13.11.25.
//

import Foundation
import PrinceOfVersions

private let requirementKey = "requiredNumberOfLetters"
private let threshold = 5
private let updateUrl = "https://pastebin.com/raw/VMgd71VH"

@objcMembers
final class ExampleRequirementsChecker: NSObject, RequirementChecker {
    
    private let threshold = 5

    func checkRequirements(value: String?) -> Bool {
        let n = Int(value ?? "") ?? 0
        return n >= threshold
    }
}

@MainActor
final class CustomCheckerViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var showAlert = false
    @Published var alertMessage = ""
    @Published var lastMessage: String?

    private var task: Task<Void, Never>?
    private lazy var pov: any PrinceOfVersionsBase = {
        return IosPrinceOfVersionsKt.princeOfVersionsWithCustomChecker(
            key: Constants.checkerKey,
            checker: ExampleRequirementsChecker(),
            keepDefaultCheckers: true
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
        switch result.status {
        case .mandatory: return "Update available (mandatory): \(result.version ?? "-")"
        case .optional: return "Update available (optional): \(result.version ?? "-")"
        default:return "No update available"
        }
    }
}
