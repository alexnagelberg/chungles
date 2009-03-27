#import "FileTransfer.h"
#import "Progress.h"
#import "OutlineItem.h"
#import "MainApp.h"
#import <jni.h>

const int SENDING_FILES=13;
const int SENT_FILES=14;
const int GETTING_FILES=15;
const int GOT_FILES=16;

JNIEXPORT void JNICALL updateProgress(JNIEnv *env, jobject obj, jlong increment)
{
	Progress *progress=[Progress getInstance];
	[[progress getTotalProgressBar] incrementBy:increment];
	[[progress getFileProgressBar] incrementBy:increment];
	
}

@implementation FileTransfer

- (void) mcast:(NSOpenPanel *)panel {
	NSAutoreleasePool *pool = [[NSAutoreleasePool allocWithZone:NULL] init];
	NSString *filename=[[panel filenames] objectAtIndex:0];
	//NSString *remotename=[NSString stringWithFormat:@"/%@",[[filename componentsSeparatedByString:@"/"] lastObject]];
	NSString *remotename=[[filename componentsSeparatedByString:@"/"] lastObject];
	JavaVM *jvm=get_jvm();
	JNIEnv *env;
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
	
	// Load progress window and prep the bars
	[NSBundle loadNibNamed:@"Progress" owner:self];	
	Progress *progress=[Progress getInstance];
	[[progress getPanel] makeKeyAndOrderFront:self];
	NSFileManager *fileManager = [NSFileManager defaultManager];
	NSDictionary *fileAttributes = [fileManager fileAttributesAtPath:filename traverseLink:YES];
	jlong totalSize=[[fileAttributes objectForKey:NSFileSize] longLongValue];
	[[progress getTotalProgressBar] setMaxValue:totalSize];
	[[progress getFileProgressBar] setMaxValue:totalSize];
	[[progress getTotalProgressBar] setMinValue:0];
	[[progress getFileProgressBar] setMinValue:0];
	[[progress getFileLabel] setTitleWithMnemonic:[[NSString alloc] initWithString:filename]];
	
	// Create progress listener class, link updates to updateProgress
	jclass progress_cls=(*env)->FindClass(env,"org/chungles/ui/cocoa/UploadProgress");
	jmethodID mid=(*env)->GetMethodID(env,progress_cls,"<init>","()V");
	jobject progress_inst=(*env)->NewObject(env,progress_cls,mid);
	JNINativeMethod nm;
	nm.name="update";
	nm.signature="(J)V";
	nm.fnPtr=updateProgress;
	(*env)->RegisterNatives(env,progress_cls,&nm,1);
	
	jclass cls=(*env)->FindClass(env,"org/chungles/core/mServer");
	mid=(*env)->GetMethodID(env,cls,"<init>","(Ljava/lang/String;Ljava/lang/String;Lorg/chungles/core/SendProgressListener;)V");
	(*env)->NewObject(env,cls,mid,(*env)->NewStringUTF(env,[filename UTF8String]), (*env)->NewStringUTF(env,[remotename UTF8String]), progress_inst);
	
	[[progress getPanel] close];
	
	(*env)->UnregisterNatives(env,progress_cls);
	(*jvm)->DetachCurrentThread(jvm);
	[pool release];
}


