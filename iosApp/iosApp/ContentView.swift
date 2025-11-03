import SwiftUI
import PrinceOfVersions

private let UPDATE_URL = "https://pastebin.com/raw/0MfYmWGu"

struct ContentView: View {
    @StateObject private var vm = CommonUsageViewModel()

    var body: some View {
        NavigationView {
            VStack(spacing: 12) {
                Button("Check for updates") {
                    vm.check(url: UPDATE_URL, slow: false)
                }
                .buttonStyle(.borderedProminent)

                Button("Check (simulate slow request)") {
                    vm.check(url: UPDATE_URL, slow: true)
                }
                .buttonStyle(.bordered)

                Button("Cancel") {
                    vm.cancel()
                }
                .buttonStyle(.bordered)
                .tint(.red)

                if vm.isLoading {
                    ProgressView().padding(.top, 8)
                }

                if let last = vm.lastMessage {
                    Text(last)
                        .font(.footnote)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.top, 8)
                }

                Spacer()
            }
            .padding(16)
            .navigationTitle("Common Usage")
            .alert(isPresented: $vm.showAlert) {
                Alert(title: Text("Update check"), message: Text(vm.alertMessage), dismissButton: .default(Text("OK")))
            }
        }
    }
}
