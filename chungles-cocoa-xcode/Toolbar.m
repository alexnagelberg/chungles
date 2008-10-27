#import "Toolbar.h"
#import "MainApp.h"
#import "Outline.h"
#import "OutlineItem.h"
#import "FileTransfer.h"
#import "Preferences.h"
#import <jni.h>

@implementation Toolbar

static BOOL delEnabled=NO;
static BOOL downloadEnabled=NO;
static BOOL multicastEnabled=YES;
static BOOL newdirEnabled=NO;
static BOOL uploadEnabled=NO;

- (IBAction)delClick:(id)sender {
	OutlineItem *item=[[Outline getInstance] getSelection];
	NSAlert *alert=[NSAlert alertWithMessageText:@"Sure?" defaultButton:@"No wait" alternateButton:@"Do it" otherButton:nil informativeTextWithFormat:@"Are you sure you want to delete this?"];
	int alertret=[alert runModal];
	if (alertret!=NSAlertAlternateReturn)
		return;
	
	JavaVM *jvm=get_jvm();
	JNIEnv *env;
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
	jclass cls=(*env)->FindClass(env,"org/chungles/core/Client");
	jmethodID mid=(*env)->GetMethodID(env,cls,"<init>","(Ljava/lang/String;)V");
	jobject client_inst=(*env)->NewObject(env,cls,mid,(*env)->NewStringUTF(env, [item getIP]));
	mid=(*env)->GetMethodID(env,cls,"deleteFile","(Ljava/lang/String;)Z");
	jboolean success=(*env)->CallBooleanMethod(env,client_inst,mid,(*env)->NewStringUTF(env, [item getRelativePath]));

	if (success)
		[item removeItem];
	else
		[[NSAlert alertWithMessageText:@"Error" defaultButton:@"Ok" alternateButton:nil otherButton:nil informativeTextWithFormat:@"Error deleting"] runModal];	

	// Put away your toys when you're finished playing with them
	mid=(*env)->GetMethodID(env,cls,"close","()V");
	(*env)->CallObjectMethod(env,client_inst,mid);	
	(*jvm)->DetachCurrentThread(jvm);
}

- (IBAction)downloadClick:(id)sender {
	NSOpenPanel *panel=[NSOpenPanel openPanel];
	[panel setAllowsMultipleSelection:NO];
	[panel setCanChooseDirectories:YES];
	[panel setCanChooseFiles:NO];
	[panel setTitle:@"Download to..."];
	[panel setPrompt:@"Save"];
	if ([panel runModalForDirectory:nil file:nil]==NSCancelButton)
		return;
	
	NSArray *objects=[NSArray arrayWithObjects:[[Outline getInstance] getSelection],[[panel filenames] objectAtIndex:0],nil];
	NSArray *keys=[NSArray arrayWithObjects:@"item",@"file",nil];
	[NSThread detachNewThreadSelector:@selector(download:) toTarget:[FileTransfer alloc] withObject:[NSDictionary dictionaryWithObjects:objects forKeys:keys]];
}

- (IBAction)multicastClick:(id)sender {
	NSOpenPanel *panel=[NSOpenPanel openPanel];
	[panel setAllowsMultipleSelection:NO];
	[panel setCanChooseDirectories:NO];
	if ([panel runModalForDirectory:nil file:nil]==NSCancelButton)
		return;
	
	[NSThread detachNewThreadSelector:@selector(mcast:) toTarget:[FileTransfer alloc] withObject:panel];
}

- (IBAction)newdirClick:(id)sender {
	Outline *outline=[Outline getInstance];
	int row=[[outline getView] selectedRow]+1;
	[[outline getSelection] addChild:@"New Directory" isExpandable:YES];
	[[outline getView] expandItem:[outline getSelection]];
	[[outline getView] editColumn:0 row:row withEvent:nil select:YES];
	[[outline getView] selectRow:row byExtendingSelection:NO];
	
}

- (IBAction)preferencesClick:(id)sender {
	[Preferences openPreferences];
}

- (IBAction)quitClick:(id)sender {
	[[NSApplication sharedApplication] terminate:self];
}

- (IBAction)uploadClick:(id)sender {
	
	NSOpenPanel *panel=[NSOpenPanel openPanel];
	[panel setAllowsMultipleSelection:YES];
	[panel setCanChooseDirectories:YES];
	if ([panel runModalForDirectory:nil file:nil]==NSCancelButton)
		return;
	
	NSArray *objects=[NSArray arrayWithObjects:[[Outline getInstance] getSelection],[panel filenames],nil];
	NSArray *keys=[NSArray arrayWithObjects:@"item",@"files",nil];
	[NSThread detachNewThreadSelector:@selector(upload:) toTarget:[FileTransfer alloc] withObject:[NSDictionary dictionaryWithObjects:objects forKeys:keys]];
}

+ (void)setDel:(BOOL)enabled {
	delEnabled=enabled;
}

+ (void)setDownload:(BOOL)enabled {
	downloadEnabled=enabled;
}

+ (void)setMulticast:(BOOL)enabled {
	multicastEnabled=enabled;
}

+ (void)setNewdir:(BOOL)enabled {
	newdirEnabled=enabled;
}

+ (void)setUpload:(BOOL)enabled {
	uploadEnabled=enabled;
}

- (BOOL)validateToolbarItem:(NSToolbarItem *)theItem {
	if ([theItem tag]==1)
		return delEnabled;
	else if ([theItem tag]==2)
		return downloadEnabled;
	else if ([theItem tag]==3)
		return multicastEnabled;
	else if ([theItem tag]==4)
		return newdirEnabled;
	else if ([theItem tag]==5)
		return uploadEnabled;
	else
		return NO;
}

@end
