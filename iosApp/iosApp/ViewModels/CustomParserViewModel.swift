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
    @Published var inputJSON = Constants.JSON.demoIos2JSON
    @Published var resultText = ""
    @Published var isLoading = false
    @Published var showAlert = false
    @Published var alertMessage = ""

    private let checker = SystemVersionRequirementCheckerKt.makeSystemVersionRequirementChecker()
    private lazy var pov: any PrinceOfVersionsBase = {
        return IosPrinceOfVersionsKt.princeOfVersionsWithCustomChecker(
            key: Constants.checkerKey,
            checker: checker,
            keepDefaultCheckers: true
        )
    }()

    func parseLocally() {
        do {
            let cfg = try IosConfigurationParserKt.parseWithIosParserForSample(json: inputJSON)
            self.resultText = describe(config: cfg)
        } catch _ as RequirementsNotSatisfiedException {
            self.resultText = "❌ Requirements not satisfied"
        } catch _ as ConfigurationException {
            self.resultText = "❌ Bad configuration"
        } catch {
            self.resultText = "❌ Error: \(error.localizedDescription)"
        }
    }

    func checkFromURL() {
        isLoading = true
        resultText = ""
        Task {
            do {
                let update = try await IosPrinceOfVersionsKt.checkForUpdatesFromUrl(
                    pov,
                    url: Constants.customParserUrl,
                    username: nil,
                    password: nil,
                    networkTimeout: Int64(Constants.networkTimeout)
                )
                let status = String(describing: update.status)
                let version = update.version ?? "(nil)"
                self.show("✅ From URL → status: \(status), version: \(version)\nmeta: \(update.metadata)")
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

    private func show(_ message: String) {
        self.resultText = message
        self.alertMessage = message
        self.showAlert = true
    }
}
