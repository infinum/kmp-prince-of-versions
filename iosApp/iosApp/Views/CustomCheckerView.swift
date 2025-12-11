//
//  CustomCheckerView.swift
//  iosApp
//
//  Created by Filip Stojanovski on 6.11.25.
//

import SwiftUI

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

