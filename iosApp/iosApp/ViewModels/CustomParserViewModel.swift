//
//  CustomParserViewModel.swift
//  iosApp
//
//  Created by Filip Stojanovski on 13.11.25.
//

import Foundation
import PrinceOfVersions

@MainActor
final class CustomParserViewModel: ObservableObject {
    @Published var inputJSON = Constants.JSON.minimumVersion
    @Published var resultText = ""
    @Published var isLoading = false
    @Published var showAlert = false
    @Published var alertMessage = ""

    private lazy var pov: any PrinceOfVersionsBase = IosDefaultVersionComparatorKt.princeOfVersionsWithCustomParser(parser: MinimalConfigParser())


    func parseLocally() {
        isLoading = true
        resultText = ""

        let loader = StringLoader(payload: inputJSON)

        pov.checkForUpdates(source: loader) { [weak self] result, error in
            Task { @MainActor [weak self] in
                guard let self else { return }
                self.isLoading = false
                
                if let error = error as NSError? {
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

                guard let result else {
                    self.show(message: "❌ Unknown error")
                    return
                }
                
                let status = String(describing: result.status)
                let version = String(describing: result.version)
                self.show(message: "✅ From JSON → status: \(status), version: \(version)\nmeta: \(result.metadata)")
            }
        }
    }

    func checkFromURL() {
        isLoading = true
        resultText = ""
        Task {
            do {
                let update = try await IosPrinceOfVersionsKt.checkForUpdatesFromUrl(
                    pov,
                    url: Constants.minimumUrl,
                    username: nil,
                    password: nil,
                    networkTimeout: Int64(Constants.networkTimeout)
                )
                let status = String(describing: update.status)
                let version = (update.version as String?) ?? "nil"
                self.show(message: "✅ From URL → status: \(status), version: \(version)\nmeta: \(update.metadata)")
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

    private func describe(config: BasePrinceOfVersionsConfig<NSString>) -> String {
        let mandatory = config.mandatoryVersion ?? "(nil)"
        let optional  = config.optionalVersion ?? "(nil)"
        let notify    = String(describing: config.optionalNotificationType)
        return """
        ✅ Parsed successfully:
        - mandatoryVersion: \(mandatory)
        - optionalVersion:  \(optional)
        - optionalNotificationType: \(notify)
        - requirements: \(config.requirements)
        - metadata: \(config.metadata)
        """
    }

    private func show(message: String) {
        resultText = message
        alertMessage = message
        showAlert = true
    }
}

