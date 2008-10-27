#import <Cocoa/Cocoa.h>


@interface Toolbar : NSObject {
	IBOutlet NSToolbarItem *del;
	IBOutlet NSToolbarItem *download;
	IBOutlet NSToolbarItem *multicast;
	IBOutlet NSToolbarItem *newdir;
	IBOutlet NSToolbarItem *upload;
}

- (IBAction)delClick:(id)sender;
- (IBAction)downloadClick:(id)sender;
- (IBAction)multicastClick:(id)sender;
- (IBAction)newdirClick:(id)sender;
- (IBAction)preferencesClick:(id)sender;
- (IBAction)quitClick:(id)sender;
- (IBAction)uploadClick:(id)sender;
- (BOOL)validateToolbarItem:(NSToolbarItem *)theItem;
+ (void)setDel:(BOOL)enabled;
+ (void)setDownload:(BOOL)enabled;
+ (void)setMulticast:(BOOL)enabled;
+ (void)setNewdir:(BOOL)enabled;
+ (void)setUpload:(BOOL)enabled;

@end
