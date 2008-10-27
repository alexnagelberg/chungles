#import <Cocoa/Cocoa.h>


@interface OutlineItem : NSObject {
	NSString *name;
	OutlineItem *parent;
	NSMutableArray *children;
	BOOL expandable;
	NSString *path;
	NSString *relativePath;
	NSString *IP;
}

+ (int)numRoots;
+ (void)addRootItem:(NSString *)name ip:(NSString *)IP;
+ (void)delRootItem:(NSString *)name ip:(NSString *)IP;
+ (OutlineItem *)rootItem:(NSInteger)index;
- (id)initWithStr:(NSString *)str;
- (int)numberOfChildren;
- (NSString *)getName;
- (const char *)getPath;
- (BOOL)isExpandable;
- (OutlineItem *)getChild:(NSInteger)index;
- (void) addChild:(NSString *)childname isExpandable:(BOOL)isExpandable;
- (void) clearChildren;
- (BOOL) isRootItem;
- (OutlineItem *)getParent;
- (const char *)getIP;
- (const char *)getRelativePath;
- (void) removeItem;
- (void) updateName:(NSString *)newName;

@end
