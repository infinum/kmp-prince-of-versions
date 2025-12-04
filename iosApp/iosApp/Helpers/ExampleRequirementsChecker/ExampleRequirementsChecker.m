//
//  ExampleRequirementsChecker.m
//  iosApp
//
//  Created by Filip Stojanovski on 4.12.25.
//

#import <Foundation/Foundation.h>

#import "ExampleRequirementsChecker.h"

@implementation ExampleRequirementsChecker

- (BOOL)checkRequirementsValue:(NSString * _Nullable)value
                         error:(NSError * _Nullable * _Nullable)error
{
    // Convert String → Integer
    NSInteger n = value.integerValue;

    // Example of throwing an Objective-C error
    if (n < 0) {
        if (error != NULL) {
            *error = [NSError errorWithDomain:@"RequirementErrorDomain"
                                         code:1
                                     userInfo:@{
                                         NSLocalizedDescriptionKey: @"Value cannot be negative"
                                     }];
        }
        return NO;
    }

    // Return true if valid
    return n >= 5;
}

@end
