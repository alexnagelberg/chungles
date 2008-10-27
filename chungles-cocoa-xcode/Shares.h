#import <Cocoa/Cocoa.h>

@interface Shares : NSObject {
	NSInteger size;
	NSMutableArray *sharetable;
	NSMutableArray *pathtable;
}
- (void)addShare:(NSString *)path;
- (void)removeShare:(NSInteger)selection;
+ (Shares *)getInstance;
- (NSMutableArray *)getSharetable;
- (NSMutableArray *)getPathtable;

@end
