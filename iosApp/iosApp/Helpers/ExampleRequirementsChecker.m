//
//  ExampleRequirementsChecker.m
//  iosApp
//
//  Created by Filip Stojanovski on 24.11.25.
//

#import "ExampleRequirementsChecker.h"

@implementation ExampleRequirementsChecker

static const NSInteger kThreshold = 5;

- (BOOL)checkRequirementsValue:(NSString * _Nullable)value error:(NSError * _Nullable __autoreleasing * _Nullable)error {
    if (!value) {
        if (error) {
            *error = [NSError errorWithDomain:@"com.infinum.princeofversions"
                                         code:1
                                     userInfo:@{NSLocalizedDescriptionKey: @"Value cannot be nil"}];
        }
        return NO;
    }

    NSInteger n = [value integerValue];
    if (n == 0 && ![value isEqualToString:@"0"]) {
        // Invalid number format
        if (error) {
            *error = [NSError errorWithDomain:@"com.infinum.princeofversions"
                                         code:2
                                     userInfo:@{NSLocalizedDescriptionKey: [NSString stringWithFormat:@"Invalid number format: %@", value]}];
        }
        return NO;
    }

    return n >= kThreshold;
}

@end
