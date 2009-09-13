#import "growl.h"

@implementation growl

JNIEXPORT void JNICALL Java_org_chungles_growl_Main_register (JNIEnv *env, jobject obj, jbyteArray imgarray)
{
  NSAutoreleasePool *pool = [[NSAutoreleasePool allocWithZone:NULL] init];
  jsize len=(*env)->GetArrayLength(env, imgarray);
  jbyte *data=malloc(len);
  (*env)->GetByteArrayRegion(env, imgarray, 0, len, data);
  NSData *image=[NSData dataWithBytes:data length:len];
  [growl regApp:image];
  [pool release];
}

JNIEXPORT void JNICALL Java_org_chungles_growl_Main_notify (JNIEnv *env, jobject obj, jstring name, jstring title, jstring description)
{
  NSString *namestr=[[NSString alloc] initWithUTF8String:(*env)->GetStringUTFChars(env, name, NULL)];
  NSString *titlestr=[[NSString alloc] initWithUTF8String:(*env)->GetStringUTFChars(env, title, NULL)];
  NSString *descriptionstr=[[NSString alloc] initWithUTF8String:(*env)->GetStringUTFChars(env, description, NULL)];
  
  [growl notifyGrowlOf:namestr title:titlestr description:descriptionstr];
}

+ (void) regApp:(NSData *)image
{
  NSAutoreleasePool *pool = [[NSAutoreleasePool allocWithZone:NULL] init];
  NSArray *notifications=[[NSArray alloc] initWithObjects:@"General",@"Error",nil];
  NSArray *keys=[[NSArray alloc] initWithObjects:GROWL_APP_NAME,GROWL_NOTIFICATIONS_ALL,GROWL_NOTIFICATIONS_DEFAULT,GROWL_APP_ICON,nil];
  NSArray *objs=[[NSArray alloc] initWithObjects:@"Chungles",notifications,notifications,image,nil];
  NSDictionary *regDict=[NSDictionary dictionaryWithObjects:objs forKeys:keys];
  NSDistributedNotificationCenter *theCenter=[NSDistributedNotificationCenter defaultCenter];
  [theCenter postNotificationName:GROWL_APP_REGISTRATION object:@"" userInfo:regDict deliverImmediately:YES];
  [pool release];
}

+ (void) notifyGrowlOf:(NSString *)notificationName title:(NSString *)title description:(NSString *)description
{
  NSAutoreleasePool *pool = [[NSAutoreleasePool allocWithZone:NULL] init];
  NSArray *keys=[[NSArray alloc] initWithObjects:GROWL_NOTIFICATION_NAME,GROWL_NOTIFICATION_TITLE,GROWL_NOTIFICATION_DESCRIPTION,GROWL_APP_NAME,nil];
  NSArray *objs=[[NSArray alloc] initWithObjects:notificationName,title,description,@"Chungles",nil];
  NSDictionary *noteDict=[NSDictionary dictionaryWithObjects:objs forKeys:keys];
  NSDistributedNotificationCenter *theCenter=[NSDistributedNotificationCenter defaultCenter];
  [theCenter postNotificationName:GROWL_NOTIFICATION object:@"" userInfo:noteDict deliverImmediately:YES];
  [pool release];
}
@end
