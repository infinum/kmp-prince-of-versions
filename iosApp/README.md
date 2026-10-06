# iOS Sample App

This SwiftUI sample app demonstrates how to integrate and use the Prince of Versions KMP library in a native iOS application.

## Overview

The app showcases various usage patterns:
- **Common Usage**: Basic update checking
- **Custom Checker**: Custom requirement validation
- **Custom Version Logic**: Custom version providers and comparators
- **Custom Parser**: Custom configuration parsing
- **Custom Storage**: Custom storage implementation

## Project Structure

```
iosApp/
├── iosApp/
│   ├── Views/               # SwiftUI views for each example
│   ├── ViewModels/          # View models with Prince of Versions integration
│   ├── iOSApp.swift         # App entry point
│   └── Assets.xcassets/     # Assets
├── iosAppTests/             # XCTest integration tests
│   ├── iosAppTests.swift    # Swift tests
│   ├── VersionProviderObjcTests.m    # Objective-C tests
│   └── CustomVersionLogicObjcTests.m # ObjC integration tests
└── Configuration/
    └── Config.xcconfig      # Build configuration
```

## Building and Running

### Prerequisites

- Xcode 15.0+
- macOS 14.0+
- The Prince of Versions framework (built from `princeofversions` module)

### Build the Framework

First, build the Kotlin framework:

```bash
# From project root
./gradlew :princeofversions:compileKotlinIosSimulatorArm64  # For Apple Silicon
# or
./gradlew :princeofversions:compileKotlinIosX64             # For Intel Mac
```

### Open in Xcode

```bash
open iosApp/iosApp.xcodeproj
```

### Run the App

1. Select a simulator or device
2. Press Cmd+R or click Run
3. Navigate through different examples in the app

### Run Tests

```bash
# From command line
xcodebuild test \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 16'

# Or in Xcode
Cmd+U
```

## Usage Examples

### 1. Common Usage

**File**: `Views/CommonUsageView.swift`

Basic update checking with async/await:

```swift
import PrinceOfVersions

let pov = IosPrinceOfVersionsKt.makePrinceOfVersions()

let result = try await IosPrinceOfVersionsKt.checkForUpdates(
    pov,
    from: "https://example.com/version.json",
    username: nil,
    password: nil,
    timeout: 60_000
)

switch String(describing: result.status) {
case "MANDATORY":
    print("Mandatory update: \(result.version ?? "")")
case "OPTIONAL":
    print("Optional update: \(result.version ?? "")")
default:
    print("No update")
}
```

**Key Points:**
- Uses default configuration
- async/await pattern
- Timeout in milliseconds (60,000 = 60 seconds)
- Status enum comparison via string

### 2. Custom Requirement Checker

**File**: `Views/CustomCheckerView.swift`

Add custom validation logic:

```swift
// Create custom checker (can be implemented in Swift or use Kotlin helper)
let checker = SystemVersionRequirementCheckerKt.makeSystemVersionRequirementChecker()

// Create PrinceOfVersions with custom checker
let pov = IosPrinceOfVersionsKt.makePrinceOfVersions(
    checkerKey: "required_os_version",
    checker: checker,
    keepDefaultCheckers: true
)

// Use it
do {
    let result = try await IosPrinceOfVersionsKt.checkForUpdates(pov, from: url, ...)
} catch let error as RequirementsNotSatisfiedException {
    print("Requirements not met: \(error)")
}
```

**Key Points:**
- Custom requirement validation
- Can implement `RequirementChecker` protocol in Swift
- Throws `RequirementsNotSatisfiedException` when requirements fail
- Can combine with default checkers

### 3. Custom Version Logic

**File**: `Views/CustomVersionLogicView.swift`

Custom version provider and comparator:

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

// Get default comparator
let baseComparator = IosDefaultVersionComparatorKt.makeDefaultVersionComparator()

// Wrap with custom logic (e.g., treat "-0" builds as dev builds)
let customComparator = DevBuildVersionComparator(delegate: baseComparator)

// Create PrinceOfVersions with custom logic
let pov = IosDefaultVersionComparatorKt.makePrinceOfVersions(
    versionProvider: provider,
    comparator: customComparator
)

