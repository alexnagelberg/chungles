#include "javalibraryloader.h"
#include "statelessapp.h"
#include <dlfcn.h>

JNIEXPORT jobject JNICALL
Java_org_chungles_frameworks_stateless_StatelessNativeLibraryLoader_addApplication
(JNIEnv *env, jobject obj, jstring library)
{
	const char *libname=(*env)->GetStringUTFChars(env, library, NULL);
	jint handle=(jint)dlopen(libname, RTLD_LAZY);	
	
	char *error=dlerror();
	if (error)
		fprintf(stderr, error);
	
	// Creates new instance of StatelessNativeApplication
	jclass class=(*env)->FindClass(env, "org/chungles/frameworks/stateless/StatelessNativeApplication");
	jmethodID id=(*env)->GetMethodID(env, class, "<init>", "(I)V");
	jobject inst=(*env)->NewObject(env, class, id, handle);
	
	// register native methods
	JNINativeMethod method[5]=
	{
		{"ngetAppID", "()Ljava/lang/String;", (void *)Java_org_chungles_frameworks_stateless_StatelessNativeApplication_ngetAppID},
		{"ngetSystemCommand", "()Ljava/lang/String;", (void *)Java_org_chungles_frameworks_stateless_StatelessNativeApplication_ngetSystemCommand},
		{"nfreeze", "()Lorg/chungles/frameworks/stateless/NativeState;", (void *)Java_org_chungles_frameworks_stateless_StatelessNativeApplication_nfreeze},
		{"nthaw", "(Lorg/chungles/frameworks/stateless/NativeState;)V", (void *)Java_org_chungles_frameworks_stateless_StatelessNativeApplication_nthaw},
		{"nhasEnoughMemory", "(J)Z", (void *)Java_org_chungles_frameworks_stateless_StatelessNativeApplication_nhasEnoughMemory}
	};
	(*env)->RegisterNatives(env, class, method, 5);
		
	return inst;
}

JNIEXPORT jstring JNICALL
Java_org_chungles_frameworks_stateless_StatelessNativeApplication_ngetAppID
(JNIEnv *env, jobject obj)
{	
	// retrieve dl handle from instance
	jclass class=(*env)->GetObjectClass(env, obj);
	jfieldID id=(*env)->GetFieldID(env, class, "dlhandle", "I");
	void *lib=(void *)((*env)->GetIntField(env, obj, id));

	// call get_app_id and return output	
	char *(*get_id)()=(char * (*)())dlsym(lib, "get_app_id");
	return (*env)->NewStringUTF(env, (*get_id)());
}

JNIEXPORT jstring JNICALL
Java_org_chungles_frameworks_stateless_StatelessNativeApplication_ngetSystemCommand
(JNIEnv *env, jobject obj)
{
	// retrieve dl handle from instance
	jclass class=(*env)->GetObjectClass(env, obj);
	jfieldID id=(*env)->GetFieldID(env, class, "dlhandle", "I");
	void *lib=(void *)((*env)->GetIntField(env, obj, id));
	
	// call get_system_command and return output
	char *(*get_command)()=(char * (*)())dlsym(lib, "get_system_command");
	return (*env)->NewStringUTF(env, (*get_command)());
}

JNIEXPORT jobject JNICALL
Java_org_chungles_frameworks_stateless_StatelessNativeApplication_nfreeze
(JNIEnv *env, jobject obj)
{
	// retrieve dl handle from instance
	jclass class=(*env)->GetObjectClass(env, obj);
	jfieldID id=(*env)->GetFieldID(env, class, "dlhandle", "I");
	void *lib=(void *)((*env)->GetIntField(env, obj, id));
	
	// call freeze function and retrieve state
	state *(*freeze_func)()=(state * (*)())dlsym(lib, "freeze");
	state st=*(*freeze_func)();
	
	// convert c buffer to byte array
	jbyteArray buf=(*env)->NewByteArray(env, st.length);
	(*env)->SetByteArrayRegion(env, buf, 0, st.length, st.buf);
	
	// create new instance of NativeState using retrieved state
	class=(*env)->FindClass(env, "org/chungles/frameworks/stateless/NativeState");
	jmethodID methid=(*env)->GetMethodID(env, class, "<init>", "(J[B)V");
	jobject inst=(*env)->NewObject(env, class, methid, st.required_mem, buf);
	
	return inst;
}

JNIEXPORT void JNICALL
Java_org_chungles_frameworks_stateless_StatelessNativeApplication_nthaw
(JNIEnv *env, jobject obj, jobject stateobj)
{
	// retrieve dl handle from instance
	jclass class=(*env)->GetObjectClass(env, obj);
	jfieldID id=(*env)->GetFieldID(env, class, "dlhandle", "I");
	void *lib=(void *)((*env)->GetIntField(env, obj, id));
	
	class=(*env)->GetObjectClass(env, stateobj);
	id=(*env)->GetFieldID(env, class, "memory", "J");
	long reqmem=(*env)->GetLongField(env, stateobj, id);
	
	jmethodID methid=(*env)->GetMethodID(env, class, "getBuffer", "()[B");
	jbyteArray byteBuf=(*env)->CallObjectMethod(env, stateobj, methid);	
	long length=(*env)->GetArrayLength(env, byteBuf);
	char *buffer=malloc(sizeof(char)*length);
	(*env)->GetByteArrayRegion(env, byteBuf, 0, length, buffer);
		
	state st={buffer, length, reqmem};	
	void (*thaw_func)(state *)=(void (*)(state *))dlsym(lib, "thaw");
	(*thaw_func)(&st);

}
	
JNIEXPORT jboolean JNICALL
Java_org_chungles_frameworks_stateless_StatelessNativeApplication_nhasEnoughMemory
(JNIEnv *env, jobject obj, jlong memory)
{
	// retrieve dl handle from instance
	jclass class=(*env)->GetObjectClass(env, obj);
	jfieldID id=(*env)->GetFieldID(env, class, "dlhandle", "I");
	void *lib=(void *)((*env)->GetIntField(env, obj, id));
	
	int ((*memory_func)(long))=(int (*)(long))dlsym(lib, "has_enough_memory");
	return ((*memory_func)(memory))?JNI_TRUE:JNI_FALSE;
}
	