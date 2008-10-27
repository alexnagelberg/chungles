#import "Outline.h"
#import "OutlineItem.h"
#import "Toolbar.h"
#import "MainApp.h"
#import "FileTransfer.h"
#import <jni.h>

static Outline *inst;

@implementation Outline

+ (Outline *)getInstance {
	return inst;
}

- (NSOutlineView *)getView {
	return outline;
}

- (void)update {
	NSAutoreleasePool *pool = [[NSAutoreleasePool allocWithZone:NULL] init];
	[outline reloadData];
	[pool release];
}

- (id)init {
	inst=self;
	return self;
}

- (void)addItem:(char *)name {
}

- (void)outlineViewSelectionDidChange:(NSNotification *)notification {
	OutlineItem *item=[outline itemAtRow:[outline selectedRow]];
	selection=item;
	if ([item isExpandable])
	{
		// Share directory
		if ([item getParent]!=nil && [[item getParent] isRootItem])
		{
			[Toolbar setDownload:NO];
			[Toolbar setDel:NO];
			[Toolbar setNewdir:YES];
			[Toolbar setUpload:YES];
		}
		// Node
		else if ([item isRootItem])
		{
			[Toolbar setDownload:NO];
			[Toolbar setDel:NO];
			[Toolbar setNewdir:NO];
			[Toolbar setUpload:NO];
		}
		// Everything below
		else
		{	
			[Toolbar setDownload:YES];
			[Toolbar setDel:YES];
			[Toolbar setNewdir:YES];
			[Toolbar setUpload:YES];
		}
	}
	else
	{
		[Toolbar setDownload:YES];
		[Toolbar setDel:YES];
		[Toolbar setNewdir:NO];
		[Toolbar setUpload:NO];
	}
}

- (OutlineItem *)getSelection {
	return selection;
}

- (void)outlineViewItemDidCollapse:(NSNotification *)notification {
	OutlineItem *item=(OutlineItem *)[[notification userInfo] objectForKey:@"NSObject"];
	[item clearChildren];
	[self update];
}

- (void)outlineViewItemDidExpand:(NSNotification *)notification {
	OutlineItem *item=(OutlineItem *)[[notification userInfo] objectForKey:@"NSObject"];
	//fprintf(stderr, "listing %s\n", [item getPath]);
	JavaVM *jvm=get_jvm();
	JNIEnv *env;
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
	jclass cls=(*env)->FindClass(env, "org/chungles/plugin/FileSystem");

	// Create new instance
	jmethodID mid=(*env)->GetMethodID(env, cls, "<init>", "()V");
	jobject class_inst=(*env)->NewObject(env,cls,mid);
	
	// Change to path
	mid=(*env)->GetMethodID(env, cls, "changeDirectory", "(Ljava/lang/String;)V");
	jobject cur_path=(*env)->NewStringUTF(env, [item getPath]);
	(*env)->CallObjectMethod(env, class_inst, mid, cur_path);
	
	// List path
	mid=(*env)->GetMethodID(env, cls, "listPath", "()[Ljava/lang/String;");
	jobject paths=(*env)->CallObjectMethod(env, class_inst, mid);
	(*env)->ExceptionDescribe(env);
	
	int num_files=(*env)->GetArrayLength(env, paths);
	int i;
	for (i=0; i<num_files; i++)
	{
		jobject file=(*env)->GetObjectArrayElement(env, paths, i);
		const char *filename=(*env)->GetStringUTFChars(env, file, JNI_FALSE);
		[item addChild:[NSString stringWithUTF8String:(filename+1)] isExpandable:(filename[0]=='D')];
	}
	[self update];
	(*jvm)->DetachCurrentThread(jvm);
}

- (NSInteger)outlineView:(NSOutlineView *)outlineView numberOfChildrenOfItem:(id)item {
	if (item==nil) //root items
		return [OutlineItem numRoots];
	else		
		return [item numberOfChildren];
}

- (BOOL)outlineView:(NSOutlineView *)outlineView isItemExpandable:(id)item {
	return [item isExpandable];
}

- (id)outlineView:(NSOutlineView *)outlineView child:(NSInteger)index ofItem:(id)item {	
	if (item==nil)
		return [OutlineItem rootItem:index];
	else
		return [(OutlineItem *)item getChild:index];
}

- (id)outlineView:(NSOutlineView *)outlineView objectValueForTableColumn:(NSTableColumn *)tableColumn byItem:(id)item {
	return [item getName];
}

- (void)controlTextDidEndEditing:(NSNotification *)aNotification {
	NSString *name=[[aNotification object] stringValue];
	OutlineItem *item=[self getSelection];	
	[item updateName:name];
	[self update];
	
	JavaVM *jvm=get_jvm();
	JNIEnv *env;
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
	jclass cls=(*env)->FindClass(env, "org/chungles/core/Client");
	jmethodID mid=(*env)->GetMethodID(env,cls,"<init>","(Ljava/lang/String;)V");
	jobject client=(*env)->NewObject(env,cls,mid,(*env)->NewStringUTF(env,[item getIP]));
	
	mid=(*env)->GetMethodID(env,cls,"mkdir","(Ljava/lang/String;Ljava/lang/String;)Z");
	(*env)->CallBooleanMethod(env,client,mid,(*env)->NewStringUTF(env,[[item getParent] getRelativePath]), (*env)->NewStringUTF(env,[name UTF8String]));
	
	mid=(*env)->GetMethodID(env,cls,"close","()V");
	(*env)->CallObjectMethod(env,client,mid);
	(*jvm)->DetachCurrentThread(jvm);
}

- (BOOL)outlineView:(NSOutlineView *)outlineView acceptDrop:(id < NSDraggingInfo >)info item:(OutlineItem *)item childIndex:(NSInteger)index {
	NSPasteboard *pboard=[info draggingPasteboard];
	NSArray *files=[pboard propertyListForType:NSFilenamesPboardType];
	
	NSDictionary *dict=[NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:files,item,nil] forKeys:[NSArray arrayWithObjects:@"files",@"item",nil]];
	[NSThread detachNewThreadSelector:@selector(upload:) toTarget:[FileTransfer alloc] withObject:dict];
	
	return YES;
}

- (NSDragOperation)outlineView:(NSOutlineView *)outlineView validateDrop:(id < NSDraggingInfo >)info proposedItem:(OutlineItem *)item proposedChildIndex:(NSInteger)index {
	NSPasteboard *pboard;
    NSDragOperation sourceDragMask;
 
    sourceDragMask = [info draggingSourceOperationMask];
    pboard = [info draggingPasteboard];
		
	if ((sourceDragMask & NSDragOperationCopy) && ![item isRootItem] && [item isExpandable])
		return NSDragOperationCopy;
	else
		return NSDragOperationNone;
}

@end
