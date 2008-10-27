#import "Progress.h"

static Progress *inst;
@implementation Progress
+ (Progress *)getInstance {
	return inst;
}

- (id)init {
	inst=self;
	return self;
}

- (NSPanel *)getPanel {
	return inst->ProgressPanel;
}

- (NSProgressIndicator *)getFileProgressBar {
	return FileProgressBar;
}

- (NSProgressIndicator *)getTotalProgressBar {
	return TotalProgressBar;
}

- (NSTextField *)getFileLabel {
	return FileLabel;
}
@end
