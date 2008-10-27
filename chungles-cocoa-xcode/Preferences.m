#import "Preferences.h"
#import "MainApp.h"
#import "Shares.h"
#import "Plugins.h"
#import <jni.h>
static Preferences *inst;
@implementation Preferences

- (void)windowDidBecomeKey:(NSNotification *)notification {
	NSMenu *menu=[[NSApplication sharedApplication] mainMenu];
	NSMenuItem *savemenu=[[[menu itemWithTitle:@"File"] submenu] itemWithTitle:@"Save"];	
	[savemenu setTarget:self];
	[savemenu setEnabled:YES];
	[savemenu setAction:@selector(save)];
}

- (void)windowDidResignKey:(NSNotification *)notification {
	NSMenu *menu=[[NSApplication sharedApplication] mainMenu];
	NSMenuItem *savemenu=[[[menu itemWithTitle:@"File"] submenu] itemWithTitle:@"Save"];
	[savemenu setEnabled:NO];
}

+ (void)openPreferences {
	[NSBundle loadNibNamed:@"Preferences" owner:self];
	while (inst==nil);
	[[inst getWindow] makeKeyWindow];
	
	JavaVM *jvm=get_jvm();
	JNIEnv *env;
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
	
	jclass config_cls=(*env)->FindClass(env,"org/chungles/core/Configuration");
	jmethodID mid=(*env)->GetStaticMethodID(env,config_cls,"getComputerName","()Ljava/lang/String;");
	jobject compname_j=(*env)->CallStaticObjectMethod(env,config_cls,mid);
	const char *compname_c=(*env)->GetStringUTFChars(env,compname_j,NULL);
	[[inst getCompname] setTitle:[NSString stringWithUTF8String:compname_c]];
	(*env)->ReleaseStringUTFChars(env,compname_j,compname_c);
	
	mid=(*env)->GetStaticMethodID(env,config_cls,"getMCastKBPSSpeed","()I");
	int mcastspeed=(*env)->CallStaticIntMethod(env,config_cls,mid);
	[[inst getMcastthrottle] setTitle:[NSString stringWithFormat:@"%i",mcastspeed]];
	
	mid=(*env)->GetStaticMethodID(env,config_cls,"isMCastThrottled","()Z");
	bool isThrottled=(*env)->CallStaticBooleanMethod(env,config_cls,mid);
	[[inst getMcastthrottle] setEnabled:isThrottled];
	if (isThrottled)
		[[inst getEnablemcastthrottle] setState:NSOnState];
	else
		[[inst getEnablemcastthrottle] setState:NSOffState];
	
	mid=(*env)->GetStaticMethodID(env,config_cls,"getMCastShare", "()Ljava/lang/String;");
	jobject mcastsharepath_j=(*env)->CallStaticObjectMethod(env,config_cls,mid);
	const char *mcastsharepath_c=(*env)->GetStringUTFChars(env,mcastsharepath_j,NULL);
	[[inst getMcastshare] setTitle:[NSString stringWithUTF8String:mcastsharepath_c]];
	(*env)->ReleaseStringUTFChars(env,mcastsharepath_j,mcastsharepath_c);
	
	(*jvm)->DetachCurrentThread(jvm);
	[Shares getInstance];
	Plugins *plugins_inst=[Plugins getInstance];
	[[inst getPlugins] setDelegate:plugins_inst];
	[[inst getPlugins] setDataSource:plugins_inst];
}

+ (Preferences *)getInstance {
	return inst;
}

- (NSTableView *)getShares {
	return shares;
}

- (NSButton *)getEnablemcastthrottle {
	return enablemcastthrottle;
}

- (NSTextFieldCell *)getCompname {
	return compname;
}

- (NSTextFieldCell *)getMcastshare {
	return mcastshare;
}

- (NSTextFieldCell *)getMcastthrottle {
	return mcastthrottle;
}

- (id)init {
	inst=self;
	return self;
}

- (NSWindow *)getWindow {
	return window;
}

- (IBAction)addPlugin:(id)sender {
	NSOpenPanel *panel=[NSOpenPanel openPanel];
	[panel setCanChooseFiles:YES];
	[panel setCanChooseDirectories:NO];
	[panel setAllowsMultipleSelection:NO];
	NSInteger ret=[panel runModalForTypes:[NSArray arrayWithObjects:@"jar",nil]];
	if (ret==NSCancelButton)
		return;
	[[Plugins getInstance] addPlugin:[[panel filenames] objectAtIndex:0]];
	
}

