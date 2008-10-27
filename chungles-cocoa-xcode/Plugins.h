#import <Cocoa/Cocoa.h>


@interface Plugins : NSObject {
	NSMutableArray *checkboxes;
	NSMutableArray *name;
	NSMutableArray *type;
	NSMutableArray *jars;
	NSInteger size;
}

+ (Plugins *)getInstance;
- (id)init;
- (void)loadList;
- (void)removePlugin:(NSInteger)selectedRow;
- (void)addPlugin:(NSString *)path;
- (NSMutableArray *)getNames;
- (NSMutableArray *)getCheckboxes;
- (NSMutableArray *)getJARs;
@end
