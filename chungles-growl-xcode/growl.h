#import <Foundation/Foundation.h>
#import <GrowlDefines.h>
#import <jni.h>

@interface growl : NSObject {

}
+ (void) regApp:(NSData *)image;
+ (void) notifyGrowlOf:(NSString *)notificationName title:(NSString *)title description:(NSString *)description;
@end
