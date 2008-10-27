#import "MainApp.h"
#import "Toolbar.h"
#import "Outline.h"
#import "OutlineItem.h"
#import "Preferences.h"
#import <jni.h>

static jobject java_instance=nil;
static JNIEnv *jvm_env;
static NSThread *jvm_thread=nil;
static JavaVM *jvm;

JavaVM *get_jvm()
{
	return jvm;
}

jobject get_class_instance()
{
	return java_instance;
}

JNIEXPORT void JNICALL Java_org_chungles_ui_cocoa_Main_cocoa_openprefs (JNIEnv *env, jobject obj)
{
	[Preferences openPreferences];
}

JNIEXPORT void JNICALL Java_org_chungles_ui_cocoa_Main_cocoa_init (JNIEnv *env, jobject obj, jobject class_inst)
{
	java_instance=(*env)->NewGlobalRef(env,class_inst);
	jclass cls=(*env)->GetObjectClass(env, class_inst);
	
	JNINativeMethod nm1;
	nm1.name="cocoa_addNode";
	nm1.signature="(Ljava/lang/String;Ljava/lang/String;)V";
	nm1.fnPtr=Java_org_chungles_ui_cocoa_Main_cocoa_addNode;
	(*env)->RegisterNatives(env,cls,&nm1,1);
	
	JNINativeMethod nm2;
	nm2.name="cocoa_removeNode";
	nm2.signature="(Ljava/lang/String;Ljava/lang/String;)V";
	nm2.fnPtr=Java_org_chungles_ui_cocoa_Main_cocoa_removeNode;
	(*env)->RegisterNatives(env,cls,&nm2,1);	
	
	JNINativeMethod nm3;
	nm3.name="cocoa_openprefs";
	nm3.signature="()V";
	nm3.fnPtr=Java_org_chungles_ui_cocoa_Main_cocoa_openprefs;
	(*env)->RegisterNatives(env,cls,&nm3,1);
}

JNIEXPORT void JNICALL Java_org_chungles_ui_cocoa_Main_cocoa_addNode (JNIEnv *env, jobject obj, jstring IP, jstring compname)
{
	const char *compstr = (*env)->GetStringUTFChars(env, compname, NULL);
	NSString *name=[[NSString alloc] initWithUTF8String:compstr];
	const char *ipstr=(*env)->GetStringUTFChars(env, IP, NULL);
	NSString *nsip=[[NSString alloc] initWithUTF8String:ipstr];
	[OutlineItem addRootItem:name ip:nsip];
	(*env)->ReleaseStringUTFChars(env,IP,ipstr);
	(*env)->ReleaseStringUTFChars(env,compname,compstr);
}

JNIEXPORT void JNICALL Java_org_chungles_ui_cocoa_Main_cocoa_removeNode (JNIEnv *env, jobject obj, jstring IP, jstring compname)
{
	const char *compstr = (*env)->GetStringUTFChars(env, compname, NULL);
	NSString *name=[[NSString alloc] initWithUTF8String:compstr];
	const char *ipstr=(*env)->GetStringUTFChars(env, IP, NULL);
	NSString *nsip=[[NSString alloc] initWithUTF8String:ipstr];
	[OutlineItem delRootItem:name ip:nsip];
	(*env)->ReleaseStringUTFChars(env,IP,ipstr);
	(*env)->ReleaseStringUTFChars(env,compname,compstr);
}

void destroy_vm(JNIEnv *env, JavaVM *jvm)
{
    if ((*env)->ExceptionOccurred(env)) {
         (*env)->ExceptionDescribe(env);
     }
	 (*env)->DeleteGlobalRef(env,java_instance);
     (*jvm)->DestroyJavaVM(jvm);
}

void startVM()
{
	JNIEnv *env;
	jint res;
	JavaVMInitArgs vm_args;
	JavaVMOption options[1];
	NSBundle *bundle=[NSBundle mainBundle];
	options[0].optionString=(char *)[[NSString stringWithFormat:@"-Djava.class.path=%@:%@:%@",[bundle pathForResource:@"chungles-cocoa.jar" ofType:nil],
									  [bundle pathForResource:@"jmdns.jar" ofType:nil],[bundle pathForResource:@"chungles.jar" ofType:nil]] UTF8String];
	vm_args.version = JNI_VERSION_1_4;
	vm_args.options = options;
	vm_args.nOptions = 1;
	vm_args.ignoreUnrecognized = JNI_FALSE;
	
	/* Create the Java VM */
	res = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
	jvm_env=env;
	if (res < 0) {
		fprintf(stderr, "Can't create Java VM\n");
		exit(1);
	}
}

void startupJava()
{	
	JNIEnv *env;
	jclass cls;
	jmethodID mid;
	jstring jstr;
	jclass stringClass;
	jobjectArray args;
	
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);

	cls = (*env)->FindClass(env, "org/chungles/ui/cocoa/Main");
 
	JNINativeMethod nm;
	nm.name="cocoa_init";
	nm.signature="(Ljava/lang/Object;)V";
	nm.fnPtr=Java_org_chungles_ui_cocoa_Main_cocoa_init;
	(*env)->RegisterNatives(env,cls,&nm,1);			
	
	cls = (*env)->FindClass(env, "org/chungles/application/Main");
	mid = (*env)->GetStaticMethodID(env, cls, "main","([Ljava/lang/String;)V");
	jstr = (*env)->NewStringUTF(env, "");
	stringClass = (*env)->FindClass(env, "java/lang/String");
	args = (*env)->NewObjectArray(env, 1, stringClass, jstr);
	(*env)->CallStaticVoidMethod(env, cls, mid, args);
}


@implementation MainApp

