/**
*/

package distfilelisting;

import java.util.Vector;



class LocalPathListing extends Vector<Object> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LocalPathListing instance = null;
	
	private LocalPathListing () {
		
	}
	
	public LocalPathListing get_Instance() {
		if (instance == null) {
			instance = new LocalPathListing();
		}
		return instance;
	}
}