- (void) download:(NSDictionary *)dict {
	NSAutoreleasePool *pool = [[NSAutoreleasePool allocWithZone:NULL] init];
	OutlineItem *item=[dict objectForKey:@"item"];
	const char *file=[[dict objectForKey:@"file"] UTF8String];
	JavaVM *jvm=get_jvm();
	JNIEnv *env;
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
	
	// Spawn new client
	jclass client_cls=(*env)->FindClass(env,"org/chungles/core/Client");
	jmethodID mid=(*env)->GetMethodID(env,client_cls,"<init>","(Ljava/lang/String;)V");
	jobject client_inst=(*env)->NewObject(env,client_cls,mid,(*env)->NewStringUTF(env, [item getIP]));
	
	// Ask client to create list of files to retrieve
	mid=(*env)->GetMethodID(env,client_cls,"recurseFiles","(Ljava/lang/String;)Lorg/chungles/core/FileList;");
	jobject filelist=(*env)->CallObjectMethod(env, client_inst, mid, (*env)->NewStringUTF(env, [item getRelativePath]));
	jclass filelist_cls=(*env)->GetObjectClass(env,filelist);
	
	// Load progress window and prep the bars
	[NSBundle loadNibNamed:@"Progress" owner:self];	
	Progress *progress=[Progress getInstance];
	[[progress getPanel] makeKeyAndOrderFront:self];
	mid=(*env)->GetStaticMethodID(env,filelist_cls,"getTotalSize","()J");
	jlong totalSize=(*env)->CallStaticLongMethod(env,filelist_cls,mid);
	[[progress getTotalProgressBar] setMaxValue:totalSize];
	[[progress getTotalProgressBar] setMinValue:0];
	
	// Create progress listener class, link updates to updateProgress
	jclass progress_cls=(*env)->FindClass(env,"org/chungles/ui/cocoa/DownloadProgress");
	mid=(*env)->GetMethodID(env,progress_cls,"<init>","()V");
	jobject progress_inst=(*env)->NewObject(env,progress_cls,mid);
	JNINativeMethod nm;
	nm.name="update";
	nm.signature="(J)V";
	nm.fnPtr=updateProgress;
	(*env)->RegisterNatives(env,progress_cls,&nm,1);

	// Generate offset for substringing paths to produce outputfile (needs to be fixed in core soon)
	mid=(*env)->GetMethodID(env,filelist_cls, "getRemotePath", "()Ljava/lang/String;");
	jobject remotepath=(*env)->CallObjectMethod(env, filelist, mid);
	jclass cls=(*env)->FindClass(env, "java/lang/String");
	mid=(*env)->GetMethodID(env,cls,"lastIndexOf", "(I)I");
	int offset=(*env)->CallIntMethod(env,remotepath,mid, '/');
	
	// Send notification
	mid=(*env)->GetMethodID(env, client_cls, "sendNotification", "(I)V");
	(*env)->CallObjectMethod(env, client_inst, mid, GETTING_FILES);
	
	jmethodID remotepath_mid=(*env)->GetMethodID(env,filelist_cls,"getRemotePath","()Ljava/lang/String;");
	while ((*env)->CallObjectMethod(env,filelist,remotepath_mid)!=nil)
	{
		mid=(*env)->GetMethodID(env,filelist_cls,"getSize","()J");
		jlong filesize=(*env)->CallLongMethod(env,filelist,mid);
		[[progress getFileProgressBar] setMinValue:0];
		[[progress getFileProgressBar] setMaxValue:filesize];
		[[progress getFileProgressBar] setDoubleValue:0];
		mid=(*env)->GetMethodID(env,filelist_cls,"getRemotePath","()Ljava/lang/String;");
		jobject filepath_j=(*env)->CallObjectMethod(env,filelist,mid);
		const char *filepath=(*env)->GetStringUTFChars(env, filepath_j, NULL);
		[[progress getFileLabel] setTitleWithMnemonic:[[NSString alloc] initWithUTF8String:filepath]];
		(*env)->ReleaseStringUTFChars(env,filepath_j,filepath);
		
		// Generate filename to write to
		mid=(*env)->GetMethodID(env,filelist_cls, "getRemotePath", "()Ljava/lang/String;");
		remotepath=(*env)->CallObjectMethod(env, filelist, mid);
		mid=(*env)->GetMethodID(env,cls,"substring","(I)Ljava/lang/String;");
		jobject remotepath_trunc=(*env)->CallObjectMethod(env,remotepath,mid,offset);
		const char *remotepath_c=(*env)->GetStringUTFChars(env, remotepath_trunc, NULL);		
		char *outputfile;
		asprintf(&outputfile, "%s/%s", file, remotepath_c);		
		jobject outputfile_j=(*env)->NewStringUTF(env, outputfile);
		free(outputfile);
		(*env)->ReleaseStringUTFChars(env,remotepath_trunc, remotepath_c);
			
		mid=(*env)->GetMethodID(env,client_cls,"requestRetrieveFile","(Lorg/chungles/core/FileList;Ljava/lang/String;)Z");
		(*env)->CallBooleanMethod(env,client_inst,mid,filelist,outputfile_j);
		
		mid=(*env)->GetMethodID(env,client_cls,"retrieveFile", "(Ljava/lang/String;Lorg/chungles/core/FileList;Lorg/chungles/core/ReceiveProgressListener;)V");
		(*env)->CallObjectMethod(env,client_inst,mid,outputfile_j,filelist,progress_inst);
		
		jmethodID getnext_mid=(*env)->GetMethodID(env,filelist_cls,"getNext","()Lorg/chungles/core/FileList;");
		filelist=(*env)->CallObjectMethod(env,filelist,getnext_mid);
	}
	
	// Send notification
	mid=(*env)->GetMethodID(env, client_cls, "sendNotification", "(I)V");
	(*env)->CallObjectMethod(env, client_inst, mid, GOT_FILES);
	
	[[progress getPanel] close];
	
	(*env)->UnregisterNatives(env,progress_cls);
	mid=(*env)->GetMethodID(env,client_cls,"close","()V");
	(*env)->CallObjectMethod(env,client_inst,mid);
	[pool release];
	(*jvm)->DetachCurrentThread(jvm);
}

