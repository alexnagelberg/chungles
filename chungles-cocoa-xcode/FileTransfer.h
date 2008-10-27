#import <Cocoa/Cocoa.h>

@interface FileTransfer : NSObject {

}

- (void) mcast:(NSOpenPanel *)panel;
- (void) download:(NSDictionary *)dict;
- (void)upload:(NSDictionary *)dict;

@end
