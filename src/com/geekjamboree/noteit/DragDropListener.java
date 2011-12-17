package com.geekjamboree.noteit;

public interface DragDropListener {
	/*
	 *  @param dragSource: 	The item that is being dragged
	 *  @param dropTarget:	THe item over which @dragSource is being dragged
	 */
	void onDrag(final int dragSource, final int dropTarget);
	void onDrop(final int dragSource, final int dropTarget);
}