let result = try await IosPrinceOfVersionsKt.checkForUpdates(pov, from: url, ...)
```

**Key Points:**
- Create mock version providers in Swift for testing
- `DevBuildVersionComparator` - Example custom comparator
- Compose version logic with delegation
- Provider reads app version (can override for testing)

### 4. Custom Parser

**File**: `Views/CustomParserView.swift`

Parse custom JSON formats:

```swift
// Implement POVBaseConfigurationParser protocol
class MyCustomParser: POVBaseConfigurationParser {
    func parse(source: String) throws -> POVBasePrinceOfVersionsConfig {
        // Parse your custom JSON format
        // Return config object
    }
}

let parser = MyCustomParser()
let pov = IosDefaultVersionComparatorKt.makePrinceOfVersions(parser: parser)
```

**Key Points:**
- Parse custom configuration formats
- Implement `POVBaseConfigurationParser` protocol
- Full control over JSON structure

### 5. Custom Storage

**File**: `Views/CustomStorageView.swift`

Implement custom storage for version check results:

```swift
// Implement POVStorage protocol
class MyCustomStorage: POVStorage {
    func lastNotifiedVersion(updateInfo: POVUpdateInfo) -> String? {
        // Return last notified version from custom storage
    }

    func setLastNotifiedVersion(updateInfo: POVUpdateInfo, version: String) {
        // Save to custom storage
    }
}

let storage = MyCustomStorage()
let pov = IosStorageKt.makePrinceOfVersions(storage: storage)
```

**Key Points:**
- Control how "notify once" works
- Can use UserDefaults, Keychain, CoreData, etc.
- Implement `POVStorage` protocol

## Error Handling

The library throws specific Kotlin exceptions that can be caught in Swift:

```swift
do {
    let result = try await IosPrinceOfVersionsKt.checkForUpdates(pov, from: url, ...)
} catch let error as RequirementsNotSatisfiedException {
    // Device doesn't meet requirements (OS version, etc.)
    print("Requirements not satisfied: \(error.metadata)")
} catch let error as ConfigurationException {
    // JSON parsing error or invalid configuration
    print("Bad configuration: \(error)")
} catch let error as IoException {
    // Network error, invalid URL, timeout
    print("Network error: \(error)")
} catch {
    // Other errors
    print("Unexpected error: \(error.localizedDescription)")
}
```

## SwiftUI Patterns

### ViewModel Pattern

```swift
@MainActor
final class UpdateViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var message: String?

    private let pov = IosPrinceOfVersionsKt.makePrinceOfVersions()
    private var task: Task<Void, Never>?

    func checkForUpdates(url: String) {
        task?.cancel()
        isLoading = true

        task = Task { [weak self] in
            defer { self?.isLoading = false }

            do {
                let result = try await IosPrinceOfVersionsKt.checkForUpdates(
                    self!.pov,
                    from: url,
                    username: nil,
                    password: nil,
                    timeout: 60_000
                )
                self?.message = self?.format(result: result)
            } catch {
                self?.message = "Error: \(error.localizedDescription)"
            }
        }
    }

    func cancel() {
        task?.cancel()
    }

    private func format(result: BaseUpdateResult<NSString>) -> String {
        switch String(describing: result.status) {
        case "MANDATORY": return "Mandatory update: \(result.version ?? "")"
        case "OPTIONAL": return "Optional update: \(result.version ?? "")"
        default: return "Up to date!"
        }
    }
}
```

### Task Cancellation

All examples support cancellation:

```swift
private var task: Task<Void, Never>?

func check() {
    task = Task {
        // ... async work
    }
}

func cancel() {
    task?.cancel()  // Cancels network request
}
```

## Testing

### Swift Tests

**File**: `iosAppTests/iosAppTests.swift`

```swift
import Testing
import XCTest
import PrinceOfVersions

final class VersionProviderHostTests: XCTestCase {
    func testReadsVersionFromHostInfoPlist() {
        let version = TestHooksKt.povExposeappversionforunittests()
        XCTAssertFalse(version.isEmpty)
        XCTAssertTrue(version.contains("-"))
    }
}
```

### Objective-C Tests

**File**: `iosAppTests/VersionProviderObjcTests.m`

```objc
@import PrinceOfVersions;
#import <XCTest/XCTest.h>

@implementation VersionProviderObjcTests

- (void)testInvalidURL_YieldsIoError {
    id<POVPrinceOfVersionsBase> pov = [POVIosPrinceOfVersionsKt makePrinceOfVersions];

    XCTestExpectation *exp = [self expectationWithDescription:@"io-error"];
    [POVIosPrinceOfVersionsKt checkForUpdates:pov
                                         from:@"not a url"
                                     username:nil
                                     password:nil
                                      timeout:3000
                            completionHandler:^(POVBaseUpdateResult<NSString *> *result,
                                                NSError *error) {
        XCTAssertNil(result);
        XCTAssertNotNil(error);
        [exp fulfill];
    }];

    [self waitForExpectations:@[exp] timeout:5.0];
}

