#import "Shares.h"
#import "MainApp.h"
#import "Preferences.h"
#import <jni.h>
static Shares *inst;
@implementation Shares

+ (Shares *)getInstance {
	if (inst==nil)
		inst=[[Shares alloc] init];
	return inst;
}

- (id)init {
	inst=self;
	sharetable=[[NSMutableArray array] retain];
	pathtable=[[NSMutableArray array] retain];
	size=0;
	
	JavaVM *jvm=get_jvm();
	JNIEnv *env;
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
	
	jclass conf_cls=(*env)->FindClass(env,"org/chungles/core/Configuration");
	jmethodID mid=(*env)->GetStaticMethodID(env,conf_cls,"getSharesIterator", "()Ljava/util/Iterator;");
	jobject iter=(*env)->CallStaticObjectMethod(env,conf_cls,mid);
	
	jclass iter_cls=(*env)->FindClass(env,"java/util/Iterator");
	jmethodID hasnext_mid=(*env)->GetMethodID(env,iter_cls,"hasNext","()Z");
	
	while ((*env)->CallBooleanMethod(env,iter,hasnext_mid))
	{
		mid=(*env)->GetMethodID(env,iter_cls,"next","()Ljava/lang/Object;");
		jobject share_j=(*env)->CallObjectMethod(env,iter,mid);
		const char *share_c=(*env)->GetStringUTFChars(env,share_j,NULL);
		mid=(*env)->GetStaticMethodID(env,conf_cls,"getSharePath","(Ljava/lang/String;)Ljava/lang/String;");
		jobject sharepath_j=(*env)->CallStaticObjectMethod(env,conf_cls,mid,share_j);
		const char *sharepath_c=(*env)->GetStringUTFChars(env,sharepath_j,NULL);		
		[sharetable addObject:[NSString stringWithUTF8String:share_c]];
		[pathtable addObject:[NSString stringWithUTF8String:sharepath_c]];
		(*env)->ReleaseStringUTFChars(env,share_j,share_c);
		(*env)->ReleaseStringUTFChars(env,sharepath_j,sharepath_c);
		size++;
	}
	
	(*jvm)->DetachCurrentThread(jvm);
	return self;
}

- (NSInteger)numberOfRowsInTableView:(NSTableView *)aTableView {
	return size;
}

- (id)tableView:(NSTableView *)aTableView objectValueForTableColumn:(NSTableColumn *)aTableColumn row:(NSInteger)rowIndex {
	if ([[aTableColumn identifier] isEqualToString:@"Name"])
		return [sharetable objectAtIndex:rowIndex];
	else
		return [pathtable objectAtIndex:rowIndex];
}

- (void)update {
	NSAutoreleasePool *pool = [[NSAutoreleasePool allocWithZone:NULL] init];
	[[[Preferences getInstance] getShares] reloadData];
	[pool release];
}

- (void)addShare:(NSString *)path {
	[sharetable addObject:@"changeme"];
	[pathtable addObject:path];
	size++;
	[self update];
	NSTableView *view=[[Preferences getInstance] getShares];
	[view editColumn:0 row:size-1 withEvent:nil select:YES];
	[view selectRow:size-1 byExtendingSelection:NO];
}

- (void)controlTextDidEndEditing:(NSNotification *)aNotification {
	NSString *name=[[aNotification object] stringValue];
	[sharetable replaceObjectAtIndex:size-1 withObject:name];
	[self update];
	[[[Preferences getInstance] getWindow] setDocumentEdited:YES];
}

- (void)tableViewSelectionDidChange:(NSNotification *)notification {
	if ([[[Preferences getInstance] getShares] selectedRow]==-1)
		[[[Preferences getInstance] getRemoveShare] setEnabled:NO];
	else
		[[[Preferences getInstance] getRemoveShare] setEnabled:YES];
}

- (void)removeShare:(NSInteger)selection {
	[sharetable removeObjectAtIndex:selection];
	[pathtable removeObjectAtIndex:selection];
	size--;
	[self update];
	[[[Preferences getInstance] getWindow] setDocumentEdited:YES];
}

- (NSMutableArray *)getSharetable {
	return sharetable;
}

- (NSMutableArray *)getPathtable {
	return pathtable;
}

@end
