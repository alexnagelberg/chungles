#import "Plugins.h"
#import "Preferences.h"
#import <jni.h>
#import "MainApp.h"

static Plugins *inst;
@implementation Plugins
+ (Plugins *)getInstance {
	if (inst==nil)
	{
		inst=[Plugins alloc];
		[inst loadList];
	}
	return inst;
}

- (id)init {
	return [Plugins getInstance];
}

- (void)loadList {
	size=0;
	checkboxes=[[NSMutableArray array] retain];
	name=[[NSMutableArray array] retain];
	type=[[NSMutableArray array] retain];
	jars=[[NSMutableArray array] retain];
	
	JavaVM *jvm=get_jvm();
	JNIEnv *env;
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
	
	jclass conf_cls=(*env)->FindClass(env,"org/chungles/core/Configuration");
	jfieldID fid=(*env)->GetStaticFieldID(env,conf_cls,"UIplugins","Ljava/util/LinkedList;");
	jobject uiplugins=(*env)->GetStaticObjectField(env,conf_cls,fid);
	fid=(*env)->GetStaticFieldID(env,conf_cls,"otherplugins","Ljava/util/LinkedList;");
	jobject otherplugins=(*env)->GetStaticObjectField(env,conf_cls,fid);
	
	jclass cls=(*env)->FindClass(env,"java/util/LinkedList");
	jmethodID mid=(*env)->GetMethodID(env,cls,"listIterator", "(I)Ljava/util/ListIterator;");
	jobject iter=(*env)->CallObjectMethod(env,uiplugins,mid,0);
	
	cls=(*env)->FindClass(env,"java/util/ListIterator");
	mid=(*env)->GetMethodID(env,cls,"hasNext","()Z");
	jmethodID next_mid=(*env)->GetMethodID(env,cls,"next", "()Ljava/lang/Object;");
	while ((*env)->CallBooleanMethod(env,iter,mid))
	{
		jobject cur=(*env)->CallObjectMethod(env,iter,next_mid);
		cls=(*env)->FindClass(env,"org/chungles/plugin/PluginInfo");
		jmethodID classname_mid=(*env)->GetMethodID(env,cls,"getMainClass","()Ljava/lang/String;");
		jobject pluginname_j=(*env)->CallObjectMethod(env,cur,classname_mid);
		const char *pluginname_c=(*env)->GetStringUTFChars(env,pluginname_j,NULL);
		[name addObject:[NSString stringWithUTF8String:pluginname_c]];
		[type addObject:@"UI"];
		jmethodID enabled_mid=(*env)->GetMethodID(env,cls,"isEnabled","()Z");
		NSButton *enabled_button=[[NSButton alloc] init];
		[enabled_button setButtonType:NSSwitchButton];
		if ((*env)->CallBooleanMethod(env,cur,enabled_mid))
			[enabled_button setState:NSOnState];
		else
			[enabled_button setState:NSOffState];
		[checkboxes addObject:enabled_button];
		[jars addObject:[NSNull null]];
		(*env)->ReleaseStringUTFChars(env,pluginname_j,pluginname_c);
		size++;
	}
	
	cls=(*env)->FindClass(env,"java/util/LinkedList");
	mid=(*env)->GetMethodID(env,cls,"listIterator","(I)Ljava/util/ListIterator;");
	iter=(*env)->CallObjectMethod(env,otherplugins,mid,0);
	cls=(*env)->FindClass(env,"java/util/ListIterator");
	mid=(*env)->GetMethodID(env,cls,"hasNext","()Z");
	while ((*env)->CallBooleanMethod(env,iter,mid))
	{
		//jmethodID next_mid=(*env)->GetMethodID(env,cls,"next", "()Ljava/lang/Object;");
		jobject cur=(*env)->CallObjectMethod(env,iter,next_mid);
		cls=(*env)->FindClass(env,"org/chungles/plugin/PluginInfo");
		jmethodID classname_mid=(*env)->GetMethodID(env,cls,"getMainClass","()Ljava/lang/String;");
		jobject pluginname_j=(*env)->CallObjectMethod(env,cur,classname_mid);
		const char *pluginname_c=(*env)->GetStringUTFChars(env,pluginname_j,NULL);
		[name addObject:[NSString stringWithUTF8String:pluginname_c]];
		[type addObject:@"other"];
		jmethodID enabled_mid=(*env)->GetMethodID(env,cls,"isEnabled","()Z");
		NSButton *enabled_button=[[NSButton alloc] init];
		[enabled_button setButtonType:NSSwitchButton];
		if ((*env)->CallBooleanMethod(env,cur,enabled_mid))
			[enabled_button setState:NSOnState];
		else
			[enabled_button setState:NSOffState];
		[checkboxes addObject:enabled_button];
		[jars addObject:[NSNull null]];
		(*env)->ReleaseStringUTFChars(env,pluginname_j,pluginname_c);
		size++;
	}
	
	(*jvm)->DetachCurrentThread(jvm);
}