- (void) createConfig {
	JNIEnv *env=jvm_env;
	NSBundle *bundle=[NSBundle mainBundle];
	
	jclass cls=(*env)->FindClass(env,"org/chungles/plugin/PluginAction");
	jmethodID mid=(*env)->GetStaticMethodID(env,cls,"PeekInJAR","(Ljava/lang/String;)Lorg/chungles/plugin/PluginInfo;");
	jobject cocoa_plugin=(*env)->CallStaticObjectMethod(env,cls,mid,(*env)->NewStringUTF(env,[[bundle pathForResource:@"chungles-cocoa.jar" ofType:nil] UTF8String]));
	
	cls=(*env)->FindClass(env,"org/chungles/plugin/PluginInfo");
	mid=(*env)->GetMethodID(env,cls,"setEnabled","(Z)V");
	(*env)->CallVoidMethod(env,cocoa_plugin,mid,YES);
	
	cls=(*env)->FindClass(env,"org/chungles/core/Configuration");
	jfieldID fid=(*env)->GetStaticFieldID(env,cls,"UIplugins","Ljava/util/LinkedList;");
	jobject uiplugins=(*env)->GetStaticObjectField(env,cls,fid);
	
	mid=(*env)->GetStaticMethodID(env,cls,"init","()V");
	(*env)->CallStaticVoidMethod(env,cls,mid);
	
	cls=(*env)->GetObjectClass(env,uiplugins);
	mid=(*env)->GetMethodID(env,cls,"add","(Ljava/lang/Object;)Z");
	(*env)->CallBooleanMethod(env,uiplugins,mid,cocoa_plugin);
	
	cls=(*env)->FindClass(env,"java/io/File");
	mid=(*env)->GetMethodID(env,cls,"<init>","(Ljava/lang/String;)V");
	jobject file_obj=(*env)->NewObject(env,cls,mid,(*env)->NewStringUTF(env,[[NSHomeDirectory() stringByAppendingPathComponent:@".chungles/config.xml"] UTF8String]));
	
	cls=(*env)->FindClass(env,"org/chungles/core/ConfigurationParser");
	mid=(*env)->GetStaticMethodID(env,cls,"createConfig", "(Ljava/io/File;)V");
	(*env)->CallStaticVoidMethod(env,cls,mid,file_obj);
	
	mid=(*env)->GetStaticMethodID(env,cls,"saveConfig", "()V");
	(*env)->CallStaticVoidMethod(env,cls,mid);
	
	// Remove from list to leave empty conf for startup
	cls=(*env)->GetObjectClass(env,uiplugins);
	mid=(*env)->GetMethodID(env,cls,"remove","(Ljava/lang/Object;)Z");
	(*env)->CallObjectMethod(env,uiplugins,mid,cocoa_plugin);
}

- (void) updateConfig:(NSString *)pref_loc {
	// If app location has moved, update plugin attribute in conf
	JNIEnv *env=jvm_env;
	NSBundle *bundle=[NSBundle mainBundle];
	
	jclass cls=(*env)->FindClass(env,"org/chungles/ui/cocoa/Main");
	jmethodID mid=(*env)->GetStaticMethodID(env,cls,"updateCocoaPath","(Ljava/lang/String;Ljava/lang/String;)V");
	(*env)->CallStaticVoidMethod(env,cls,mid,(*env)->NewStringUTF(env,[pref_loc UTF8String]),
								 (*env)->NewStringUTF(env,[[[bundle bundlePath] stringByAppendingPathComponent:@"Contents/Resources/chungles-cocoa.jar"] UTF8String]));
}

- (IBAction)applicationDidFinishLaunching:(id)sender {
	NSBundle *bundle=[NSBundle mainBundle];
	NSString *pref_loc=(NSString *)CFPreferencesCopyAppValue((CFStringRef)@"location", kCFPreferencesCurrentApplication);
	
	startVM();
	
	// If no conf exists, make one
	if (![[NSFileManager defaultManager] fileExistsAtPath:[NSHomeDirectory() stringByAppendingPathComponent:@".chungles/config.xml"]])
		[self createConfig];
	
	// Check mac prefs, see if location in prefs matches location of this app. If not, update chungles config
	if (pref_loc==nil)
	{
		CFPreferencesSetAppValue((CFStringRef)@"location", [bundle bundlePath],kCFPreferencesCurrentApplication);
		CFPreferencesAppSynchronize(kCFPreferencesCurrentApplication);
	}
	else if (![pref_loc isEqual:[bundle bundlePath]])
		[self updateConfig:[pref_loc stringByAppendingPathComponent:@"Contents/Resources/chungles-cocoa.jar"]];
	
	// Detach a new thread, and in that thread start chungles core
    [NSThread detachNewThreadSelector:@selector(startupJava:) toTarget:self withObject:nil];
	[[[Outline getInstance]	getView] registerForDraggedTypes:[NSArray arrayWithObjects:NSFilenamesPboardType,nil]];
}

- (void)applicationWillTerminate:(id)sender {
	if (java_instance!=nil)
	{
		// JNI call to shutdown		
		JNIEnv *env;
		(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
		jclass cls=(*env)->GetObjectClass(env, java_instance);
		jmethodID mid=(*env)->GetMethodID(env, cls, "shutdown", "()V");
		(*env)->CallObjectMethod(env,java_instance,mid);
		destroy_vm(env,jvm);		
	}
}

- (IBAction)openPreferences:(id)sender {
	[Preferences openPreferences];
}

- (void)startupJava:(id)userData {
	jvm_thread=[NSThread currentThread];
    // All new native threads (Cocoa and Java) need an autorelease pool.
    NSAutoreleasePool *pool = [[NSAutoreleasePool allocWithZone:NULL] init];

    // Startup the JVM (startupJava is a C function defined elsewhere)
    startupJava();

    [pool release];
}

@end
