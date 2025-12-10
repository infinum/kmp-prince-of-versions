//
//  VersionProviderObjcTests.m
//  iosAppTests
//
//  Created by Filip Stojanovski on 6.11.25.
//

#import <XCTest/XCTest.h>
@import PrinceOfVersions;
#import "ThresholdChecker.h"

@interface VersionProviderObjcTests : XCTestCase
@end

@implementation VersionProviderObjcTests

- (void)testReadsVersionFromHostInfoPlist {
    POVTestHooks *hooks = POVTestHooks.shared;
    NSString *version = [hooks exposeAppVersionForUnitTests];

    XCTAssertTrue(version.length > 0);
    XCTAssertTrue([version containsString:@"-"],
                  @"Expected short-build like 1.2.3-456; got %@", version);
}

- (NSString *)kotlinExceptionStringFrom:(NSError *)error {
    id ke = error.userInfo[@"KotlinException"];
    return ke ? [ke description] : error.localizedDescription;
}

- (void)testInvalidURL_YieldsIoError {
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

- (void)testRequirementsNotSatisfied_YieldsError {
    ThresholdChecker *checker = [ThresholdChecker new];

    id<POVPrinceOfVersionsBase> pov =
    [POVIosPrinceOfVersionsKt princeOfVersionsWithCustomCheckerKey:@"requiredNumberOfLetters"
                                                           checker:checker
                                               keepDefaultCheckers:YES];

    NSString *url = @"https://pastebin.com/raw/VMgd71VH"; // use your stable fixture

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

- (void)testBadConfig_YieldsConfigurationException {
    id<POVPrinceOfVersionsBase> pov = [POVIosPrinceOfVersionsKt PrinceOfVersions];

    // Point to a JSON that definitely lacks the "ios" key
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
        // Optionally assert the error *class*:
        // XCTAssertTrue([error isKindOfClass:[POVConfigurationException class]]);
        XCTAssertTrue([self kotlinExceptionStringFrom:error].length > 0);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:5.0];
}

- (void)testCompletesWithinTimeout {
    id<POVPrinceOfVersionsBase> pov = [POVIosPrinceOfVersionsKt PrinceOfVersions];

    XCTestExpectation *exp = [self expectationWithDescription:@"completes"];
    [POVIosPrinceOfVersionsKt checkForUpdatesFromUrl:pov
                                                       url:@"not a url"
                                                  username:nil
                                                  password:nil
                                       networkTimeout:3000
                                         completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                             NSError * _Nullable error)
    {
        XCTAssertTrue(result != nil || error != nil);
        [exp fulfill];
    }];
    [self waitForExpectations:@[exp] timeout:10.0];
}

- (void)testCustomVersionLogic {
    // 1) Provider + comparator
    POVHardcodedVersionProviderIos *provider =
        [[POVHardcodedVersionProviderIos alloc] initWithCurrent:@"1.2.3"];

    id<POVBaseVersionComparator> baseComparator =
            [POVIosDefaultVersionComparatorKt defaultIosVersionComparator];
    POVDevBuildVersionComparator *comparator =
        [[POVDevBuildVersionComparator alloc] initWithDelegate:baseComparator];
    id<POVPrinceOfVersionsBase> pov =
        [POVIosDefaultVersionComparatorKt princeOfVersionsWithCustomVersionLogicProvider:provider
                                                                             comparator:comparator];

    [POVIosPrinceOfVersionsKt checkForUpdatesFromUrl:pov
                                                 url:@"https://pastebin.com/raw/KgAZQUb5"
                                            username:nil
                                            password:nil
                                      networkTimeout:3000
                                   completionHandler:^(POVBaseUpdateResult<NSString *> * _Nullable result,
                                                       NSError * _Nullable error) {
        if (error) {
            NSLog(@"Error: %@", error);
        } else {
            NSLog(@"Status=%@ Version=%@", result.status, result.version);
        }
    }];
}

@end

