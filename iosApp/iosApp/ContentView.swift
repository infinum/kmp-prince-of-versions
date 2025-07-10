import SwiftUI
import PrinceOfVersions

struct ContentView: View {
    @State private var showContent = false
    var body: some View {
        VStack {
            Button("Click me!") {
                withAnimation {
                    showContent = !showContent
                }
            }

            if showContent {
                VStack(spacing: 16) {
                    Image(systemName: "swift")
                        .font(.system(size: 200))
                        .foregroundColor(.accentColor)
                    Text("SwiftUI: \(Greeting().greet())")
                }
                .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
        .onAppear {
            PrinceOfVersions.MyApi().fetchSomething { result, error in
                if let result = result {
                    print("Fetch using completion handler: \(result)")
                } else if let error = error {
                    print("Error: \(error.localizedDescription)")
                }
            }

            Task {
                do {
                    let value = try await PrinceOfVersions.MyApi().fetchSomething()
                    print("Fetch using async/await: \(value)")
                } catch {
                    print("Error: \(error)")
                }
            }

            Task {
                do {
                    let value = try await fetchSomethingAsync()
                    print("Fetch using withCheckedThrowingContinuation: \(value)")
                } catch {
                    print("Error: \(error)")
                }
            }
        }
    }
}

private extension ContentView {
    func fetchSomethingAsync() async throws -> String {
        try await withCheckedThrowingContinuation { continuation in
            PrinceOfVersions.MyApi().fetchSomething { result, error in
                if let result = result {
                    continuation.resume(returning: result)
                } else if let error = error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(throwing: NSError(domain: "Unknown", code: -1))
                }
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
