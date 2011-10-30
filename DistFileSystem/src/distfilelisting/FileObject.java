/**
*/

package distfilelisting;

import distconfig.UserManagement;

public class FileObject {
	
	private String name;
	private String owner;
	private String group;
	private int ownerPermission;
	private int groupPermission;
	private int globalPermission;
	private boolean isDirectory;
	
	public FileObject (String name, boolean isDirectory, int ownerPerm, 
			int groupPerm, int globalPerm, String reqUser, String defGroup) {
		
		this.name = name;
		this.ownerPermission = ownerPerm;
		this.groupPermission = groupPerm;
		this.globalPermission = globalPerm;
		this.isDirectory = isDirectory;
		this.owner = reqUser;
		this.group = defGroup;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getOwner() {
		return owner;
	}

	public String[] setOwner(String newOwner, String reqUser, String hashAuth) {
		UserManagement um = UserManagement.get_Instance();
		String[] retval = {null, null};
		
		if (!this.owner.equals(reqUser)) {
			retval[0] = "0";
			retval[1] = "Requesting user, " + reqUser + ", is not owner";
		}
		else if (!um.auth_User(reqUser, hashAuth)) {
			retval[0] = "0";
			retval[1] = "Bad password for user " + reqUser;
		}
		else {
			this.owner = newOwner;
			retval[0] = "1";
			retval[1] = "Owner of " + this.name +
					", changed to " + newOwner;
		}
		
		return retval;
	}

	public String getGroup() {
		return group;
	}

	public String[] setGroup(String newGroup, String reqUser, String authHash) {
		String[] retval = {null, null};
		
		UserManagement um = UserManagement.get_Instance();
		
		if(!um.auth_User(reqUser, authHash)) {
			retval[0] = "0";
			retval[1] = "Bad password for user " + reqUser;
		}
		else if (!reqUser.equals(this.owner)) {
			retval[0] = "0";
			retval[1] = "User " + reqUser +
					", is not the owner of " + this.name;
			this.group = newGroup;
		}
		else {
			retval[0] = "1";
			retval[1] = "Group has been changed to " + newGroup;
			this.group = newGroup;
		}
				
		return retval;
	}

	public int getOwnerPermission() {
		return ownerPermission;
	}

	public String[] setOwnerPermission(int ownerPerm,
			String reqUser, String authHash) {
		
		return this.set_Permissions(ownerPerm, this.groupPermission, 
				this.globalPermission,reqUser, authHash);
	}

	public int getGroupPermision() {
		return groupPermission;
	}

	public String[] setGroupPermision(int groupPerm,
			String reqUser, String authHash) {
		
		return this.set_Permissions(this.ownerPermission, groupPerm,
				this.globalPermission, reqUser, authHash);
	}

	public int getGlobalPermission() {
		return globalPermission;
	}

	public String[] setGlobalPermission(int globalPerm,
			String reqUser, String authHash) {
		
		return this.set_Permissions(this.ownerPermission, this.groupPermission,
				globalPerm, reqUser, authHash);
	}
	
	public String[] set_Permissions (int ownerPerm, int groupPerm, 
			int globalPerm, String reqUser, String authHash) {
		String[] retval = {null, null};
		
		UserManagement um = UserManagement.get_Instance();
		
		if (!um.auth_User(reqUser, authHash)) {
			retval[0] = "0";
			retval[1] = "User " + reqUser +
					" is not the owner of " + this.name;
		}
		else {
			this.ownerPermission = ownerPerm;
			this.groupPermission = groupPerm;
			this.globalPermission = globalPerm;
			
			retval[0] = "1";
			retval[1] = "Permissions of " + this.name +
					"Successfully set to " +
					Integer.toString(this.ownerPermission) +
					Integer.toString(this.groupPermission) +
					Integer.toString(this.globalPermission);
		}
		
		return retval;
	}
	
	public boolean get_isDirectory () {
		return this.isDirectory;
	}
}