- (IBAction)addShare:(id)sender {
	NSOpenPanel *panel=[NSOpenPanel openPanel];
	[panel setCanChooseFiles:NO];
	[panel setCanChooseDirectories:YES];
	[panel setAllowsMultipleSelection:NO];
	NSInteger ret=[panel runModalForTypes:nil];
	if (ret==NSCancelButton)
		return;
	[[Shares getInstance] addShare:[[panel filenames] objectAtIndex:0]];
}

- (IBAction)browseForMCastShare:(id)sender {
    NSOpenPanel *panel=[NSOpenPanel openPanel];
	[panel setCanChooseFiles:NO];
	[panel setCanChooseDirectories:YES];
	[panel setAllowsMultipleSelection:NO];
	NSInteger ret=[panel runModalForTypes:nil];
	if (ret==NSCancelButton)
		return;
	
	[mcastshare setTitle:[[panel filenames] objectAtIndex:0]];
}

- (IBAction)removePlugin:(id)sender {
	int selection=[[self getPlugins] selectedRow];
    [[Plugins getInstance] removePlugin:selection];
}

- (IBAction)removeShare:(id)sender {
    int selection=[[self getShares] selectedRow];
	[[Shares getInstance] removeShare:selection];
}

- (IBAction)setMCastThrottle:(id)sender {	
    [mcastthrottle setEnabled:![mcastthrottle isEnabled]];
	[window setDocumentEdited:YES];
}

- (void)controlTextDidChange:(NSNotification *)aNotification {
	[window setDocumentEdited:YES];
}

- (NSButton *)getRemoveShare {
	return removeShare;
}

- (NSButton *)getRemovePlugin {
	return removePlugin;
}

- (NSTableView *)getPlugins {
	return plugins;
}

