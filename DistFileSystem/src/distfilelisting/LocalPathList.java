/**
*/

package distfilelisting;

import java.io.File;
import java.util.Vector;

import distconfig.DistConfig;



public class LocalPathList extends Vector<FileObject> {
	
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
	
	
	public String[] new_file (String filename, String permissions,
			String userName, String groupName, String authHash) {
		
		String[] retval = new String[2];
		
		DistConfig distConfig = DistConfig.get_Instance();
		File newFile = new File (distConfig.get_rootPath() + filename);
		
		if (newFile.exists()) {
			retval[0] = "1";
			retval[1] = filename + " already exists";
			return retval;
		}
		
		int[] perms = FileObject.get_permissionsFromString(permissions);
		FileObject fo = new FileObject(filename, false, perms[0],
				perms[1], perms[2], userName, groupName);
		this.add(fo);
		
		return retval;
	}
	
	public FileObject get_file (String file_name) {
		for (int index = 0; index < this.size(); index++) {
			FileObject currentObj = (FileObject)this.get(index);
			if (currentObj.getName().equals(file_name)) {
				return currentObj;
			}
		}
		return null;
	}
	
	public Vector<FileObject> get_filesBetween(int lowerHash, int upperHash) {
		Vector<FileObject> retval = new Vector<FileObject>();
		
		for(int index = 0; index < this.size(); index++) {
			if (this.get(index).get_hash() > lowerHash &&
					this.get(index).get_hash() < upperHash) {
				retval.add(this.get(index));
			}
		}
		
		return retval;
	}
	
	
}