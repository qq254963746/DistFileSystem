/**
*/

package distfilelisting;

import java.util.Vector;



public class LocalPathList extends Vector<Object> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static LocalPathList instance = null;
	
	private LocalPathList () {
		
	}
	
	public static LocalPathList get_Instance() {
		if (instance == null) {
			instance = new LocalPathList();
		}
		return instance;
	}
}