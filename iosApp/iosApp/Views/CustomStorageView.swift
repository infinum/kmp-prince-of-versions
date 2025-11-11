//
//  CustomStorageView.swift
//  iosApp
//
//  Created by Filip Stojanovski on 6.11.25.
//

import SwiftUI
import PrinceOfVersions

private let demoURL = "https://pastebin.com/raw/KgAZQUb5"
private let delayMs: UInt64 = 5_000

@MainActor
final class CustomStorageViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var lastMessage: String?
    @Published var showAlert = false
    @Published var alertMessage = ""

    private var task: Task<Void, Never>?
    private let pov = IosStorageKt.princeOfVersionsWithCustomStorage(storage: UserDefaultsStorageSwift())

    func check(isSlow: Bool) {
        cancel()
        isLoading = true
        lastMessage = nil

        task = Task { [weak self] in
            guard let self else { return }
            do {
                if isSlow { try await Task.sleep(nanoseconds: delayMs * 1_000_000) }

                let result = try await IosPrinceOfVersionsKt.checkForUpdatesFromUrl(
                    pov,
                    url: demoURL,
                    username: nil,
                    password: nil,
                    networkTimeout: Int64(5_000)
                )

                let status = String(describing: result.status)
                let version = result.version ?? "(nil)"
                self.show("✅ status: \(status), version: \(version)\nmeta: \(result.metadata)")
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

    func cancel() {
        task?.cancel()
        task = nil
        isLoading = false
    }

    private func show(_ text: String) {
        lastMessage = text
        alertMessage = text
        showAlert = true
    }
}

struct CustomStorageView: View {
    @StateObject private var vm = CustomStorageViewModel()

    var body: some View {
        VStack(spacing: 12) {
            Text("Custom Storage").font(.headline)

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
        .navigationTitle("Custom Storage")
        .alert(isPresented: $vm.showAlert) {
            Alert(title: Text("Update check"),
                  message: Text(vm.alertMessage),
                  dismissButton: .default(Text("OK")))
        }
    }
}

final class UserDefaultsStorageSwift: NSObject, BaseStorage {
    private let key = "demo.last_notified_version"

    func getLastSavedVersion(completionHandler: @escaping (Any?, Error?) -> Void) {
        let value = UserDefaults.standard.string(forKey: key) as NSString?
        completionHandler(value, nil)
    }

    func saveVersion(version: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let v = version as? NSString {
            UserDefaults.standard.set(v as String, forKey: key)
        } else {
            UserDefaults.standard.removeObject(forKey: key)
        }
        completionHandler(nil)
    }
}