- (void)upload:(NSDictionary *)dict {
	NSAutoreleasePool *pool = [[NSAutoreleasePool allocWithZone:NULL] init];
	//OutlineItem *item=[[Outline getInstance] getSelection];
	OutlineItem *item=[dict objectForKey:@"item"];
	NSArray *files=[dict objectForKey:@"files"];
	JavaVM *jvm=get_jvm();
	JNIEnv *env;
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
	jclass cls=(*env)->FindClass(env, "java/lang/String");
	// copy nsstring array to java string array
	jobjectArray files_j=(*env)->NewObjectArray(env, [files count], cls, nil);
	int i;
	for (i=0; i<[files count]; i++)
	{
		const char *file=[[files objectAtIndex:i] UTF8String];
		fprintf(stderr, "%s\\0\n",file);
		(*env)->SetObjectArrayElement( env, files_j, i, (*env)->NewStringUTF(env, file) );
	}
	
	// create new client
	jclass client_cls=(*env)->FindClass(env,"org/chungles/core/Client");
	jmethodID mid=(*env)->GetMethodID(env,client_cls,"<init>","(Ljava/lang/String;)V");
	jobject client_inst=(*env)->NewObject(env,client_cls,mid,(*env)->NewStringUTF(env, [item getIP]));
	
	cls=(*env)->FindClass(env,"org/chungles/core/FileList");
	mid=(*env)->GetStaticMethodID(env,cls,"recurseFiles","([Ljava/lang/String;)Lorg/chungles/core/FileList;");
	jobject filelist=(*env)->CallStaticObjectMethod(env,cls,mid, files_j);
	jmethodID getnext_mid=(*env)->GetMethodID(env,cls,"getNext","()Lorg/chungles/core/FileList;");
	
	// Load progress window and prep the bars
	[NSBundle loadNibNamed:@"Progress" owner:self];	
	Progress *progress=[Progress getInstance];
	[[progress getPanel] makeKeyAndOrderFront:self];
	mid=(*env)->GetStaticMethodID(env,cls,"getTotalSize","()J");
	jlong totalSize=(*env)->CallStaticLongMethod(env,cls,mid);
	[[progress getTotalProgressBar] setMaxValue:totalSize];
	[[progress getTotalProgressBar] setMinValue:0];
	
	// Create progress listener class, link updates to updateProgress
	jclass progress_cls=(*env)->FindClass(env,"org/chungles/ui/cocoa/UploadProgress");
	mid=(*env)->GetMethodID(env,progress_cls,"<init>","()V");
	jobject progress_inst=(*env)->NewObject(env,progress_cls,mid);
	JNINativeMethod nm;
	nm.name="update";
	nm.signature="(J)V";
	nm.fnPtr=updateProgress;
	(*env)->RegisterNatives(env,progress_cls,&nm,1);

	// Send notification
	mid=(*env)->GetMethodID(env,client_cls, "sendNotification", "(I)V");
	(*env)->CallObjectMethod(env, client_inst, mid, SENDING_FILES);
	
	// Traverse file list sending files
	while (filelist!=nil)
	{
		mid=(*env)->GetMethodID(env,cls,"getSize","()J");
		jlong filesize=(*env)->CallLongMethod(env,filelist,mid);
		[[progress getFileProgressBar] setMinValue:0];
		[[progress getFileProgressBar] setMaxValue:filesize];
		[[progress getFileProgressBar] setDoubleValue:0];
		mid=(*env)->GetMethodID(env,cls,"getLocalPath","()Ljava/lang/String;");
		jobject filepath_j=(*env)->CallObjectMethod(env,filelist,mid);
		const char *filepath=(*env)->GetStringUTFChars(env, filepath_j, NULL);
		[[progress getFileLabel] setTitleWithMnemonic:[[NSString alloc] initWithUTF8String:filepath]];
		(*env)->ReleaseStringUTFChars(env,filepath_j,filepath);
		
		// Request to send file
		mid=(*env)->GetMethodID(env,client_cls,"requestFileSend","(Ljava/lang/String;Lorg/chungles/core/FileList;)Z");
		(*env)->CallObjectMethod(env,client_inst,mid,(*env)->NewStringUTF(env, [item getRelativePath]),filelist);
	
		// Send file
		mid=(*env)->GetMethodID(env,client_cls,"sendFile","(Lorg/chungles/core/FileList;Lorg/chungles/core/SendProgressListener;)V");
		(*env)->CallObjectMethod(env,client_inst,mid,filelist,progress_inst);
		
		filelist=(*env)->CallObjectMethod(env,filelist,getnext_mid);
	}
	// send notification
	mid=(*env)->GetMethodID(env, client_cls, "sendNotification", "(I)V");
	(*env)->CallObjectMethod(env, client_inst, mid, SENT_FILES);
	
	[[progress getPanel] close];
	// Cleanup
	mid=(*env)->GetMethodID(env,client_cls,"close","()V");
	(*env)->CallObjectMethod(env,client_inst,mid);
	(*env)->UnregisterNatives(env,progress_cls);
	[pool release];
	(*jvm)->DetachCurrentThread(jvm);
}


@end
