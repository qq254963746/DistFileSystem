/**
*/

package distfilelisting;

import java.io.Serializable;
import java.util.Vector;



public class FullPathList extends Vector<Object> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static FullPathList instance = null;
	
	private FullPathList () {
		
	}
	
	public static FullPathList get_Instance() {
		if (instance == null) {
			instance = new FullPathList();
		}
		return instance;
	}
}