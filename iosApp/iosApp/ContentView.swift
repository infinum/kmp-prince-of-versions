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
            loadData()
        }
    }
}

private extension ContentView {
    func loadData() {
        Task {
            do {
                let result = try await callKotlinSuspendFunction()
                print("✅ Result:", result)
            } catch {
                switch KotlinBridgeError.from(error) {
                case .api(let backendError, let original):
                    print("🚨 Backend Error Code:", backendError)
                    print("📋 Message:", backendError.message)
                    print("🗒️ Original:", original)
                    // 👇 Each backendError case can be easily handled here;
                    // you can switch through them and add custom handling if needed.
                    switch backendError {
                    case .unauthorized:
                        print("🔑 Unauthorized error")
                    case .forbidden:
                        print("⛔ Forbidden error")
                    case .notFound:
                        print("❓ Not Found error")
                    case .internalServerError:
                        print("💥 Internal Server Error")
                    case .serviceUnavailable:
                        print("🛠 Service Unavailable")
                    case .unknown:
                        print("🤷 Unknown error")
                    }
                case .network(let message):
                    print("🌐 Network issue:", message)
                case .validation(let message):
                    print("⚠️ Validation error:", message)
                case .unknown(let message):
                    print("❓ Unknown error:", message)
                default:
                    print("Error:", error)
                }
            }
        }
    }

    func callKotlinSuspendFunction() async throws -> String {
        return try await withCheckedThrowingContinuation { continuation in
            PrinceOfVersions.MyApi().loadData() { result, error in
                if let value = result {
                    continuation.resume(returning: value)
                } else if let err = error {
                    continuation.resume(throwing: err)
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

enum KotlinBridgeError: Error {
    case illegalArgument(String)
    case network(String)
    case validation(String)
    case api(BackendError, originalMessage: String)
    case unknown(String)

    static func from(_ error: Error) -> KotlinBridgeError {
        guard let kotlinException = (error as NSError).userInfo["KotlinException"] else {
            return .unknown(error.localizedDescription)
        }

        let description = String(describing: kotlinException)

        if description.contains("IOException") ||
           description.contains("SocketTimeoutException") {
            return .network(description)
        } else if description.contains("IllegalArgumentException") {
            return .validation(description)
        } else if description.contains("ApiException") {
            let backendError = BackendError(from: description)
            return .api(backendError, originalMessage: description)
        }
        return .unknown(description)
    }
}


enum BackendError: String {
    case unauthorized = "401"
    case forbidden = "403"
    case notFound = "404"
    case internalServerError = "500"
    case serviceUnavailable = "503"
    case unknown

    init(from description: String) {
        // Extract 3-digit status code from the string
        let pattern = "\\b(\\d{3})\\b"
        let regex = try? NSRegularExpression(pattern: pattern)
        let range = NSRange(description.startIndex..., in: description)

        if let match = regex?.firstMatch(in: description, range: range),
           let swiftRange = Range(match.range(at: 1), in: description) {
            self = BackendError(rawValue: String(description[swiftRange])) ?? .unknown
        } else {
            self = .unknown
        }
    }

    var message: String {
        switch self {
        case .unauthorized:
            return "Unauthorized"
        case .forbidden:
            return "Forbidden"
        case .notFound:
            return "Not Found"
        case .internalServerError:
            return "Server Error"
        case .serviceUnavailable:
            return "Service Unavailable"
        case .unknown:
            return "Unknown Error"
        }
    }
}
