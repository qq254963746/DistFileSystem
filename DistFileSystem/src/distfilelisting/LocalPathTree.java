/**
*/

package distfilelisting;

import javax.swing.tree.*;


class LocalPathTree {
	
	private LocalPathTree instance = null;
	
	private LocalPathTree() {
		
	}
	
	public LocalPathTree get_Instance () {
		if (instance == null) {
			instance = new LocalPathTree();
		}
		return instance;
	}
}