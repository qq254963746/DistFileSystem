/**
*/

package distfilelisting;

import javax.swing.tree.*;



class FullPathTree {
	
	private FullPathTree instance = null;
	private DefaultMutableTreeNode rootNode = null;
	
	private FullPathTree () {
		this.rootNode = new DefaultMutableTreeNode("/");
	}
	
	public FullPathTree get_Instance () {
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