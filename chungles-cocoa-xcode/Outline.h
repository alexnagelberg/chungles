#import "OutlineItem.h"
#import <Cocoa/Cocoa.h>
#import <Foundation/Foundation.h>

@interface Outline : NSObject {
	IBOutlet NSOutlineView *outline;
	OutlineItem *selection;
}

+ (Outline *)getInstance;
- (id)init;
- (void)addItem:(char *)name;
- (void)outlineViewSelectionDidChange:(NSNotification *)notification;
- (void)outlineViewItemDidExpand:(NSNotification *)notification;
- (void)update;
- (OutlineItem *)getSelection;
- (NSOutlineView *)getView;

// Data Source
- (NSInteger)outlineView:(NSOutlineView *)outlineView numberOfChildrenOfItem:(id)item;
- (BOOL)outlineView:(NSOutlineView *)outlineView isItemExpandable:(id)item;
- (id)outlineView:(NSOutlineView *)outlineView child:(NSInteger)index ofItem:(id)item;
- (id)outlineView:(NSOutlineView *)outlineView objectValueForTableColumn:(NSTableColumn *)tableColumn byItem:(id)item;
- (void)controlTextDidEndEditing:(NSNotification *)aNotification;
- (BOOL)outlineView:(NSOutlineView *)outlineView acceptDrop:(id < NSDraggingInfo >)info item:(OutlineItem *)item childIndex:(NSInteger)index;
- (NSDragOperation)outlineView:(NSOutlineView *)outlineView validateDrop:(id < NSDraggingInfo >)info proposedItem:(OutlineItem *)item proposedChildIndex:(NSInteger)index;

@end
