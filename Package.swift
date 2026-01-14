// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "PrinceOfVersions",
    platforms: [
        .iOS(.v16),
        .macOS(.v14)
    ],
    products: [
        .library(
            name: "PrinceOfVersions",
            targets: ["PrinceOfVersions"]
        )
    ],
    targets: [
        .binaryTarget(
            name: "PrinceOfVersions",
            url: "https://github.com/infinum/kmp-prince-of-versions/raw/0.1.0/PrinceOfVersions.xcframework.zip",
            checksum: "2d7b588610c023eff8a3f55e9e991574979aedb8c528ee46f7e9af5764e96362"
        )
    ]
)