- (void)update {
	NSAutoreleasePool *pool = [[NSAutoreleasePool allocWithZone:NULL] init];
	[[[Preferences getInstance] getPlugins] reloadData];
	[pool release];
}

- (NSInteger)numberOfRowsInTableView:(NSTableView *)aTableView {
	return size;
}

- (id)tableView:(NSTableView *)aTableView objectValueForTableColumn:(NSTableColumn *)aTableColumn row:(NSInteger)rowIndex {
	if ([[aTableColumn identifier] isEqualToString:@"Plugin"])
		return [name objectAtIndex:rowIndex];
	else if ([[aTableColumn identifier] isEqualToString:@"Type"])
		return [type objectAtIndex:rowIndex];
	else
		return [checkboxes objectAtIndex:rowIndex];
}

- (void)tableViewSelectionDidChange:(NSNotification *)notification {
	if ([[[Preferences getInstance] getPlugins] selectedRow]==-1 || [[name objectAtIndex:[[[Preferences getInstance] getPlugins] selectedRow]] isEqual:@"org.chungles.ui.cocoa.Main"])
		[[[Preferences getInstance] getRemovePlugin] setEnabled:NO];
	else
		[[[Preferences getInstance] getRemovePlugin] setEnabled:YES];
}

- (void)removePlugin:(NSInteger)selectedRow {
	[name removeObjectAtIndex:selectedRow];
	[type removeObjectAtIndex:selectedRow];
	[checkboxes removeObjectAtIndex:selectedRow];
	[jars removeObjectAtIndex:selectedRow];
	size--;
	[self update];
	[[[Preferences getInstance] getWindow] setDocumentEdited:YES];
}

- (void)addPlugin:(NSString *)path {
	JavaVM *jvm=get_jvm();
	JNIEnv *env;
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
	
	jclass cls=(*env)->FindClass(env,"org/chungles/plugin/PluginAction");
	jmethodID mid=(*env)->GetStaticMethodID(env,cls,"PeekInJAR","(Ljava/lang/String;)Lorg/chungles/plugin/PluginInfo;");
	jobject plugin_info=(*env)->CallStaticObjectMethod(env,cls,mid,(*env)->NewStringUTF(env,[path UTF8String]));
	
	cls=(*env)->FindClass(env, "org/chungles/plugin/PluginInfo");
	mid=(*env)->GetMethodID(env,cls,"getMainClass","()Ljava/lang/String;");
	jobject main_class_j=(*env)->CallObjectMethod(env,plugin_info,mid);
	const char *main_class_c=(*env)->GetStringUTFChars(env,main_class_j, NULL);
	NSString *main_class=[NSString stringWithUTF8String:main_class_c];
	(*env)->ReleaseStringUTFChars(env,main_class_j,main_class_c);
	if ([name containsObject:main_class])
	{
		NSAlert *alert=[NSAlert alertWithError:@"Plugin conflicts with existing plugin."];
		[alert runModal];
		return;
	}
	
	[name addObject:main_class];
	
	mid=(*env)->GetMethodID(env,cls,"getType","()I");
	int plugin_type=(*env)->CallIntMethod(env,plugin_info,mid);
	if (plugin_type==0) //UI plugin
		[type addObject:@"UI"];
	else
		[type addObject:@"other"];
	
	NSButton *enabled_button=[[NSButton alloc] init];
	[enabled_button setButtonType:NSSwitchButton];
	[enabled_button setState:NSOnState];
	[checkboxes addObject:enabled_button];
	[jars addObject:path];
	size++;
	[self update];
	[[[Preferences getInstance] getWindow] setDocumentEdited:YES];
	
	(*jvm)->DetachCurrentThread(jvm);
}

- (NSMutableArray *)getNames {
	return name;
}

- (NSMutableArray *)getCheckboxes {
	return checkboxes;
}

- (NSMutableArray *)getJARs {
	return jars;
}

- (void)test {
	fprintf(stderr, "clck\n");
}

- (void)tableView:(NSTableView *)aTableView setObjectValue:(id)anObject forTableColumn:(NSTableColumn *)aTableColumn row:(NSInteger)rowIndex {
	if ([[aTableColumn identifier] isEqualToString:@"Enabled"] && ![[name objectAtIndex:rowIndex] isEqual:@"org.chungles.ui.cocoa.Main"])
	{
		[[checkboxes objectAtIndex:rowIndex] setObjectValue:anObject];
		[[[Preferences getInstance] getWindow] setDocumentEdited:YES];
	}
}

@end
