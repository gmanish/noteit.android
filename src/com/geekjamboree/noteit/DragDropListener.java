package com.geekjamboree.noteit;

public interface DragDropListener {
	/*
	 *  @param dragSource: 	The item that is being dragged
	 *  @param dropTarget:	THe item over which @dragSource is being dragged
	 */
	void onDrag(int dragSource, int dropTarget);
	void onDrop(int dragSource, int dropTarget);
}
