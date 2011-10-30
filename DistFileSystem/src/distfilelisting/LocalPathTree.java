/**
*/

package distfilelisting;

import java.util.Enumeration;

import javax.swing.tree.*;

import distconfig.DistConfig;


public class LocalPathTree {
	
	private static LocalPathTree instance = null;
	
	private DefaultMutableTreeNode rootNode = null;
	
	private LocalPathTree() {
		this.rootNode = new DefaultMutableTreeNode("/");		
	}
	
	public static LocalPathTree get_Instance () {
		if (instance == null) {
			instance = new LocalPathTree();
		}
		return instance;
	}

	// Path to new directory
	// directory name
	// 
	public String[] make_Directory (String[] args) {
		DistConfig distConfig = DistConfig.get_Instance();
		
		String[] retval = {"1", "Success"};
		
		String parentPath = null;
		String directoryName = null;
		String permissions = null;
		
		// verify that there are at least two parts to args 
		if (args.length < 2) {
			retval[0] = "0";
			retval[1] = "Args must contain the path to the new" +
					"directory and the name of the new directory";
		}
		
		// Set all the necessary parameters 
		for (int index = 0; index < args.length; index++) {
			switch (index) {
			case 0:
				parentPath = args[index];
				break;
			case 1:
				directoryName = args[index];
				break;
			case 2:
				permissions = args[index];
				break;
			default:
				break;
			}
		}
		
		// Navigate to the location given in the tree
		String[] pathParts = parentPath.split("/");
		
		return retval;
		
		
	}
}