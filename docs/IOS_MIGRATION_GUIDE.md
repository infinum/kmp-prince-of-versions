# iOS Migration Guide: Native to KMP

This guide helps you migrate from the native [iOS Prince of Versions](https://github.com/infinum/ios-prince-of-versions) library to the new Kotlin Multiplatform version.

## Why Migrate?

- **Cross-platform**: Same logic across iOS, Android, and Desktop
- **Modern async/await**: Native Swift concurrency support
- **Better maintained**: Single codebase for all platforms
- **Type-safe**: Strong typing from Kotlin

## Overview

| Aspect | Native (Swift/ObjC) | KMP (Kotlin → Swift) |
|--------|-------------------|---------------------|
| Language | Swift/Objective-C | Kotlin (exposed to Swift) |
| Concurrency | Completion handlers | async/await |
| API Style | Static methods | Instance-based |
| Customization | Swift protocols | Kotlin protocols (exposed to Swift) |

## Quick Migration Example

### Before (Native Library)

```swift
import PrinceOfVersions

let url = URL(string: "https://example.com/versions.json")!

PrinceOfVersions.checkForUpdates(
    from: url,
    completion: { response in
        switch response.result {
        case .success(let updateData):
            print("Update version: \(updateData.updateVersion)")
            print("Status: \(updateData.updateStatus)")
        case .failure(let error):
            print("Error: \(error)")
        }
    }
)
```

### After (KMP Library)

```swift
import PrinceOfVersions

let url = "https://example.com/versions.json"
let pov = IosPrinceOfVersionsKt.makePrinceOfVersions()

Task {
    do {
        let result = try await IosPrinceOfVersionsKt.checkForUpdates(
            pov,
            from: url,
            username: nil,
            password: nil,
            timeout: 60_000
        )

        switch String(describing: result.status) {
        case "MANDATORY":
            print("Update available (mandatory): \(result.version ?? "")")
        case "OPTIONAL":
            print("Update available (optional): \(result.version ?? "")")
        default:
            print("No update available")
        }
    } catch let error as RequirementsNotSatisfiedException {
        print("Requirements not met")
    } catch let error as ConfigurationException {
        print("Configuration error")
    } catch let error as IoException {
        print("Network error")
    } catch {
        print("Error: \(error.localizedDescription)")
    }
}
```

## Migration Steps

### 1. Update Dependencies

**Remove old dependency:**
```ruby
# Podfile or SPM - remove old library
pod 'PrinceOfVersions'  # ← Remove this
```

**Add KMP framework via Swift Package Manager:**

In Xcode:
1. File → Add Package Dependencies
2. Enter repository URL: `https://github.com/infinum/kmp-prince-of-versions.git`
3. Under "Dependency Rule", select **Branch** and enter `main`

Or in your `Package.swift`:
```swift
dependencies: [
    .package(url: "https://github.com/infinum/kmp-prince-of-versions.git", branch: "main")
]
```

> **Note**: Version-based dependency rules are not yet available. Use the `main` branch until a new tagged release with SPM support is published.

Alternatively, download the prebuilt XCFramework from [Releases](https://github.com/infinum/kmp-prince-of-versions/releases) and add it manually to your project.

### 2. Update Imports

```swift
// No change needed - both use:
import PrinceOfVersions
```

### 3. Update API Calls

#### Basic Check for Updates

**Before:**
```swift
PrinceOfVersions.checkForUpdates(from: url) { response in
    // Handle response
}
```

**After:**
```swift
let pov = IosPrinceOfVersionsKt.makePrinceOfVersions()
let result = try await IosPrinceOfVersionsKt.checkForUpdates(
    pov,
    from: urlString,
    username: nil,
    password: nil,
    timeout: 60_000  // milliseconds
)
```

#### With Authentication

**Before:**
```swift
PrinceOfVersions.checkForUpdates(
    from: url,
    httpHeaders: ["Authorization": "Bearer token"]
) { response in ... }
```

**After:**
```swift
let result = try await IosPrinceOfVersionsKt.checkForUpdates(
    pov,
    from: urlString,
    username: "user",       // Basic auth
    password: "password",
    timeout: 60_000
)
```

### 4. Update Response Handling

**Before:**
```swift
switch response.result {
case .success(let updateData):
    let version = updateData.updateVersion
    let status = updateData.updateStatus  // enum
case .failure(let error):
    // handle error
}
```

**After:**
```swift
// Success case - no wrapping Result type
let version = result.version
let status = String(describing: result.status)  // Convert enum to string

// Errors are caught with try/catch
catch let error as RequirementsNotSatisfiedException {
    // handle requirements error
}
```

### 5. Custom Requirements Checker

**Before:**
```swift
let options = PoVRequestOptions()
options.addRequirement(key: "region", ofType: String.self) {
    $0.starts(with: "hr")
}

PrinceOfVersions.checkForUpdates(from: url, options: options) { ... }
```

**After:**
```swift
// Implement in Swift if needed
class RegionChecker: RequirementChecker {
    func checkRequirements(value: String?) -> Bool {
        return value?.starts(with: "hr") ?? false
    }
}

let checker = RegionChecker()
let pov = IosPrinceOfVersionsKt.makePrinceOfVersions(
    checkerKey: "region",
    checker: checker,
    keepDefaultCheckers: true
)

let result = try await IosPrinceOfVersionsKt.checkForUpdates(pov, from: url, ...)
```

Or use Kotlin helper:
```swift
let checker = SystemVersionRequirementCheckerKt.makeSystemVersionRequirementChecker()
let pov = IosPrinceOfVersionsKt.makePrinceOfVersions(
    checkerKey: "required_os_version",
    checker: checker,
    keepDefaultCheckers: true
)
```

### 6. Custom Version Comparison

**After (creating custom Swift version provider):**
```swift
// Create a simple mock version provider in Swift
class MockVersionProvider: POVBaseApplicationVersionProvider {
    private let version: String

    init(version: String) {
        self.version = version
    }

    func getVersion() -> String {
        return version
    }
}

let provider = MockVersionProvider(version: "1.2.3")
let baseComparator = IosDefaultVersionComparatorKt.makeDefaultVersionComparator()
let customComparator = DevBuildVersionComparator(delegate: baseComparator)

let pov = IosDefaultVersionComparatorKt.makePrinceOfVersions(
    versionProvider: provider,
    comparator: customComparator
)

let result = try await IosPrinceOfVersionsKt.checkForUpdates(pov, from: url, ...)
```

## Key Differences to Remember

### 1. Naming Conventions

- Factory functions end with `Kt`: `IosPrinceOfVersionsKt`
- This is standard Kotlin/Native interop - unavoidable but consistent

### 2. Timeout Values

```swift
// Before: seconds (TimeInterval)
timeout: 30

// After: milliseconds (Int64)
networkTimeout: 30_000
```

### 3. Async Patterns

```swift
// Before: Completion handlers
PrinceOfVersions.checkForUpdates(...) { response in }

// After: async/await (better!)
let result = try await IosPrinceOfVersionsKt.checkForUpdates(pov, from: url, ...)
```

### 4. Error Handling

```swift
// Before: Result type
switch response.result {
case .success: ...
case .failure: ...
}

// After: try/catch
do {
    let result = try await ...
} catch {
    // Handle error
}
```

### 5. Status Enum

```swift
// Before: Direct enum comparison
if updateData.updateStatus == .required { }

// After: String comparison (or use ordinal)
if String(describing: result.status) == "MANDATORY" { }
// or
if result.status.ordinal == UpdateStatus.mandatory.ordinal { }
```

## SwiftUI Integration

### ViewModel Example

```swift
import SwiftUI
import PrinceOfVersions

@MainActor
final class UpdateCheckViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var updateMessage: String?

    private let pov = IosPrinceOfVersionsKt.makePrinceOfVersions()

    func checkForUpdates(url: String) async {
        isLoading = true
        defer { isLoading = false }

        do {
            let result = try await IosPrinceOfVersionsKt.checkForUpdates(
                pov,
                from: url,
                username: nil,
                password: nil,
                timeout: 60_000
            )

            switch String(describing: result.status) {
            case "MANDATORY":
                updateMessage = "Update required: \(result.version ?? "")"
            case "OPTIONAL":
                updateMessage = "Update available: \(result.version ?? "")"
            default:
                updateMessage = "You're up to date!"
            }
        } catch let error as RequirementsNotSatisfiedException {
            updateMessage = "Device doesn't meet requirements"
        } catch let error as IoException {
            updateMessage = "Network error: \(error.localizedDescription)"
        } catch {
            updateMessage = "Error: \(error.localizedDescription)"
        }
    }
}
```

### View Example

```swift
struct UpdateCheckView: View {
    @StateObject private var viewModel = UpdateCheckViewModel()

    var body: some View {
        VStack {
            Button("Check for Updates") {
                Task {
                    await viewModel.checkForUpdates(
                        url: "https://example.com/version.json"
                    )
                }
            }
            .disabled(viewModel.isLoading)

            if viewModel.isLoading {
                ProgressView()
            }

            if let message = viewModel.updateMessage {
                Text(message)
            }
        }
    }
}
```

## Testing

### Unit Tests

```swift
import XCTest
@testable import YourApp
import PrinceOfVersions

final class UpdateCheckTests: XCTestCase {

    func testMandatoryUpdate() async throws {
        // Create a simple mock version provider
        class MockVersionProvider: POVBaseApplicationVersionProvider {
            func getVersion() -> String { "1.0.0" }
        }

        let provider = MockVersionProvider()
        let comparator = IosDefaultVersionComparatorKt.makeDefaultVersionComparator()

        let pov = IosDefaultVersionComparatorKt.makePrinceOfVersions(
            versionProvider: provider,
            comparator: comparator
        )

        // Create test loader with JSON
        let json = """
        {
            "ios2": {
                "required_version": "2.0.0",
                "last_version_available": "2.0.0"
            }
        }
        """
        let loader = POVTestStringLoader(payload: json)

        let result = try await IosPrinceOfVersionsKt.checkForUpdatesBridged(
            pov,
            source: loader
        )

        XCTAssertEqual(result.status.ordinal, UpdateStatus.mandatory.ordinal)
        XCTAssertEqual(result.version, "2.0.0")
    }
}
```

## Common Issues

### Issue 1: Type Mismatches

**Problem:**
```swift
// Error: Cannot convert value of type 'String' to expected argument type 'NSString?'
```

**Solution:**
```swift
// The framework expects NSString for optional parameters
let version: String? = result.version as String?
```

### Issue 2: Enum Comparison

**Problem:**
```swift
if result.status == .MANDATORY { }  // Doesn't work
```

**Solution:**
```swift
// Use string comparison
if String(describing: result.status) == "MANDATORY" { }

// Or use ordinal comparison
if result.status.ordinal == UpdateStatus.mandatory.ordinal { }
```

### Issue 3: Timeout Confusion

**Problem:**
```swift
networkTimeout: 30  // Too short! This is 30 milliseconds
```

**Solution:**
```swift
networkTimeout: 30_000  // 30 seconds in milliseconds
```

## Best Practices

1. **Create PrinceOfVersions instance once** - Reuse it across your app
2. **Use dependency injection** - Inject the PrinceOfVersions instance
3. **Wrap in a service layer** - Hide Kotlin interop details from your app
4. **Handle all exception types** - Catch specific Kotlin exceptions
5. **Use async/await** - Don't mix with completion handlers
6. **Test with mock providers** - Create simple Swift mock classes for testing

## Example: Service Layer Pattern

Hide the Kotlin interop behind a clean Swift interface:

```swift
protocol UpdateCheckService {
    func checkForUpdates(from url: String) async throws -> UpdateResult
}

struct UpdateResult {
    let hasUpdate: Bool
    let isMandatory: Bool
    let version: String?
    let metadata: [String: Any]
}

final class PrinceOfVersionsUpdateService: UpdateCheckService {
    private let pov = IosPrinceOfVersionsKt.makePrinceOfVersions()
    private let timeout: Int64 = 60_000

    func checkForUpdates(from url: String) async throws -> UpdateResult {
        let result = try await IosPrinceOfVersionsKt.checkForUpdates(
            pov,
            from: url,
            username: nil,
            password: nil,
            timeout: timeout
        )

        let status = String(describing: result.status)
        return UpdateResult(
            hasUpdate: status != "NO_UPDATE",
            isMandatory: status == "MANDATORY",
            version: result.version as String?,
            metadata: result.metadata as? [String: Any] ?? [:]
        )
    }
}

// Usage in your app
let service: UpdateCheckService = PrinceOfVersionsUpdateService()
let result = try await service.checkForUpdates(from: url)

if result.isMandatory {
    // Show force update dialog
} else if result.hasUpdate {
    // Show optional update dialog
}
```

## See Also

- [iOS Sample App](../iosApp/) - Full working examples
- [Main README](../README.md) - Installation and setup
- [Sample App Tests](../iosApp/iosAppTests/) - XCTest integration examples
