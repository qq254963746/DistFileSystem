/**
*/

package distfilelisting;

import java.util.Vector;



class FullPathListing extends Vector<Object> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FullPathListing instance = null;
	
	private FullPathListing () {
		
	}
	
	public FullPathListing get_Instance() {
		if (instance == null) {
			instance = new FullPathListing();
		}
		return instance;
	}
}