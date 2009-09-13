#import <Foundation/Foundation.h>
#import <Growl/GrowlDefines.h>
#import <JavaVM/jni.h>

@interface growl : NSObject {

}
+ (void) regApp:(NSData *)image;
+ (void) notifyGrowlOf:(NSString *)notificationName title:(NSString *)title description:(NSString *)description;
@end
