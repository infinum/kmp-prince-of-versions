//
//  CustomVersionLogicView.swift
//  iosApp
//
//  Created by Filip Stojanovski on 6.11.25.
//

import SwiftUI
import PrinceOfVersions

@MainActor
final class CustomVersionLogicViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var lastMessage: String?
    @Published var showAlert = false
    @Published var alertMessage = ""

    private var task: Task<Void, Never>?
    private var pov: any PrinceOfVersionsBase

    init() {
        let provider = HardcodedVersionProviderIos(current: "1.2.3")
        let base = IosDefaultVersionComparatorKt.defaultIosVersionComparator()
        let comparator = DevBuildVersionComparator(delegate: base)

        pov = IosDefaultVersionComparatorKt.princeOfVersionsWithCustomVersionLogic(provider: provider, comparator: comparator)
    }

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

struct CustomVersionLogicView: View {
    @StateObject private var vm = CustomVersionLogicViewModel()
    var body: some View {
        VStack(spacing: 12) {
            Text("Custom Version Logic").font(.headline)
            HStack {
                Button("Check") { vm.check(isSlow: false) }.buttonStyle(.borderedProminent)
                Button("Check (slow)") { vm.check(isSlow: true) }.buttonStyle(.bordered)
            }
            Button("Cancel") { vm.cancel() }.buttonStyle(.bordered).tint(.red)
            if vm.isLoading { ProgressView().padding(.top, 8) }
            if let last = vm.lastMessage {
                Text(last).font(.footnote).foregroundStyle(.secondary).multilineTextAlignment(.center).padding(.top, 8)
            }
            Spacer()
        }
        .padding(16)
        .navigationTitle("Custom Version Logic")
        .alert(isPresented: $vm.showAlert) {
            Alert(title: Text("Update check"), message: Text(vm.alertMessage), dismissButton: .default(Text("OK")))
        }
    }
}