@end
```

### Test Helpers

The framework provides test utilities:

- **TestHooks**: Expose internal functionality for testing
- **POVTestStringLoader**: Mock loader with hardcoded JSON
- **DevBuildVersionComparator**: Example custom comparator
- Create mock version providers in Swift for testing

## JSON Configuration Format

The library expects JSON in this format:

### Modern Format (ios2)

```json
{
  "ios2": {
    "required_version": "1.2.3",
    "last_version_available": "1.2.5",
    "notify_last_version_frequency": "ALWAYS",
    "requirements": {
      "required_os_version": "14.0"
    },
    "meta": {
      "release_notes_url": "https://example.com/notes",
      "custom_field": "value"
    }
  }
}
```

### Legacy Format (ios)

```json
{
  "ios": {
    "minimum_version": "1.2.3",
    "latest_version": {
      "version": "1.2.5",
      "notification_type": "ONCE",
      "min_sdk": "14.0"
    }
  }
}
```

**Note**: `ios2` format is preferred and takes precedence if both are present.

## Common Patterns

### 1. Dependency Injection

```swift
protocol UpdateCheckService {
    func checkForUpdates(from url: String) async throws -> UpdateResult
}

class PrinceOfVersionsService: UpdateCheckService {
    private let pov: any PrinceOfVersionsBase

    init(pov: any PrinceOfVersionsBase = IosPrinceOfVersionsKt.makePrinceOfVersions()) {
        self.pov = pov
    }

    func checkForUpdates(from url: String) async throws -> UpdateResult {
        // Implementation
    }
}
```

### 2. Configuration from Enum

```swift
enum UpdateCheckConfiguration {
    case production
    case staging
    case custom(url: String)

    var url: String {
        switch self {
        case .production: return "https://api.prod.com/version.json"
        case .staging: return "https://api.staging.com/version.json"
        case .custom(let url): return url
        }
    }
}

let result = try await checkForUpdates(config: .production)
```

### 3. Retry Logic

```swift
func checkWithRetry(maxAttempts: Int = 3) async throws -> BaseUpdateResult<NSString> {
    var lastError: Error?

    for attempt in 1...maxAttempts {
        do {
            return try await IosPrinceOfVersionsKt.checkForUpdates(pov, from: url, ...)
        } catch let error as IoException {
            lastError = error
            if attempt < maxAttempts {
                try await Task.sleep(nanoseconds: UInt64(attempt) * 1_000_000_000)
                continue
            }
        } catch {
            throw error  // Non-network errors - don't retry
        }
    }

    throw lastError ?? UpdateError.unknown
}
```

## Tips and Best Practices

1. **Reuse PrinceOfVersions instance** - Create once, use throughout the app
2. **Use async/await** - Don't mix with completion handlers
3. **Handle specific exceptions** - Catch `RequirementsNotSatisfiedException`, `IoException`, etc.
4. **Timeout in milliseconds** - Remember: 60_000 = 60 seconds
5. **Test with mock providers** - Create simple Swift mock classes for testing
6. **Cancel tasks** - Always cancel ongoing tasks when views disappear
7. **Wrap in service layer** - Hide Kotlin interop from your app code

## Troubleshooting

### Framework Not Found

**Error**: `No such module 'PrinceOfVersions'`

**Solution**: Build the Kotlin framework first:
```bash
./gradlew :princeofversions:compileKotlinIosSimulatorArm64
```

### Type Mismatch

**Error**: `Cannot convert 'String' to 'NSString'`

**Solution**: Let Swift bridge automatically or cast explicitly:
```swift
let version: String? = result.version as String?
```

### Enum Comparison

**Error**: Enum comparison doesn't work

**Solution**: Use string comparison or ordinal:
```swift
String(describing: result.status) == "MANDATORY"
// or
result.status.ordinal == UpdateStatus.mandatory.ordinal
```

## See Also

- [iOS Migration Guide](../docs/IOS_MIGRATION_GUIDE.md) - Migrate from native library
- [Main README](../README.md) - Project overview
- [Kotlin Source](../princeofversions/src/iosMain/) - iOS-specific Kotlin code
