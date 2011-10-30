/**
*/

package distfilelisting;

import javax.swing.tree.*;



public class FullPathTree {
	
	private static FullPathTree instance = null;
	private DefaultMutableTreeNode rootNode = null;
	
	private FullPathTree () {
		this.rootNode = new DefaultMutableTreeNode("/");
	}
	
	public static FullPathTree get_Instance () {
		if (instance == null) {
			instance = new FullPathTree();
		}
		return instance;
	}
	
	public void make_Directory (String fullPath) {
		String path = null;
		String newDir = null;
		path = newDir;
		rootNode.add(new DefaultMutableTreeNode(path));
	}
	
}