//
//  VersionProviderObjcTests.m
//  iosAppTests
//
//  Created by Filip Stojanovski on 6.11.25.
//
//  Demonstrates various PrinceOfVersions library features from Objective-C

#import <XCTest/XCTest.h>
@import PrinceOfVersions;
#import "ThresholdChecker.h"

@interface VersionProviderObjcTests : XCTestCase
@end

@implementation VersionProviderObjcTests

// Test: Reading app version from Info.plist using test hooks
- (void)test_versionProvider_shouldReadVersionFromHostInfoPlist {
    POVTestHooks *hooks = POVTestHooks.shared;
    NSString *version = [hooks exposeAppVersionForUnitTests];

    XCTAssertTrue(version.length > 0, @"Version should not be empty");
    XCTAssertTrue([version containsString:@"-"],
                  @"Expected short-build like 1.2.3-456; got %@", version);
}

// Helper to extract Kotlin exception message
- (NSString *)kotlinExceptionStringFrom:(NSError *)error {
    id ke = error.userInfo[@"KotlinException"];
    return ke ? [ke description] : error.localizedDescription;
}

// Test: IO error when using invalid URL
- (void)test_checkForUpdates_shouldYieldIoError_whenInvalidURL {
    id<POVPrinceOfVersionsBase> pov = [POVIosPrinceOfVersionsKt PrinceOfVersions];

    XCTestExpectation *exp = [self expectationWithDescription:@"io-error"];
    [POVIosPrinceOfVersionsKt checkForUpdatesFromUrl:pov
                                                       url:@"not a url"
                                                  username:nil
                                                  password:nil
                                       networkTimeout:3000
                                         completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                             NSError * _Nullable error)
    {
        XCTAssertNil(result, @"Result should be nil on error");
        XCTAssertNotNil(error, @"Should have error");
        XCTAssertTrue([self kotlinExceptionStringFrom:error].length > 0,
                     @"Error message should not be empty");
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:5.0];
}

// Test: Custom requirement checker implementation in Objective-C
- (void)test_checkForUpdates_shouldYieldError_whenRequirementsNotSatisfied {
    ThresholdChecker *checker = [ThresholdChecker new];

    id<POVPrinceOfVersionsBase> pov =
    [POVIosPrinceOfVersionsKt princeOfVersionsWithCustomCheckerKey:@"requiredNumberOfLetters"
                                                           checker:checker
                                               keepDefaultCheckers:YES];

    NSString *url = @"https://pastebin.com/raw/VMgd71VH";

    XCTestExpectation *exp = [self expectationWithDescription:@"requirements"];
    [POVIosPrinceOfVersionsKt checkForUpdatesFromUrl:pov
                                                       url:url
                                                  username:nil
                                                  password:nil
                                       networkTimeout:3000
                                         completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                             NSError * _Nullable error)
    {
        XCTAssertNil(result, @"Result should be nil when requirements fail");
        XCTAssertNotNil(error, @"Should have error when requirements not satisfied");
        XCTAssertTrue([self kotlinExceptionStringFrom:error].length > 0);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:5.0];
}

// Test: Configuration exception with bad JSON
- (void)test_checkForUpdates_shouldYieldConfigurationException_whenBadConfig {
    id<POVPrinceOfVersionsBase> pov = [POVIosPrinceOfVersionsKt PrinceOfVersions];

    // Point to a JSON that will cause configuration error
    NSString *url = @"https://example.com/pov_bad_config.json";

    XCTestExpectation *exp = [self expectationWithDescription:@"config-error"];
    [POVIosPrinceOfVersionsKt checkForUpdatesFromUrl:pov
                                                       url:url
                                                  username:nil
                                                  password:nil
                                       networkTimeout:3000
                                         completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                             NSError * _Nullable error)
    {
        XCTAssertNil(result);
        XCTAssertNotNil(error, @"Should have configuration error");
        XCTAssertTrue([self kotlinExceptionStringFrom:error].length > 0);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:5.0];
}

// Test: Using custom version provider with default comparator
- (void)test_checkForUpdates_shouldWorkWithCustomVersionProvider {
    // Create a hardcoded version provider
    POVHardcodedVersionProviderIos *provider =
        [[POVHardcodedVersionProviderIos alloc] initWithCurrent:@"1.2.3"];

    // Use default comparator
    id<POVBaseVersionComparator> comparator =
        [POVIosDefaultVersionComparatorKt defaultIosVersionComparator];

    // Create PrinceOfVersions instance with custom provider
    id<POVPrinceOfVersionsBase> pov =
        [POVIosDefaultVersionComparatorKt princeOfVersionsWithCustomVersionLogicProvider:provider
                                                                             comparator:comparator];

    XCTestExpectation *exp = [self expectationWithDescription:@"custom-provider"];
    [POVIosPrinceOfVersionsKt checkForUpdatesFromUrl:pov
                                                       url:@"https://pastebin.com/raw/KgAZQUb5"
                                                  username:nil
                                                  password:nil
                                       networkTimeout:3000
                                         completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                             NSError * _Nullable error) {
        if (error) {
            NSLog(@"Error: %@", error);
            XCTFail(@"Should not have error: %@", error);
        } else {
            NSLog(@"Status=%@ Version=%@", result.status, result.version);
            XCTAssertNotNil(result, @"Result should not be nil");
        }
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:5.0];
}

// Test: Default PrinceOfVersions instance works
- (void)test_checkForUpdates_shouldWorkWithDefaultInstance {
    id<POVPrinceOfVersionsBase> pov = [POVIosPrinceOfVersionsKt PrinceOfVersions];

    XCTestExpectation *exp = [self expectationWithDescription:@"default-instance"];
    [POVIosPrinceOfVersionsKt checkForUpdatesFromUrl:pov
                                                       url:@"https://pastebin.com/raw/rPZ4iRJB"
                                                  username:nil
                                                  password:nil
                                       networkTimeout:3000
                                         completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                             NSError * _Nullable error) {
        // Either result or error should be present
        BOOL hasResultOrError = (result != nil) || (error != nil);
        XCTAssertTrue(hasResultOrError, @"Should have either result or error");

        if (result) {
            NSLog(@"Update check completed: status=%@, version=%@",
                  result.status, result.version);
        } else if (error) {
            NSLog(@"Update check failed with error: %@", error);
        }
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:5.0];
}

@end
