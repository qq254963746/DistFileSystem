/**
*/

package distfilelisting;

import java.io.Serializable;
import java.util.Date;

import distconfig.Constants;
import distconfig.Sha1Generator;


public class FileObject implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String owner;
	private String group;
	private int ownerPermission;
	private int groupPermission;
	private int globalPermission;
	private boolean isDirectory;
	private Date lastupdate;
	private int hash;
	
	public FileObject (String name, boolean isDirectory, int ownerPerm, 
			int groupPerm, int globalPerm, String reqUser, String defGroup) {
		
		this.name = name;
		this.ownerPermission = ownerPerm;
		this.groupPermission = groupPerm;
		this.globalPermission = globalPerm;
		this.isDirectory = isDirectory;
		this.owner = reqUser;
		this.group = defGroup;
		this.set_hash(Sha1Generator.generate_Sha1(this.name));
		this.lastupdate = new Date();
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
			retval[0] = Integer.toString(Constants.FAILURE);
			retval[1] = "Requesting user, " + reqUser + ", is not owner";
		}
		else if (!um.auth_User(reqUser, hashAuth)) {
			retval[0] = Integer.toString(Constants.FAILURE);
			retval[1] = "Bad password for user " + reqUser;
		}
		else {
			this.owner = newOwner;
			retval[0] = Integer.toString(Constants.SUCCESS);
			retval[1] = "Owner of " + this.name +
					", changed to " + newOwner;
			this.lastupdate = new Date();
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
			retval[0] = Integer.toString(Constants.FAILURE);
			retval[1] = "Bad password for user " + reqUser;
		}
		else if (!reqUser.equals(this.owner)) {
			retval[0] = Integer.toString(Constants.FAILURE);
			retval[1] = "User " + reqUser +
					", is not the owner of " + this.name;
			this.group = newGroup;
		}
		else {
			retval[0] = Integer.toString(Constants.SUCCESS);
			retval[1] = "Group has been changed to " + newGroup;
			this.group = newGroup;
			this.lastupdate = new Date();
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
			retval[0] = Integer.toString(Constants.FAILURE);
			retval[1] = "User " + reqUser +
					" is not the owner of " + this.name;
		}
		else {
			this.ownerPermission = ownerPerm;
			this.groupPermission = groupPerm;
			this.globalPermission = globalPerm;
			
			retval[0] = Integer.toString(Constants.SUCCESS);
			retval[1] = "Permissions of " + this.name +
					"Successfully set to " +
					Integer.toString(this.ownerPermission) +
					Integer.toString(this.groupPermission) +
					Integer.toString(this.globalPermission);
			
			this.lastupdate = new Date();
		}
		
		return retval;
	}
	
	public boolean get_isDirectory () {
		return this.isDirectory;
	}
	
	public static int[] get_permissionsFromString(String permissions) {
		int[] perms = new int[3];
		
		char[] permchar = permissions.toCharArray();
		perms[0] = Character.getNumericValue(permchar[0]);
		perms[1] = Character.getNumericValue(permchar[1]);
		perms[2] = Character.getNumericValue(permchar[2]);
		
		return perms;
	}

	public int get_hash() {
		return hash;
	}

	public void set_hash(int hash) {
		this.hash = hash;
		this.lastupdate = new Date();
	}

	/**
	 * @return the date this file was last updated
	 */
	public Date getLastupdate() {
		return lastupdate;
	}

	/**
	 * @param lastupdate the date this file was last updated to set
	 */
	public void setLastupdate(Date lastupdate) {
		this.lastupdate = lastupdate;
	}
}