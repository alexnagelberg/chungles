#import <Cocoa/Cocoa.h>
#import "Shares.h"

@interface Preferences : NSObject {
    IBOutlet NSTableView *shares;
    IBOutlet NSTableView *plugins;
    IBOutlet NSTextFieldCell *compname;
    IBOutlet NSTextFieldCell *mcastshare;
    IBOutlet NSButton *enablemcastthrottle;
    IBOutlet NSTextFieldCell *mcastthrottle;
	IBOutlet NSWindow *window;
	IBOutlet NSButton *removeShare;
	IBOutlet NSButton *removePlugin;
	Shares *shares_source;	
}
+ (void)openPreferences;
+ (Preferences *)getInstance;
- (id)init;
- (NSWindow *)getWindow;
- (IBAction)addPlugin:(id)sender;
- (IBAction)addShare:(id)sender;
- (IBAction)browseForMCastShare:(id)sender;
- (IBAction)removePlugin:(id)sender;
- (IBAction)removeShare:(id)sender;
- (IBAction)setMCastThrottle:(id)sender;
- (NSTextFieldCell *)getCompname;
- (NSTextFieldCell *)getMcastshare;
- (NSTextFieldCell *)getMcastthrottle;
- (NSButton *)getEnablemcastthrottle;
- (NSTableView *)getShares;
- (NSButton *)getRemoveShare;
- (NSTableView *)getPlugins;
- (NSButton *)getRemovePlugin;
- (void)controlTextDidChange:(NSNotification *)aNotification;
- (void) alertDidEnd:(NSAlert *)alert returnCode:(int)returnCode contextInfo:(void *)contextInfo;
@end
