#import <Cocoa/Cocoa.h>
#import <jni.h>

@interface MainApp : NSObject {
	
}

- (IBAction)applicationDidFinishLaunching:(id)sender;
- (IBAction)applicationWillTerminate:(id)sender;
- (IBAction)openPreferences:(id)sender;
- (void)applicationWillTerminate:(id)sender;
- (void)startupJava:(id)userData;
@end

JNIEXPORT void JNICALL Java_org_chungles_ui_cocoa_Main_cocoa_openprefs (JNIEnv *env, jobject obj);
JNIEXPORT void JNICALL Java_org_chungles_ui_cocoa_Main_cocoa_init (JNIEnv *env, jobject obj, jobject class_inst);
JNIEXPORT void JNICALL Java_org_chungles_ui_cocoa_Main_cocoa_addNode (JNIEnv *env, jobject obj, jstring IP, jstring compname);
JNIEXPORT void JNICALL Java_org_chungles_ui_cocoa_Main_cocoa_removeNode (JNIEnv *env, jobject obj, jstring IP, jstring compname);
JavaVM *get_jvm();
jobject get_class_instance();
void destroy_vm(JNIEnv *env, JavaVM *jvm);
void startupJava();
