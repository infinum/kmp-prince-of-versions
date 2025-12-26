//
//  POVTestStringLoader.m
//  iosAppTests
//
//  Created by Filip Stojanovski on 10.11.25.
//

#import <Foundation/Foundation.h>
@import PrinceOfVersions;

@interface POVTestStringLoader : NSObject <POVLoader>
- (instancetype)initWithPayload:(NSString *)payload;
- (instancetype)initWithError:(NSError *)error;
@end

@implementation POVTestStringLoader {
    NSString *_payload;
    NSError *_fakeError;
}

- (instancetype)initWithPayload:(NSString *)payload {
    if ((self = [super init])) { _payload = [payload copy]; }
    return self;
}

- (instancetype)initWithError:(NSError *)error {
    if ((self = [super init])) { _fakeError = error; }
    return self;
}

- (void)loadWithCompletionHandler:(void (^)(NSString * _Nullable, NSError * _Nullable))completionHandler {
    if (_fakeError) {
        completionHandler(nil, _fakeError);
    } else {
        completionHandler(_payload ?: @"", nil);
    }
}

@end


