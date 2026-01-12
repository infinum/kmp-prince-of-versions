//
//  POVTestVersionProvider.h
//  iosAppTests
//

#import <Foundation/Foundation.h>
@import PrinceOfVersions;

NS_ASSUME_NONNULL_BEGIN

/// Simple test-only version provider
@interface POVTestVersionProvider : NSObject <POVBaseApplicationVersionProvider>
@property (nonatomic, copy) NSString *version;
- (instancetype)initWithVersion:(NSString *)version;
@end

NS_ASSUME_NONNULL_END
