//
//  CustomParserView.swift
//  iosApp
//
//  Created by Filip Stojanovski on 6.11.25.
//

import SwiftUI
import PrinceOfVersions

@MainActor
final class CustomParserViewModel: ObservableObject {
    @Published var inputJSON = Constants.JSON.demoIos2JSON
    @Published var resultText = ""
    @Published var isLoading = false
    @Published var showAlert = false
    @Published var alertMessage = ""

    private let checker = SystemVersionRequirementCheckerKt.makeSystemVersionRequirementChecker()

        // Build PoV with the checker
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
                    networkTimeout: Int64(5_000)
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

struct CustomParserView: View {
    @StateObject private var vm = CustomParserViewModel()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("Custom Configuration Parser (iOS)")
                    .font(.headline)

                Group {
                    Text("Input JSON")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                    TextEditor(text: $vm.inputJSON)
                        .frame(minHeight: 180)
                        .font(.system(.body, design: .monospaced))
                        .overlay(RoundedRectangle(cornerRadius: 8).stroke(.secondary.opacity(0.3)))
                }

                HStack {
                    Button("Parse JSON with iOS parser") { vm.parseLocally() }
                        .buttonStyle(.borderedProminent)

                    Button("Load from URL with iOS parser") { vm.checkFromURL() }
                        .buttonStyle(.bordered)
                }

                if vm.isLoading { ProgressView().padding(.top, 6) }

                Group {
                    Text("Result")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                    Text(vm.resultText.isEmpty ? "—" : vm.resultText)
                        .font(.system(.footnote, design: .monospaced))
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(8)
                        .background(Color.secondary.opacity(0.07))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }

                Spacer(minLength: 12)
            }
            .padding(16)
        }
        .navigationTitle("Custom Parser")
        .alert(isPresented: $vm.showAlert) {
            Alert(title: Text("Parser / URL result"), message: Text(vm.alertMessage), dismissButton: .default(Text("OK")))
        }
    }
}

