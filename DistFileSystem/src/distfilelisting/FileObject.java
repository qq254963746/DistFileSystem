/**
*/

package distfilelisting;

import distconfig.UserManagement;

public class FileObject {
	
	private String name;
	private String owner;
	private String group;
	private int ownerPermission;
	private int groupPermision;
	private int globalPermission;
	
	public FileObject () {
		
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

	public void setOwnerPermission(int ownerPermission) {
		this.ownerPermission = ownerPermission;
	}

	public int getGroupPermision() {
		return groupPermision;
	}

	public void setGroupPermision(int groupPermision) {
		this.groupPermision = groupPermision;
	}

	public int getGlobalPermission() {
		return globalPermission;
	}

	public void setGlobalPermission(int globalPermission) {
		this.globalPermission = globalPermission;
	}
}