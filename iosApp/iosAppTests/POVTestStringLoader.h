//
//  POVTestStringLoader.h
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
