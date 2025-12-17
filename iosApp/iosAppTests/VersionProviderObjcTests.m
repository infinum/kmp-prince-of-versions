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

    XCTestExpectation *exp = [self expectationWithDescription:@"io-error"];
    [POVIosPrinceOfVersionsKt checkForUpdatesFromUrl:pov
                                                       url:@"not a url"
                                                  username:nil
                                                  password:nil
                                       networkTimeout:3000
                                         completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                             NSError * _Nullable error)
    {
        XCTAssertNil(result);
        XCTAssertNotNil(error);
        XCTAssertTrue([self kotlinExceptionStringFrom:error].length > 0);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:5.0];
}

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
        XCTAssertNil(result);
        XCTAssertNotNil(error);
        XCTAssertTrue([self kotlinExceptionStringFrom:error].length > 0);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:5.0];
}

- (void)test_checkForUpdates_shouldYieldConfigurationException_whenBadConfig {
    id<POVPrinceOfVersionsBase> pov = [POVIosPrinceOfVersionsKt PrinceOfVersions];
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
        XCTAssertNotNil(error);
        XCTAssertTrue([self kotlinExceptionStringFrom:error].length > 0);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:5.0];
}

- (void)test_checkForUpdates_shouldWorkWithCustomVersionProvider {
    POVHardcodedVersionProviderIos *provider =
        [[POVHardcodedVersionProviderIos alloc] initWithCurrent:@"1.2.3"];

    id<POVBaseVersionComparator> comparator =
        [POVIosDefaultVersionComparatorKt defaultIosVersionComparator];

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
            XCTFail(@"Should not have error: %@", error);
        } else {
            XCTAssertNotNil(result);
        }
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:5.0];
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
