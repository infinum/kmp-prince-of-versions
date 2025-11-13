//
//  CustomParserView.swift
//  iosApp
//
//  Created by Filip Stojanovski on 6.11.25.
//

import SwiftUI
import PrinceOfVersions

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

