#import <Cocoa/Cocoa.h>

@interface Progress : NSObject {
	IBOutlet NSPanel *ProgressPanel;
	IBOutlet NSProgressIndicator *FileProgressBar;
	IBOutlet NSProgressIndicator *TotalProgressBar;
	IBOutlet NSTextField *FileLabel;
}

+ (Progress *)getInstance;
- (NSPanel *)getPanel;
- (NSProgressIndicator *)getFileProgressBar;
- (NSProgressIndicator *)getTotalProgressBar;
- (NSTextField *)getFileLabel;

@end
