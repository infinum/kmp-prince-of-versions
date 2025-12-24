//
//  POVTestVersionProvider.m
//  iosAppTests
//

#import "POVTestVersionProvider.h"

@implementation POVTestVersionProvider

- (instancetype)initWithVersion:(NSString *)version {
    if (self = [super init]) {
        _version = version;
    }
    return self;
}

- (id)getVersion {
    return self.version;
}

@end
