//
//  VersionProviderObjcTests.m
//  iosAppTests
//
//  Created by Filip Stojanovski on 6.11.25.
//

#import <XCTest/XCTest.h>

@import PrinceOfVersions;

@interface VersionProviderObjcTests : XCTestCase

@end

@implementation VersionProviderObjcTests

- (void)testReadsVersionFromHostInfoPlist {
    POVTestHooks *hooks = [POVTestHooks shared];
    NSString *version = [hooks exposeAppVersionForUnitTests];

    XCTAssertTrue(version.length > 0);
    XCTAssertTrue([version containsString:@"-"],
                  @"Expected short-build like 1.2.3-456; got %@", version);
}

@end
