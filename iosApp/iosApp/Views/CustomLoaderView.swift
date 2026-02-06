//
//  CustomLoaderView.swift
//  iosApp
//

import SwiftUI
import PrinceOfVersions

struct CustomLoaderView: View {
    @StateObject private var vm = CustomLoaderViewModel()

    var body: some View {
        VStack(spacing: 16) {
            Text("This example shows how to use a custom Loader with HTTP headers (e.g., API key authentication).")
                .font(.footnote)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)

            TextField("API Key", text: $vm.apiKey)
                .textFieldStyle(.roundedBorder)
                .autocapitalization(.none)
                .disableAutocorrection(true)

            Button("Check for updates") {
                vm.check()
            }
            .buttonStyle(.borderedProminent)

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
        .navigationTitle("Custom Loader")
        .alert(isPresented: $vm.showAlert) {
            Alert(
                title: Text("Update check"),
                message: Text(vm.alertMessage),
                dismissButton: .default(Text("OK"))
            )
        }
    }
}
