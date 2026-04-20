//
//  CustomVersionLogicObjcTests.m
//  iosAppTests
//
//  Created by Filip Stojanovski on 10.11.25.
//

#import <XCTest/XCTest.h>
@import PrinceOfVersions;
#import "POVTestStringLoader.h"
#import "POVTestVersionProvider.h"

@interface CustomVersionLogicObjcTests : XCTestCase
@end

@implementation CustomVersionLogicObjcTests

/**
 Creates a PrinceOfVersions instance with custom version provider and comparator.
 @param currentVersion The version string to use as the current app version
 @return Configured PrinceOfVersions instance
 */
- (id<POVPrinceOfVersionsBase>)makePOVWithCurrent:(NSString *)currentVersion {
    POVTestVersionProvider *provider = [[POVTestVersionProvider alloc] initWithVersion:currentVersion];

    id<POVBaseVersionComparator> comparator =
        [POVIosDefaultVersionComparatorKt makeDefaultVersionComparator];

    id<POVPrinceOfVersionsBase> pov =
        [POVIosDefaultVersionComparatorKt makePrinceOfVersionsVersionProvider:provider
                                                                   comparator:comparator];
    return pov;
}

- (NSString *)expectedNotifiedVersionForRequired:(NSString *)req optional:(NSString *)opt {
    id<POVBaseVersionComparator> cmp = [POVIosDefaultVersionComparatorKt makeDefaultVersionComparator];
    if (opt == nil) return req;
    return ([cmp compareFirstVersion:req secondVersion:opt] < 0) ? opt : req;
}

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
    [pov checkForUpdatesSource:loader completionHandler:^(POVBaseUpdateResult<id> * _Nullable result,
                                                               NSError * _Nullable error) {
        XCTAssertNil(error);
        XCTAssertNotNil(result);
        XCTAssertEqual(result.status.ordinal, [POVUpdateStatus mandatory].ordinal);

        NSString *expected = [self expectedNotifiedVersionForRequired:@"1.2.3" optional:@"1.2.5"];
        XCTAssertEqualObjects(result.version, expected);

        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

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
    [pov checkForUpdatesSource:loader completionHandler:^(POVBaseUpdateResult<id> * _Nullable result,
                                                               NSError * _Nullable error) {
        XCTAssertNil(error);
        XCTAssertNotNil(result);
        XCTAssertEqual(result.status.ordinal, [POVUpdateStatus optional].ordinal);
        XCTAssertEqualObjects(result.version, @"1.4.0");
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

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
    [pov checkForUpdatesSource:loader completionHandler:^(POVBaseUpdateResult<id> * _Nullable result,
                                                               NSError * _Nullable error) {
        XCTAssertNil(error);
        XCTAssertNotNil(result);
        XCTAssertEqual(result.status.ordinal, [POVUpdateStatus noUpdate].ordinal);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

- (void)test_checkForUpdates_shouldReturnError_whenLoaderFails {
    id<POVPrinceOfVersionsBase> pov = [self makePOVWithCurrent:@"1.0.0"];

    NSError *testError = [NSError errorWithDomain:@"TestDomain"
                                             code:123
                                         userInfo:@{NSLocalizedDescriptionKey: @"Test error"}];
    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithError:testError];

    XCTestExpectation *exp = [self expectationWithDescription:@"error"];
    [pov checkForUpdatesSource:loader completionHandler:^(POVBaseUpdateResult<id> * _Nullable result,
                                                               NSError * _Nullable error) {
        XCTAssertNotNil(error);
        XCTAssertNil(result);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

@end
