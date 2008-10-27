#import "OutlineItem.h"
#import "Outline.h"

static NSMutableArray *rootchildren=nil;
static int rootcount=0;

@implementation OutlineItem

- (id)initWithStr:(NSString *)str {
	children=nil;
	name=str;
	return self;
}

+ (int)numRoots {
	return rootcount;
}

+ (void)delRootItem:(NSString *)name ip:(NSString *)IP {
	NSAutoreleasePool *pool=[[NSAutoreleasePool allocWithZone:NULL] init];
	int i;
	for (i=0;i<rootcount; i++)
	{
		if ([[[rootchildren objectAtIndex:i] getName] isEqualToString:name])
		{
			[rootchildren removeObjectAtIndex:i];
			i=rootcount;
			rootcount--;
			[[Outline getInstance] update];
		}
	}
	
	[pool release];
}

+ (void)addRootItem:(NSString *)name ip:(NSString *)IP {
	NSAutoreleasePool *pool=[[NSAutoreleasePool allocWithZone:NULL] init];
	if (rootchildren==nil)
	{
		rootchildren=[[NSMutableArray alloc] init];
	}
	
	OutlineItem *item;	
	item=[OutlineItem alloc];
	item->IP=IP;
	item->parent=nil;
	item->children=nil;
	item->name=name;
	item->expandable=TRUE;
	item->path=[NSString stringWithFormat:@"/%@/",item->name];
	[item->path retain];
	item->relativePath=@"/";
	[item->relativePath retain];
	[rootchildren addObject:item];	
	rootcount++;
	
	
	Outline *outline=[Outline getInstance];
	[outline update];
	[pool release];
}

+ (OutlineItem *)rootItem:(NSInteger)index {
	if (rootchildren==nil)
		return nil;
	else
		return [rootchildren objectAtIndex:index];
}

- (int)numberOfChildren {
	if (children==nil)
	{
		return 0;
	}
	else
		return [children count];
}

- (NSString *)getName {
	return name;
}

- (const char *)getPath {
	return [path UTF8String];
}

- (BOOL)isExpandable {
	return expandable;
}

- (OutlineItem *)getChild:(NSInteger)index {
	if (children==nil)
		return nil;
	else
		return [children objectAtIndex:index];
}

- (void) addChild:(NSString *)childname isExpandable:(BOOL)isExpandable {
	if (children==nil)
	{
		children=[[NSMutableArray alloc] init];		
	}
	
	OutlineItem *item;	
	item=[OutlineItem alloc];
	item->IP=IP;
	item->parent=self;
	item->children=nil;
	item->name=[childname retain];
	item->expandable=isExpandable;
	item->path=[NSString stringWithFormat:@"%@%@/",path,item->name];
	[item->path retain];	
	item->relativePath=[NSString stringWithFormat:@"%@%@/",relativePath,item->name];
	[item->relativePath retain];
	[children addObject:item];
}

- (void) clearChildren {
	if (children==nil)
		return;
	
	[children release];
	children=nil;
}

- (BOOL) isRootItem {
	return [rootchildren containsObject:self];
}

- (OutlineItem *)getParent {
	return parent;
}

- (const char *)getIP {
	return [IP UTF8String];
}

- (const char *)getRelativePath {
	return [relativePath UTF8String];
}

- (void) removeItem {
	[[self getParent]->children removeObject:self];
	Outline *outline=[Outline getInstance];
	[outline update];
}

- (void) updateName:(NSString *)newName {
	[name release];
	name=[newName retain];
	NSArray *relativePathSplit=[relativePath componentsSeparatedByString:@"/"];
	NSMutableArray *mut=[NSMutableArray arrayWithCapacity:[relativePathSplit count]];
	[mut addObjectsFromArray:relativePathSplit];
	[mut replaceObjectAtIndex:[mut count]-1 withObject:newName];
	[relativePath release];
	relativePath=[[mut componentsJoinedByString:@"/"] retain];
	
	NSArray *pathSplit=[path componentsSeparatedByString:@"/"];
	mut=[NSMutableArray arrayWithCapacity:[pathSplit count]];
	[mut addObjectsFromArray:pathSplit];
	[mut replaceObjectAtIndex:[mut count]-1 withObject:newName];
	[path release];
	path=[[mut componentsJoinedByString:@"/"] retain];
}

@end
