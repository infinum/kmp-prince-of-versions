//
//  CustomCheckerView.swift
//  iosApp
//
//  Created by Filip Stojanovski on 6.11.25.
//

import SwiftUI
import PrinceOfVersions

private let updateUrl = "https://pastebin.com/raw/0MfYmWGu"
private let delayMilliseconds: UInt64 = 5_000
private let checkerKey = "requiredNumberOfLetters"

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
            key: checkerKey,
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
                if isSlow { try await Task.sleep(nanoseconds: delayMilliseconds * 1_000_000) }

                let result = try await IosPrinceOfVersionsKt.checkForUpdatesFromUrlMillis(
                    pov,
                    url: updateUrl,
                    username: nil,
                    password: nil,
                    networkTimeoutMillis: Int64(delayMilliseconds)
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

struct CustomCheckerView: View {
    @StateObject private var vm = CustomCheckerViewModel()

    var body: some View {
        VStack(spacing: 12) {
            Text("Custom Requirement Checker").font(.headline)

            HStack {
                Button("Check") { vm.check(isSlow: false) }
                    .buttonStyle(.borderedProminent)

                Button("Check (slow)") { vm.check(isSlow: true) }
                    .buttonStyle(.bordered)
            }

            Button("Cancel") { vm.cancel() }
                .buttonStyle(.bordered)
                .tint(.red)

            if vm.isLoading { ProgressView().padding(.top, 8) }

            if let last = vm.lastMessage {
                Text(last)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 8)
            }

            Spacer()
        }
        .padding(16)
        .navigationTitle("Custom Checker")
        .alert(isPresented: $vm.showAlert) {
            Alert(title: Text("Update check"),
                  message: Text(vm.alertMessage),
                  dismissButton: .default(Text("OK")))
        }
    }
}

