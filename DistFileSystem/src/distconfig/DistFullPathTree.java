/**
*/

package distconfig;

import javax.swing.tree.*;



class DistFullPathTree {
	
	private DistFullPathTree instance = null;
	private DefaultMutableTreeNode rootNode = null;
	
	private DistFullPathTree () {
		this.rootNode = new DefaultMutableTreeNode("/");
	}
	
	public DistFullPathTree get_Instance () {
		if (instance == null) {
			instance = new DistFullPathTree();
		}
		return instance;
	}
	
}