//
//  CustomStorageView.swift
//  iosApp
//
//  Created by Filip Stojanovski on 6.11.25.
//

import SwiftUI

struct CustomStorageView: View {
    var body: some View {
        VStack(spacing: 12) {
            Text("Custom Storage")
                .font(.headline)
            Text("TODO: Show using a custom storage implementation.")
                .foregroundStyle(.secondary)
            Spacer()
        }
        .padding(16)
        .navigationTitle("Custom Storage")
    }
}
