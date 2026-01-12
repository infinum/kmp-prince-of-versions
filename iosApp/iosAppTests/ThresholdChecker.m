//
//  ThresholdChecker.m
//  iosAppTests
//
//  Created by Filip Stojanovski on 7.11.25.
//

#import <Foundation/Foundation.h>
#import "ThresholdChecker.h"

@implementation ThresholdChecker
- (BOOL)checkRequirementsValue:(NSString * _Nullable)value {
    NSInteger number = value.integerValue;
    return number >= 5;
}
@end
