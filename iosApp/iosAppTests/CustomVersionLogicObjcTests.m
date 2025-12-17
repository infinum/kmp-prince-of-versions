//
//  CustomVersionLogicObjcTests.m
//  iosAppTests
//
//  Created by Filip Stojanovski on 10.11.25.
//
//  Demonstrates how to use PrinceOfVersions library from Objective-C code

#import <XCTest/XCTest.h>
@import PrinceOfVersions;
#import "POVTestStringLoader.h"

@interface CustomVersionLogicObjcTests : XCTestCase
@end

@implementation CustomVersionLogicObjcTests

// Demonstrates creating PrinceOfVersions with custom version provider and comparator in Objective-C
- (id<POVPrinceOfVersionsBase>)makePOVWithCurrent:(NSString *)currentVersion {
    // Use the hardcoded version provider from the framework
    POVHardcodedVersionProviderIos *provider =
        [[POVHardcodedVersionProviderIos alloc] initWithCurrent:currentVersion];

    // Use the default iOS version comparator
    id<POVBaseVersionComparator> comparator =
        [POVIosDefaultVersionComparatorKt defaultIosVersionComparator];

    // Create PrinceOfVersions instance with custom provider and comparator
    id<POVPrinceOfVersionsBase> pov =
        [POVIosDefaultVersionComparatorKt princeOfVersionsWithCustomVersionLogicProvider:provider
                                                                             comparator:comparator];
    return pov;
}

// Helper to determine expected notification version
- (NSString *)expectedNotifiedVersionForRequired:(NSString *)req optional:(NSString *)opt {
    id<POVBaseVersionComparator> cmp = [POVIosDefaultVersionComparatorKt defaultIosVersionComparator];
    if (opt == nil) return req;
    // compare(first, second) < 0  ⇢  first < second
    return ([cmp compareFirstVersion:req secondVersion:opt] < 0) ? opt : req;
}

// Test: Mandatory update when current version is below required version
- (void)test_checkForUpdates_shouldReturnMandatoryUpdate_whenBelowRequiredVersion {
    id<POVPrinceOfVersionsBase> pov = [self makePOVWithCurrent:@"1.2.2"];

    NSString *json =
    @"{"
      "\"ios2\": [{"
        "\"required_version\": \"1.2.3\","
        "\"last_version_available\": \"1.2.5\","
        "\"notify_last_version_frequency\": \"ALWAYS\""
      "}]"
    "}";

    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithPayload:json];

    XCTestExpectation *exp = [self expectationWithDescription:@"mandatory-update"];
    [POVIosPrinceOfVersionsKt checkForUpdatesBridged:pov source:loader completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                         NSError * _Nullable error) {
        XCTAssertNil(error, @"Should not have error");
        XCTAssertNotNil(result, @"Result should not be nil");

        // Verify it's a mandatory update
        XCTAssertEqual(result.status.ordinal, [POVUpdateStatus mandatory].ordinal,
                      @"Should be mandatory update");

        // Verify version is the higher of required/optional
        NSString *expected = [self expectedNotifiedVersionForRequired:@"1.2.3"
                                                             optional:@"1.2.5"];
        XCTAssertEqualObjects(result.version, expected,
                             @"Should return the higher version");

        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

// Test: Optional update when current version equals required but below optional
- (void)test_checkForUpdates_shouldReturnOptionalUpdate_whenBelowOptionalVersionOnly {
    id<POVPrinceOfVersionsBase> pov = [self makePOVWithCurrent:@"1.3.0"];

    NSString *json =
    @"{"
      "\"ios2\": [{"
        "\"required_version\": \"1.3.0\","
        "\"last_version_available\": \"1.4.0\","
        "\"notify_last_version_frequency\": \"ALWAYS\""
      "}]"
    "}";

    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithPayload:json];

    XCTestExpectation *exp = [self expectationWithDescription:@"optional-update"];
    [POVIosPrinceOfVersionsKt checkForUpdatesBridged:pov source:loader completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                         NSError * _Nullable error) {
        XCTAssertNil(error);
        XCTAssertNotNil(result);
        XCTAssertEqual(result.status.ordinal, [POVUpdateStatus optional].ordinal,
                      @"Should be optional update");
        XCTAssertEqualObjects(result.version, @"1.4.0");
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

// Test: No update when current version is up to date
- (void)test_checkForUpdates_shouldReturnNoUpdate_whenCurrentVersionIsLatest {
    id<POVPrinceOfVersionsBase> pov = [self makePOVWithCurrent:@"2.0.0"];

    NSString *json =
    @"{"
      "\"ios2\": [{"
        "\"required_version\": \"1.5.0\","
        "\"last_version_available\": \"1.9.0\","
        "\"notify_last_version_frequency\": \"ALWAYS\""
      "}]"
    "}";

    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithPayload:json];

    XCTestExpectation *exp = [self expectationWithDescription:@"no-update"];
    [POVIosPrinceOfVersionsKt checkForUpdatesBridged:pov source:loader completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                         NSError * _Nullable error) {
        XCTAssertNil(error);
        XCTAssertNotNil(result);

        // Check that status is "noUpdate" since current version is higher than remote
        XCTAssertEqual(result.status.ordinal, [POVUpdateStatus noUpdate].ordinal,
                      @"Should be noUpdate status");
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

// Test: Error handling when loader returns error
- (void)test_checkForUpdates_shouldReturnError_whenLoaderFails {
    id<POVPrinceOfVersionsBase> pov = [self makePOVWithCurrent:@"1.0.0"];

    NSError *testError = [NSError errorWithDomain:@"TestDomain"
                                             code:123
                                         userInfo:@{NSLocalizedDescriptionKey: @"Test error"}];
    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithError:testError];

    XCTestExpectation *exp = [self expectationWithDescription:@"error"];
    [POVIosPrinceOfVersionsKt checkForUpdatesBridged:pov source:loader completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                         NSError * _Nullable error) {
        XCTAssertNotNil(error, @"Should have error");
        XCTAssertNil(result, @"Result should be nil on error");
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

@end
