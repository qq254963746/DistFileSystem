/**
*/

package distfilelisting;

import java.io.File;
import java.util.Vector;

import distconfig.DistConfig;
import distnodelisting.NodeSearchTable;



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
	
	public FileObject set_file (FileObject fo) {
		String fileName = fo.getName();
		
		for(int index = 0; index < this.size(); index++) {
			if (this.get(index).getName().equals(fileName)) {
				FileObject prevObj = this.get(index);
				this.set(index, fo);
				return prevObj;
			}
		}
		
		this.add(fo);
		return fo;
	}
	
	public Vector<FileObject> get_filesBetween(int lowerHash, int upperHash) {
		Vector<FileObject> retval = new Vector<FileObject>();
		
		// Loop through every file in this vector and check if it is between
		// the two hashes, or equal to the upper hash
		for (int index = 0; index < this.size(); index++) {
			if (NodeSearchTable.is_between(
					this.get(index).get_hash(), 
					lowerHash, upperHash) ||
					this.get(index).get_hash() == upperHash) {
				retval.add(this.get(index));
			}
		}
		
		return retval;
	}
	
	
}