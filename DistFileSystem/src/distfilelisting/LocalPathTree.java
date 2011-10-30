/**
*/

package distfilelisting;

import java.util.Enumeration;
import javax.swing.tree.*;
import distconfig.UserManagement;


public class LocalPathTree {
	
	private static LocalPathTree instance = null;
	
	private DefaultMutableTreeNode rootNode = null;
	
	private LocalPathTree() {
		String owner = UserManagement.get_Instance().get_ownUserName();
		FileObject rootObj = new FileObject ("/", true, 7, 7, 7, owner, owner);
		this.rootNode = new DefaultMutableTreeNode(rootObj);		
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
	public String[] make_Directory (String fullPath, String permissions,
			String reqUser, String authHash) {
		
		String[] retval = {"1", "Success"};
		
		System.out.println("Creating " + fullPath);
		
		// Navigate to the location given in the tree
		String[] pathParts = fullPath.split("/");
		String[] parentPath = new String[pathParts.length-1];
		String newDirName = pathParts[pathParts.length-1];
		
		parentPath[0] = "/";
		for (int index = 1; index < pathParts.length-1; index++) {
			parentPath[index] = pathParts[index];
		}
		
		int pathDepth = 1;
		
		@SuppressWarnings("rawtypes")
		Enumeration nodeChildren = rootNode.children();
		
		FileObject nodeFile = (FileObject)rootNode.getUserObject();
		DefaultMutableTreeNode currChild = rootNode;
		while (nodeChildren.hasMoreElements() && pathDepth < parentPath.length) {			
			currChild = (DefaultMutableTreeNode) nodeChildren.nextElement();
			nodeFile = (FileObject)currChild.getUserObject();
			
			if (!nodeFile.get_isDirectory()) {
				retval[0] = "0";
				retval[1] = parentPath[pathDepth] + " is not a directory.";
				return retval;
			}
			else if (nodeFile.getName().equals(parentPath[pathDepth])) {
				nodeChildren = currChild.children();
				pathDepth++;
			}
		}
		
		if (! nodeFile.getName().equals(parentPath[parentPath.length-1])) {
			retval[0] = "0";
			retval[1] = "";
			for (int index = 0; index < parentPath.length; index++) {
				retval[1] += parentPath[index] + "/";
			}
			retval[1] += " does not exist.";
			return retval;
		}
		
		FileObject newDir = new FileObject (newDirName, true, 7, 7, 7, reqUser, reqUser);
		currChild.add(new DefaultMutableTreeNode(newDir));
		
		retval[0] = "1";
		retval[1] = "Directory " + fullPath + " created";
		
		return retval;
	}
	
	
	public static void main (String[] args) {
		String[] retval = new String[2];
		LocalPathTree lpt = LocalPathTree.get_Instance();
		UserManagement um = UserManagement.get_Instance();
		retval = lpt.make_Directory("/home", "777", um.get_ownUserName(), "5");
		System.out.println(retval[1]);
		retval = lpt.make_Directory("/home/paul", "777", um.get_ownUserName(), "5");
		System.out.println(retval[1]);
		retval = lpt.make_Directory("/home/jeff", "777", um.get_ownUserName(), "5");
		System.out.println(retval[1]);
		retval = lpt.make_Directory("/home/paul/foo", "777", um.get_ownUserName(), "5");
		System.out.println(retval[1]);
		retval = lpt.make_Directory("/home/paul/foo/bar", "777", um.get_ownUserName(), "5");
		System.out.println(retval[1]);
		retval = lpt.make_Directory("/home/jeff/wut", "777", um.get_ownUserName(), "5");
		System.out.println(retval[1]);
		retval = lpt.make_Directory("/home/jeff/foo/bar", "777", um.get_ownUserName(), "5");
		System.out.println(retval[1]);
		
		System.out.println();
		System.out.println(lpt.toString());
	}
}