- (void)save {
	JavaVM *jvm=get_jvm();
	JNIEnv *env;
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
		
	// clear shares
	jclass cls=(*env)->FindClass(env,"org/chungles/core/Configuration");
	jmethodID mid=(*env)->GetStaticMethodID(env,cls,"clearShares","()V");
	(*env)->CallStaticObjectMethod(env,cls,mid);
		
	// record shares
	mid=(*env)->GetStaticMethodID(env,cls,"addShare","(Ljava/lang/String;Ljava/lang/String;)V");
	NSMutableArray *sharetable=[[Shares getInstance] getSharetable];
	NSMutableArray *pathtable=[[Shares getInstance] getPathtable];
	int i;
	for (i=0; i<[sharetable count] && i<[pathtable count]; i++)
	{
		jobject share=(*env)->NewStringUTF(env,[[sharetable objectAtIndex:i] UTF8String]);
		jobject path=(*env)->NewStringUTF(env,[[pathtable objectAtIndex:i] UTF8String]);
		(*env)->CallObjectMethod(env,cls,mid,share,path);
	}
		
		
	// Set mcast properties
	jobject mcastshare_j=(*env)->NewStringUTF(env, [[[self getMcastshare] title] UTF8String]);
	jboolean mcastenabled_j=([[self getEnablemcastthrottle] state]==NSOnState);
	jint mcastthrottle_j=[[[self getMcastthrottle] title] intValue];
	mid=(*env)->GetStaticMethodID(env,cls,"setMCastShare", "(Ljava/lang/String;)V");
	(*env)->CallStaticObjectMethod(env,cls,mid,mcastshare_j);
	mid=(*env)->GetStaticMethodID(env,cls,"setMCastThrottled","(Z)V");
	(*env)->CallStaticObjectMethod(env,cls,mid,mcastenabled_j);
	mid=(*env)->GetStaticMethodID(env,cls,"setMCastKBPSSpeed", "(I)V");
	(*env)->CallStaticObjectMethod(env,cls,mid,mcastthrottle_j);
		
	// Set plugins
	// First iteration: one going through existing plugins in Configuration, if one does not exist in new table, remove plugin		
	jfieldID fid=(*env)->GetStaticFieldID(env,cls,"UIplugins","Ljava/util/LinkedList;");
	jobject uiplugins=(*env)->GetStaticObjectField(env,cls,fid);
	jclass ll_cls=(*env)->GetObjectClass(env,uiplugins);
	mid=(*env)->GetMethodID(env,ll_cls,"size","()I");
	jint size=(*env)->CallIntMethod(env,uiplugins,mid);
	jint j;
	for (j=0;j<size;) // size changes as plugins are removed
	{
		mid=(*env)->GetMethodID(env,ll_cls,"get","(I)Ljava/lang/Object;");
		jobject uiplugin=(*env)->CallObjectMethod(env,uiplugins,mid,j);
		mid=(*env)->GetMethodID(env,(*env)->GetObjectClass(env,uiplugin),"getMainClass", "()Ljava/lang/String;");
		jobject main_class_j=(*env)->CallObjectMethod(env,uiplugin,mid);		
		const char *main_class_c=(*env)->GetStringUTFChars(env,main_class_j,NULL);
		
		if (![[[Plugins getInstance] getNames] containsObject:[NSString stringWithUTF8String:main_class_c]]) // Active plugin not found in pref table, so unload it
		{
			cls=(*env)->FindClass(env,"org/chungles/plugin/PluginAction");
			mid=(*env)->GetMethodID(env,(*env)->GetObjectClass(env,uiplugin),"isEnabled","()Z");
			if ((*env)->CallBooleanMethod(env,uiplugin,mid)) // if still running, shut 'er down
			{
				mid=(*env)->GetStaticMethodID(env,cls,"shutdownPlugin","(Ljava/lang/String;)V");
				(*env)->CallStaticObjectMethod(env,cls,mid,main_class_j);
			}			
			mid=(*env)->GetStaticMethodID(env,cls,"removePlugin","(Ljava/lang/String;)V");
			(*env)->CallStaticObjectMethod(env,cls,mid,main_class_j);
			size--;
		}
		else
		{
			if (![[NSString stringWithUTF8String:main_class_c] isEqual:@"org.chungles.ui.cocoa.Main"]) // see if need to enable/disable plugin
			{
				mid=(*env)->GetMethodID(env,(*env)->GetObjectClass(env,uiplugin),"isEnabled","()Z");
				jboolean plug_enabled=(*env)->CallBooleanMethod(env,uiplugin,mid);
				NSUInteger index=[[[Plugins getInstance] getNames] indexOfObject:[NSString stringWithUTF8String:main_class_c]];			
				bool boxstate=[[[[Plugins getInstance] getCheckboxes] objectAtIndex:index] state]==NSOnState;
				if (!boxstate && plug_enabled)
				{
					cls=(*env)->FindClass(env, "org/chungles/plugin/PluginAction");
					mid=(*env)->GetStaticMethodID(env,cls,"shutdownPlugin", "(Ljava/lang/String;)V");
					(*env)->CallStaticObjectMethod(env,cls,mid,main_class_j);
				}
				else if (boxstate && !plug_enabled)
				{
					cls=(*env)->FindClass(env, "org/chungles/plugin/PluginAction");
					mid=(*env)->GetStaticMethodID(env,cls,"initPlugin","(Ljava/lang/String;)V");
					(*env)->CallStaticObjectMethod(env,cls,mid,main_class_j);
				}			
			}
			j++;
		}
		
		(*env)->ReleaseStringUTFChars(env,main_class_j,main_class_c);
	}
	
	cls=(*env)->FindClass(env, "org/chungles/core/Configuration");
	fid=(*env)->GetStaticFieldID(env,cls,"otherplugins","Ljava/util/LinkedList;");
	jobject otherplugins=(*env)->GetStaticObjectField(env,cls,fid);
	mid=(*env)->GetMethodID(env,ll_cls,"size","()I");
	size=(*env)->CallIntMethod(env,otherplugins,mid);
	for (j=0;j<size;)
	{
		mid=(*env)->GetMethodID(env,ll_cls,"get","(I)Ljava/lang/Object;");
		jobject otherplugin=(*env)->CallObjectMethod(env,otherplugins,mid,j);
		mid=(*env)->GetMethodID(env,(*env)->GetObjectClass(env,otherplugin),"getMainClass", "()Ljava/lang/String;");
		jobject main_class_j=(*env)->CallObjectMethod(env,otherplugin,mid);		
		const char *main_class_c=(*env)->GetStringUTFChars(env,main_class_j,NULL);
		
		if (![[[Plugins getInstance] getNames] containsObject:[NSString stringWithUTF8String:main_class_c]])
		{
			cls=(*env)->FindClass(env,"org/chungles/plugin/PluginAction");
			mid=(*env)->GetMethodID(env,(*env)->GetObjectClass(env,otherplugin),"isEnabled","()Z");
			if ((*env)->CallBooleanMethod(env,otherplugin,mid)) // if still running, shut 'er down
			{
				mid=(*env)->GetStaticMethodID(env,cls,"shutdownPlugin","(Ljava/lang/String;)V");
				(*env)->CallStaticObjectMethod(env,cls,mid,main_class_j);
			}		
			mid=(*env)->GetStaticMethodID(env,cls,"removePlugin","(Ljava/lang/String;)V");
			(*env)->CallStaticObjectMethod(env,cls,mid,main_class_j);
			size--;
		}
		else
		{
			mid=(*env)->GetMethodID(env,(*env)->GetObjectClass(env,otherplugin),"isEnabled","()Z");
			jboolean plug_enabled=(*env)->CallBooleanMethod(env,otherplugin,mid);
			NSUInteger index=[[[Plugins getInstance] getNames] indexOfObject:[NSString stringWithUTF8String:main_class_c]];			
			bool boxstate=[[[[Plugins getInstance] getCheckboxes] objectAtIndex:index] state]==NSOnState;
			if (!boxstate && plug_enabled)
			{
				cls=(*env)->FindClass(env, "org/chungles/plugin/PluginAction");
				mid=(*env)->GetStaticMethodID(env,cls,"shutdownPlugin", "(Ljava/lang/String;)V");
				(*env)->CallStaticObjectMethod(env,cls,mid,main_class_j);
			}
			else if (boxstate && !plug_enabled)
			{
				cls=(*env)->FindClass(env, "org/chungles/plugin/PluginAction");
				mid=(*env)->GetStaticMethodID(env,cls,"initPlugin","(Ljava/lang/String;)V");
				(*env)->CallStaticObjectMethod(env,cls,mid,main_class_j);
			}
			j++;
		}
			
		(*env)->ReleaseStringUTFChars(env,main_class_j,main_class_c);
	}
			
	// Second iteration: go through list of jars, only non-null ones will be newly added ones. add them.
	NSMutableArray *jars=[[Plugins getInstance] getJARs];
	NSMutableArray *checkboxes=[[Plugins getInstance] getCheckboxes];
	for (i=0; i<[jars count]; i++)
	{
		if (![[jars objectAtIndex:i] isEqual:[NSNull null]])
		{
			cls=(*env)->FindClass(env,"org/chungles/plugin/PluginAction");
			mid=(*env)->GetStaticMethodID(env,cls,"loadPlugin","(Ljava/lang/String;Z)V");
			(*env)->CallStaticObjectMethod(env,cls,mid,(*env)->NewStringUTF(env,[[jars objectAtIndex:i] UTF8String]),
			 [[checkboxes objectAtIndex:i] state]==NSOnState);
		}
	}
		
	// Save configuration
	cls=(*env)->FindClass(env, "org/chungles/core/ConfigurationParser");
	mid=(*env)->GetStaticMethodID(env, cls, "saveConfig", "()V");
	(*env)->CallStaticObjectMethod(env,cls,mid);
		
	(*jvm)->DetachCurrentThread(jvm);
	[window setDocumentEdited:NO];
}

- (BOOL)windowShouldClose:(NSNotification *)notification {
	if ([window isDocumentEdited])
	{
		// Ask to save
		NSAlert *alert=[NSAlert alertWithMessageText:nil defaultButton:@"Save" alternateButton:@"Cancel" otherButton:@"Don't Save" informativeTextWithFormat:@"Do you want to save changes?"];
		[alert beginSheetModalForWindow:window modalDelegate:self didEndSelector:@selector(alertDidEnd:returnCode:contextInfo:) contextInfo:nil];
		return NO;	
	}
	else
		return YES;
}

- (void) alertDidEnd:(NSAlert *)alert returnCode:(int)returnCode contextInfo:(void *)contextInfo {
	if (returnCode!=NSAlertAlternateReturn)
	{	
		if (returnCode==NSAlertDefaultReturn)
			[self save];		
		[window close];
	}
}

@end
