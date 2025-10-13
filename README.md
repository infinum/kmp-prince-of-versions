<div align="center">
    <div style="display: flex; align-items: center; justify-content: center; gap: 20px;">
        <img src="logo.svg" width="72">
        <h1>Prince Of Versions<br>KMP</h1>
    </div>
</div>


<!--
    This is the logo/image area for the project.
    Add a project logo or image (if applicable) to this part of the file.

    Try to make it visually appealing and relevant to the project.
    Check the *Credits* section as an example for a centered image.
-->

<!--
    This is the status area for the project.
    Add project badges (if needed) to this part of the file.
-->

[![Quality checks](https://github.com/infinum/kmp-prince-of-versions/actions/workflows/quality_checks.yml/badge.svg)](https://github.com/infinum/kmp-prince-of-versions/actions/workflows/quality_checks.yml)

## Description

Prince of Versions KMP is a Kotlin Multiplatform library that handles app update checks by fetching remote configurations. It returns update results with version information, status, and metadata to help you implement update flows across Android, JVM and iOS platforms. The library offers extensive customization options for update info fetching to fit your specific needs.

<!--
    Provide a detailed explanation of the project, its purpose, and its goals.
    Include any relevant background information, such as the problem the project solves,
    the target audience, and how the project differs from other similar projects.
-->

## Table of contents

- [Description](#description)
- [Table of contents](#table-of-contents)
- [Getting started](#getting-started)
  - [Installation](#installation)
    - [Kotlin Multiplatform](#kotlin-multiplatform)
    - [Android Only](#android-only)
    - [JVM/Desktop](#jvmdesktop)
- [Usage](#usage)
  - [Basic Usage](#basic-usage)
    - [Android](#android)
    - [iOS](#ios)
    - [JVM/Desktop](#jvmdesktop-1)
  - [Advanced Usage with Custom Components](#advanced-usage-with-custom-components)
    - [Android with Custom Configuration](#android-with-custom-configuration)
    - [Using Custom Loader](#using-custom-loader)
  - [Kotlin Multiplatform Projects](#kotlin-multiplatform-projects)
    - [Shared Code (commonMain)](#shared-code-commonmain)
    - [Android Implementation (androidMain)](#android-implementation-androidmain)
    - [iOS Implementation (iosMain)](#ios-implementation-iosmain)
    - [JVM Implementation (jvmMain)](#jvm-implementation-jvmmain)
  - [Configuration Format](#configuration-format)
    - [Basic Structure](#basic-structure)
    - [Platform Keys](#platform-keys)
    - [Configuration Properties](#configuration-properties)
    - [Update Behavior](#update-behavior)
    - [Requirements System](#requirements-system)
    - [Multiple Configurations (Array Format)](#multiple-configurations-array-format)
    - [Metadata Handling](#metadata-handling)
    - [Complete Examples](#complete-examples)
    - [Backward Compatibility](#backward-compatibility)
    - [Error Handling](#error-handling)
- [Contributing](#contributing)
- [License](#license)
- [Credits](#credits)

## Getting started

### Installation

Add the dependency to your project from Maven Central:

#### Kotlin Multiplatform

```kotlin
// In your shared module's build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.infinum.princeofversions:princeofversions:1.0.0")
        }
    }
}
```

#### Android Only

```kotlin
// In your app's build.gradle.kts
dependencies {
    implementation("com.infinum.princeofversions:princeofversions:1.0.0")
}
```

#### JVM/Desktop

```kotlin
// In your build.gradle.kts
dependencies {
    implementation("com.infinum.princeofversions:princeofversions:1.0.0")
}
```

// TODO: iOS

## Usage

### Basic Usage

The library provides platform-specific factory methods to create `PrinceOfVersions` instances. Each platform has its own requirements and initialization patterns.

**Note**: You'll need to create an API endpoint on your server where the remote update configuration JSON will be hosted and made available for the library to fetch. If you need a more specific setup consult [Advanced Usage with Custom Components](#advanced-usage-with-custom-components)

#### Android

```kotlin
import com.infinum.princeofversions.PrinceOfVersions
import com.infinum.princeofversions.UpdateStatus

// Simple initialization with default components
val princeOfVersions = PrinceOfVersions(context)

// Check for updates from a URL
val result = princeOfVersions.checkForUpdatesFromUrl("https://your-server.com/update-config.json")

when (result.status) {
    UpdateStatus.MANDATORY -> {
        // Handle required update
        showUpdateDialog(result.version, result.metadata)
    }
    UpdateStatus.OPTIONAL -> {
        // Handle optional update
        showOptionalUpdateDialog(result.version, result.metadata)
    }
    UpdateStatus.NO_UPDATE -> {
        // App is up to date
    }
}
```

#### iOS

In iOS, you'll need to create the instance in your iOS-specific code:

```swift
// In your iOS module
import PrinceOfVersions

let princeOfVersions = PrinceOfVersionsCompanion().invoke()

// Use with async/await
Task {
    do {
        let result = try await princeOfVersions.checkForUpdatesFromUrl(
            url: "https://your-server.com/update-config.json"
        )

        switch result.status {
        case .mandatory:
            // Handle required update
        case .optional:
            // Handle optional update
        case .noUpdate:
            // App is up to date
        }
    } catch {
        // Handle error
    }
}
```

#### JVM/Desktop

```kotlin
import com.infinum.princeofversions.PrinceOfVersions
import com.infinum.princeofversions.UpdateStatus

// Initialize with your main application class
val princeOfVersions = PrinceOfVersions(YourMainClass::class.java)

// Check for updates
val result = princeOfVersions.checkForUpdatesFromUrl("https://your-server.com/update-config.json")

when (result.status) {
    UpdateStatus.MANDATORY -> {
        // Handle required update
        println("Required update to version ${result.version}")
    }
    UpdateStatus.OPTIONAL -> {
        // Handle optional update
        println("Optional update to version ${result.version} available")
    }
    UpdateStatus.NO_UPDATE -> {
        println("App is up to date")
    }
}
```

### Advanced Usage with Custom Components

You can customize the library behavior by providing your own components:

#### Android with Custom Configuration

```kotlin
val customPrinceOfVersions = PrinceOfVersions(context) {
    // Custom configuration parser
    configurationParser = MyCustomConfigurationParser()
    
    // Custom requirement checker
    withRequirementCheckers(true) // Keep default requirement checking
    
    // Custom storage implementation
    storage = MyCustomStorage()
    
    // Custom version provider
    versionProvider = MyCustomVersionProvider()
}
```

#### Using Custom Loader

Instead of `checkForUpdatesFromUrl`, you can provide your own `Loader` implementation:

```kotlin
class MyCustomLoader : Loader {
    override suspend fun load(): String {
        // Your custom loading logic
        return fetchConfigurationFromCustomSource()
    }
}

val result = princeOfVersions.checkForUpdates(MyCustomLoader())
```

### Kotlin Multiplatform Projects

In KMP projects, the recommended approach is to create platform-specific instances in platform modules,
then define your business logic in commonMain that works with the base interface directly. 
This way you avoid duplicating business logic across platforms. Since the platforms
use varying types for their versions, generics have to be used.

#### Shared Code (commonMain)

```kotlin
// Business logic use case in commonMain using the base interface directly
class YourUseCase<T>(
    private val princeOfVersions: PrinceOfVersionsBase<T>
) {
    ...

    suspend fun checkForUpdates(source: Loader): BaseUpdateResult<T> {
        return princeOfVersions.checkForUpdates(source)
    }
    
    suspend fun checkForUpdatesFromUrl(
        ...
    ): BaseUpdateResult<T> {
        return princeOfVersions.checkForUpdatesFromUrl(...)
    }
    
    suspend fun handleUpdateResult(...) {
        val result = checkForUpdates(source)
        when (result.status) {
            UpdateStatus.MANDATORY -> ...
            UpdateStatus.OPTIONAL -> ...
            UpdateStatus.NO_UPDATE -> ...
        }
    }

    ...
}
```

#### Android Implementation (androidMain)

```kotlin
// Use case creation function
fun createAndroidUseCase(context: Context): YourUseCase<Long> {
    val princeOfVersions = PrinceOfVersions(context)
    return CheckForUpdatesUseCase(princeOfVersions)
}
```

#### iOS Implementation (iosMain)

```kotlin
// Use case creation function
fun createIosUseCase(): YourUseCase<String> {
    val princeOfVersions = PrinceOfVersions()
    return CheckForUpdatesUseCase(princeOfVersions)
}
```

#### JVM Implementation (jvmMain)

```kotlin
// Use case creation function
fun createJvmUseCase(mainClass: Class<*>): YourUseCase<String> {
    val princeOfVersions = PrinceOfVersions(mainClass)
    return YourUseCase(princeOfVersions)
}
```

### Configuration Format

The library expects a JSON configuration that contains platform-specific update information. The structure varies slightly between platforms but follows similar patterns.

#### Basic Structure

```json
{
    "android": { /* Android configuration */ },
    "android2": { /* Alternative Android configuration */ },
    "jvm": { /* JVM/Desktop configuration */ },
    // TODO: iOS
    "meta": {
        "title": "New Update Available",
        "description": "This update includes bug fixes and performance improvements."
    }
}
```

#### Platform Keys

- **`android2`** - Primary Android configuration key (recommended)
- **`android`** - Fallback Android configuration key (for backward compatibility)
- **`jvm`** - JVM/Desktop configuration key 
- // TODO: iOS


#### Configuration Properties

Each platform configuration can contain the following properties (all are optional, but at least one version property should be provided):

- **`required_version`** - The minimum version required for the app to function
  - Android: Long value (version code)
  - JVM: String value (semantic version like "1.2.0")
- **`last_version_available`** - The latest available version for optional updates
  - Android: Long value (version code)  
  - JVM: String value (semantic version like "1.2.0")
- **`notify_last_version_frequency`** - How often to notify about optional updates
  - Values: `"ONCE"` (default) or `"ALWAYS"`
- **`requirements`** - Object containing update requirements that must be satisfied
- **`meta`** - Object containing metadata specific to this update configuration

#### Update Behavior

- **Required Update**: If the current version is below `required_version`, a mandatory update is triggered. If the current version is below `required_version`, and the `last_version_available` value is also provided, a mandatory update is triggered, with the version value being set to whichever one is greater.
- **Optional Update**: If the current version is below `last_version_available` but meets `required_version`, an optional update is available
- **No Update**: If the current version meets both requirements, no update is needed

#### Requirements System

The `requirements` object can contain various conditions that must be met for the update to be applicable:

```json
{
    "requirements": {
        "required_os_version": "21",
        "required_jvm_version": "11",
        "custom_requirement": "some_value"
    }
}
```

**Default Requirements:**
- **`required_os_version`** - Minimum OS version required (Android only) // TODO: iOS
    - Android: API level (e.g., "21" for Android 5.0)
- **`required_jvm_version`** - Minimum JVM version required (JVM only)
  - JVM: JVM version number (e.g., "8", "11", "17")

**Custom Requirements:** You can add custom requirements and handle them with a custom `RequirementChecker` implementation.

#### Multiple Configurations (Array Format)

Platforms can use arrays to provide multiple configuration options. The library processes them in order and selects the first one whose requirements are satisfied:

```json
{
    "android2": [
        {
            "required_version": 10,
            "last_version_available": 12,
            "requirements": {
                "required_os_version": "21"
            },
            "meta": {
                "update_type": "major"
            }
        },
        {
            "required_version": 8,
            "last_version_available": 10,
            "requirements": {
                "required_os_version": "19"
            },
            "meta": {
                "update_type": "minor"
            }
        }
    ]
}
```

#### Metadata Handling

Metadata is merged with the following priority (highest to lowest):
1. **Selected configuration metadata** - `meta` from the chosen update configuration
2. **Root metadata** - `meta` from the root level of the JSON

If there are conflicting keys, the selected configuration metadata takes precedence.

#### Complete Examples

**Android Configuration:**
```json
{
    "android2": {
        "required_version": 15,
        "last_version_available": 18,
        "notify_last_version_frequency": "ONCE",
        "requirements": {
            "required_os_version": "21"
        },
        "meta": {
            "changelog_url": "https://example.com/changelog",
            "download_url": "https://play.google.com/store/apps/details?id=com.example.app"
        }
    },
    "meta": {
        "title": "Update Available",
        "description": "Please update to the latest version"
    }
}
```

**JVM Configuration:**
```json
{
    "jvm": {
        "required_version": "1.2.0",
        "last_version_available": "1.5.0",
        "notify_last_version_frequency": "ALWAYS",
        "requirements": {
            "required_jvm_version": "11"
        },
        "meta": {
            "download_url": "https://example.com/download/app-1.5.0.jar"
        }
    },
    "meta": {
        "title": "New Version Available",
        "description": "Enhanced performance and new features"
    }
}
```

// TODO: iOS


**Multi-Platform Configuration:**
```json
{
    "android2": {
        "required_version": 15,
        "last_version_available": 18,
        "notify_last_version_frequency": "ONCE"
    },
    "jvm": {
        "required_version": "1.2.0", 
        "last_version_available": "1.5.0",
        "notify_last_version_frequency": "ALWAYS"
    },
  // TODO: iOS
  "meta": {
        "title": "Update Available",
        "description": "Bug fixes and improvements",
        "release_notes": "https://example.com/release-notes"
    }
}
```

#### Backward Compatibility

To support both legacy and current versions of Prince of Versions:
- Use `android2` for new implementations
- Keep `android` as a fallback for older versions
- The library will prefer `android2` over `android` if both are present

#### Error Handling

- If no platform key is found, the parser will throw an error
- If requirements are not satisfied for any configuration in an array, a `RequirementsNotSatisfiedException` is thrown
- Invalid version formats or missing required fields will result in parsing exceptions
- // TODO - Update once iOS implementation is completed.


## Contributing

We believe that the community can help us improve and build better a product.
Please refer to our [contributing guide](CONTRIBUTING.md) to learn about the types of contributions we accept and the process for submitting them.

To ensure that our community remains respectful and professional, we defined a [code of conduct](CODE_OF_CONDUCT.md) <!-- and [coding standards](<link>) --> that we expect all contributors to follow.

We appreciate your interest and look forward to your contributions.

## License

```text
Copyright 2025 Infinum

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Credits

Maintained and sponsored by [Infinum](https://infinum.com).

<div align="center">
    <a href='https://infinum.com'>
    <picture>
        <source srcset="https://assets.infinum.com/brand/logo/static/white.svg" media="(prefers-color-scheme: dark)">
        <img src="https://assets.infinum.com/brand/logo/static/default.svg">
    </picture>
    </a>
</div>
