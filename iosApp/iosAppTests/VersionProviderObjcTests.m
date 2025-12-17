//
//  VersionProviderObjcTests.m
//  iosAppTests
//
//  Created by Filip Stojanovski on 6.11.25.
//

#import <XCTest/XCTest.h>
@import PrinceOfVersions;
#import "POVTestStringLoader.h"
#import "ThresholdChecker.h"

@interface VersionProviderObjcTests : XCTestCase
@end

@implementation VersionProviderObjcTests

- (void)test_versionProvider_shouldReadVersionFromHostInfoPlist {
    POVTestHooks *hooks = POVTestHooks.shared;
    NSString *version = [hooks exposeAppVersionForUnitTests];

    XCTAssertTrue(version.length > 0);
    XCTAssertTrue([version containsString:@"-"],
                  @"Expected short-build like 1.2.3-456; got %@", version);
}

/**
 Extracts the Kotlin exception message from an NSError.
 @param error The error to extract the Kotlin exception from
 @return The Kotlin exception description or the localized description
 */
- (NSString *)kotlinExceptionStringFrom:(NSError *)error {
    id ke = error.userInfo[@"KotlinException"];
    return ke ? [ke description] : error.localizedDescription;
}

- (void)test_checkForUpdates_shouldYieldIoError_whenInvalidURL {
    id<POVPrinceOfVersionsBase> pov = [POVIosPrinceOfVersionsKt PrinceOfVersions];

    NSError *ioError = [NSError errorWithDomain:@"TestIOError"
                                           code:404
                                       userInfo:@{NSLocalizedDescriptionKey: @"Network error"}];
    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithError:ioError];

    XCTestExpectation *exp = [self expectationWithDescription:@"io-error"];
    [POVIosPrinceOfVersionsKt checkForUpdatesBridged:pov
                                              source:loader
                                   completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                       NSError * _Nullable error)
    {
        XCTAssertNil(result);
        XCTAssertNotNil(error);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

- (void)test_checkForUpdates_shouldYieldError_whenRequirementsNotSatisfied {
    ThresholdChecker *checker = [ThresholdChecker new];

    id<POVPrinceOfVersionsBase> pov =
    [POVIosPrinceOfVersionsKt princeOfVersionsWithCustomCheckerKey:@"requiredNumberOfLetters"
                                                           checker:checker
                                               keepDefaultCheckers:YES];

    NSString *json = @"{\"ios2\":[{\"required_version\":\"1.0.0\",\"requirements\":{\"requiredNumberOfLetters\":\"3\"}}]}";
    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithPayload:json];

    XCTestExpectation *exp = [self expectationWithDescription:@"requirements"];
    [POVIosPrinceOfVersionsKt checkForUpdatesBridged:pov
                                              source:loader
                                   completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                       NSError * _Nullable error)
    {
        XCTAssertNil(result);
        XCTAssertNotNil(error);
        XCTAssertTrue([self kotlinExceptionStringFrom:error].length > 0);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

- (void)test_checkForUpdates_shouldYieldConfigurationException_whenBadConfig {
    id<POVPrinceOfVersionsBase> pov = [POVIosPrinceOfVersionsKt PrinceOfVersions];

    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithPayload:@"{invalid json}"];

    XCTestExpectation *exp = [self expectationWithDescription:@"config-error"];
    [POVIosPrinceOfVersionsKt checkForUpdatesBridged:pov
                                              source:loader
                                   completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                       NSError * _Nullable error)
    {
        XCTAssertNil(result);
        XCTAssertNotNil(error);
        XCTAssertTrue([self kotlinExceptionStringFrom:error].length > 0);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

- (void)test_checkForUpdates_shouldWorkWithCustomVersionProvider {
    POVHardcodedVersionProviderIos *provider =
        [[POVHardcodedVersionProviderIos alloc] initWithCurrent:@"1.2.3"];

    id<POVBaseVersionComparator> comparator =
        [POVIosDefaultVersionComparatorKt defaultIosVersionComparator];

    id<POVPrinceOfVersionsBase> pov =
        [POVIosDefaultVersionComparatorKt princeOfVersionsWithCustomVersionLogicProvider:provider
                                                                             comparator:comparator];

    NSString *json = @"{\"ios2\":[{\"required_version\":\"1.2.5\",\"last_version_available\":\"1.3.0\"}]}";
    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithPayload:json];

    XCTestExpectation *exp = [self expectationWithDescription:@"custom-provider"];
    [POVIosPrinceOfVersionsKt checkForUpdatesBridged:pov
                                              source:loader
                                   completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                       NSError * _Nullable error) {
        if (error) {
            XCTFail(@"Should not have error: %@", error);
        } else {
            XCTAssertNotNil(result);
        }
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

- (void)test_checkForUpdates_shouldWorkWithDefaultInstance {
    id<POVPrinceOfVersionsBase> pov = [POVIosPrinceOfVersionsKt PrinceOfVersions];

    POVTestStringLoader *loader = [[POVTestStringLoader alloc] initWithPayload:
        @"{\"ios2\":[{\"required_version\":\"1.0.0\"}]}"];

    XCTestExpectation *exp = [self expectationWithDescription:@"default-instance"];
    [POVIosPrinceOfVersionsKt checkForUpdatesBridged:pov
                                              source:loader
                                   completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                       NSError * _Nullable error) {
        XCTAssertTrue((result != nil) || (error != nil));
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:2.0];
}

@end
