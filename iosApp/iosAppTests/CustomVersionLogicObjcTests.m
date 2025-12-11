//
//  CustomVersionLogicObjcTests.m
//  iosAppTests
//
//  Created by Filip Stojanovski on 10.11.25.
//

#import <XCTest/XCTest.h>
@import PrinceOfVersions;
#import "POVTestStringLoader.h"

@interface CustomVersionLogicObjcTests : XCTestCase
@end

@implementation CustomVersionLogicObjcTests

// Convenience to build the POV with custom provider+comparator for all tests
- (id<POVPrinceOfVersionsBase>)makePOVWithCurrent:(NSString *)currentVersion {
    POVHardcodedVersionProviderIos *provider =
        [[POVHardcodedVersionProviderIos alloc] initWithCurrent:currentVersion];

    id<POVBaseVersionComparator> defaultCmp =
        [POVIosDefaultVersionComparatorKt defaultIosVersionComparator];

    // Your special comparator: suppress updates when remote ends with "-0"
    POVDevBuildVersionComparator *cmp =
        [[POVDevBuildVersionComparator alloc] initWithDelegate:defaultCmp];

    id<POVPrinceOfVersionsBase> pov =
        [POVIosDefaultVersionComparatorKt princeOfVersionsWithCustomVersionLogicProvider:provider
                                                                             comparator:cmp];
    return pov;
}

// Returns the version the interactor will notify for a MANDATORY case:
// max(required, optional) using the default iOS comparator.
- (NSString *)expectedNotifiedVersionForRequired:(NSString *)req optional:(NSString *)opt {
    id<POVBaseVersionComparator> cmp = [POVIosDefaultVersionComparatorKt defaultIosVersionComparator];
    if (opt == nil) return req;
    // compare(first, second) < 0  ⇢  first < second
    return ([cmp compareFirstVersion:req secondVersion:opt] < 0) ? opt : req;
}

// A) Mandatory update: current < required_version
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
        XCTAssertNil(error);
        XCTAssertNotNil(result);

        // Safer enum assertion (ordinal instead of pointer identity)
        XCTAssertEqual(result.status.ordinal, [POVUpdateStatus mandatory].ordinal);

        // Expect max(required, optional) by comparator
        NSString *expected = [self expectedNotifiedVersionForRequired:@"1.2.3"
                                                             optional:@"1.2.5"];
        XCTAssertEqualObjects(result.version, expected);

        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}


// C) Optional update: current == required, optional is higher ⇒ OPTIONAL ("1.3.0")
- (void)test_checkForUpdates_shouldReturnOptionalUpdate_whenBelowOptionalVersionOnly {
    id<POVPrinceOfVersionsBase> pov = [self makePOVWithCurrent:@"1.2.3"];

    NSString *json =
    @"{"
      "\"ios2\": [{"
        "\"required_version\": \"1.2.3\","
        "\"last_version_available\": \"1.3.0\","
        "\"notify_last_version_frequency\": \"ALWAYS\""
      "}]"
    "}";

    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithPayload:json];

    XCTestExpectation *exp = [self expectationWithDescription:@"optional-update"];
    [POVIosPrinceOfVersionsKt checkForUpdatesBridged:pov source:loader completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                          NSError * _Nullable error) {
        XCTAssertNil(error);
        XCTAssertNotNil(result);
        XCTAssertEqualObjects(result.status.name, [POVUpdateStatus optional].name);
        XCTAssertEqualObjects(result.version, @"1.3.0");
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}


// D) No update: current == required == optional ⇒ NO_UPDATE, version echoes current
- (void)test_checkForUpdates_shouldReturnNoUpdate_whenUpToDate {
    id<POVPrinceOfVersionsBase> pov = [self makePOVWithCurrent:@"1.2.3"];

    NSString *json =
    @"{"
      "\"ios2\": [{"
        "\"required_version\": \"1.2.3\","
        "\"last_version_available\": \"1.2.3\","
        "\"notify_last_version_frequency\": \"ONCE\""
      "}]"
    "}";

    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithPayload:json];

    XCTestExpectation *exp = [self expectationWithDescription:@"no-update"];
    [POVIosPrinceOfVersionsKt checkForUpdatesBridged:pov source:loader completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                          NSError * _Nullable error) {
        XCTAssertNil(error);
        XCTAssertNotNil(result);
        XCTAssertEqualObjects(result.status.name, [POVUpdateStatus noUpdate].name);
        XCTAssertEqualObjects(result.version, @"1.2.3");
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}


// E) Bad configuration: missing ios/ios2 ⇒ NSError (ConfigurationException bridged)
- (void)test_checkForUpdates_shouldThrowConfigurationException_whenConfigurationIsMissing {
    id<POVPrinceOfVersionsBase> pov = [self makePOVWithCurrent:@"1.2.3"];

    NSString *badJson = @"{\"meta\":{\"only\":\"meta\"}}"; // no ios / ios2 sections
    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithPayload:badJson];

    XCTestExpectation *exp = [self expectationWithDescription:@"config-error"];
    [POVIosPrinceOfVersionsKt checkForUpdatesBridged:pov source:loader completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                          NSError * _Nullable error) {
        XCTAssertNil(result);
        XCTAssertNotNil(error);
        // (Optional) Inspect the bridged KMP exception:
        // id kex = error.userInfo[@"KotlinException"]; NSLog(@"%@", kex);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}


@end
