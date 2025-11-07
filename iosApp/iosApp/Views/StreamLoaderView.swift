//
//  StreamLoaderView.swift
//  iosApp
//
//  Created by Filip Stojanovski on 6.11.25.
//

import SwiftUI

struct StreamLoaderView: View {
    var body: some View {
        VStack(spacing: 12) {
            Text("Stream Loader")
                .font(.headline)
            Text("TODO: Show streaming/sequence of updates, cancellation, progress.")
                .foregroundStyle(.secondary)
            Spacer()
        }
        .padding(16)
        .navigationTitle("Stream Loader")
    }
}
