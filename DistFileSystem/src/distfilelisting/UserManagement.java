/**
*/

package distfilelisting;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import distconfig.Constants;

/**
 * @author paul
 *
 */
public class UserManagement implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static UserManagement instance = null;
	
	private String ownUsername = null;
    private Hashtable<String, String> globalUsers = null;
    private Vector<String> globalGroupList = null;
    private Hashtable<String, Vector<String>> globalUserGroups = null;
    
    private UserManagement () {
		//ownUsername = System.getProperty("user.name");
		this.globalUsers = new Hashtable<String, String>();
		//this.globalUsers.put(ownUsername, "5");
		this.globalGroupList = new Vector<String>();
		this.globalUserGroups = new Hashtable<String, Vector<String>>();
		
	}
	
	public static UserManagement get_Instance () {
		if (instance == null) {
			instance = new UserManagement();
		}
		
		return instance;
	}
	
	public static UserManagement set_Instance(UserManagement userManage) {
		String curruser = instance.get_ownUserName();
		instance = userManage;
		instance.set_ownUserName(curruser);
		return instance;
		
	}
	
	public void set_ownUserName (String ownUserName) {
		this.ownUsername = ownUserName;
		if (!this.globalUsers.containsKey(ownUserName))
			this.globalUsers.put(ownUserName, "1");
		this.create_Group(ownUserName, ownUserName, "1");
	}
	
	public String get_ownUserName () {
		return this.ownUsername;
	}
	
	public Vector<String> get_GroupsForUser (String userName) {
		return this.globalUserGroups.get(userName);
	}
	
	public boolean auth_User (String userName, String passHash) {
		if (this.globalUsers.get(userName).equals(passHash)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean auth_User (String userName, String passHash, FileObject fo) {
		
		if (!auth_User (userName, passHash))
			return false;
		
		if (userName.equals(fo.getOwner())) {
			return true;
		}
		
		Vector<String> groups = this.globalUserGroups.get(userName);
		if (groups.contains(fo.getGroup())) {
			if (fo.getGroupPermision() >= 4)
				return true;
		}
		
		if (fo.getGlobalPermission() >= 4)
			return true;
		
		return false;
	}
	
	public boolean is_UserInGroup (String userName, String group) {
		if (this.globalUserGroups.get(userName).contains(group)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public String[] add_User (String userName, String passHash) {
		
		String[] retval = {null, null};
		
		if (this.globalUsers.containsKey(userName)) {
			retval[0] = Integer.toString(Constants.FAILURE);
			retval[1] = "The user " + userName +
					", already exists";
		}
		else {
			this.globalUsers.put(userName, passHash);
			this.create_Group(userName, userName, passHash);
			
			retval[0] = Integer.toString(Constants.SUCCESS);
			retval[1] = "The user " + userName +
					", was successfully added";
		}
		
		return retval;
	}
	
	public String[] create_Group (String groupName, String authUserName, String passHash) {

    	String[] retval = {null, null};
    	if (!this.globalUsers.containsKey(authUserName)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "User, " + authUserName + ", does not exist";
    	}
    	else if (!((String)this.globalUsers.get(authUserName)).equals(passHash)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "Incorrect authentication";
    	}
    	else if (this.globalGroupList.contains(groupName)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "Group, " + groupName + ", already exists";
    	}
    	else {
    		this.globalGroupList.add(groupName);
    		
    		Vector<String> currGroups = this.globalUserGroups.get(authUserName);
    		if (currGroups == null) {
    			currGroups = new Vector<String>();
    		}
    		
    		currGroups.add(groupName);
    		this.globalUserGroups.put(authUserName, currGroups);
    		
    		retval[0] = Integer.toString(Constants.SUCCESS);
    		retval[1] = "Successfully created " + groupName;
    	}
    	
    	return retval;
	}
	
	public String[] remove_UserFromGroup (String userToRemove, String groupName,
			String authUserName, String passHash) {

    	String[] retval = {null, null};
    	if (!this.globalUsers.containsKey(authUserName)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "User, " + authUserName + ", does not exist";
    	}
    	else if (!this.globalUsers.containsKey(userToRemove)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "User, " + userToRemove + ", does not exist";
    	}
    	else if (!((String)this.globalUsers.get(authUserName)).equals(passHash)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "Incorrect authentication";
    	}
    	else if (!this.globalGroupList.contains(groupName)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "Group, " + groupName + ", does not exist";
    	}
    	else if (!this.globalUserGroups.get(authUserName).contains(groupName)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "Requesting user not in group " + groupName;
    	}
    	else {
    		Vector<String> currGroups = this.globalUserGroups.get(userToRemove);
    		currGroups.remove(groupName);
    		this.globalUserGroups.put(userToRemove, currGroups);
    		
    		retval[0] = Integer.toString(Constants.SUCCESS);
    		retval[1] = "Successfully removed " + userToRemove +
    				", to group " + groupName + ".";
    	}
    	
    	return retval;		
	}
	
	public String[] add_UserToGroup (String userToAdd, String groupName, 
			String authUserName, String passHash) {
		
    	String[] retval = {null, null};
    	if (!this.globalUsers.containsKey(authUserName)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "User, " + authUserName + ", does not exist";
    	}
    	else if (!this.globalUsers.containsKey(userToAdd)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "User, " + userToAdd + ", does not exist";
    	}
    	else if (!((String)this.globalUsers.get(authUserName)).equals(passHash)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "Incorrect authentication";
    	}
    	else if (!this.globalGroupList.contains(groupName)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "Group, " + groupName + ", does not exist";
    	}
    	else if (!this.globalUserGroups.get(authUserName).contains(groupName)) {
    		retval[0] = Integer.toString(Constants.FAILURE);
    		retval[1] = "Requesting user not in group " + groupName;
    	}
    	else {
    		Vector<String> currGroups = this.globalUserGroups.get(userToAdd);
    		currGroups.add(groupName);
    		this.globalUserGroups.put(userToAdd, currGroups);
    		
    		retval[0] = Integer.toString(Constants.SUCCESS);
    		retval[1] = "Successfully added " + userToAdd +
    				", to group " + groupName + ".";
    	}
    	
    	return retval;
    }
	
	public Hashtable<String,Vector<String>> get_usernames () {
		Hashtable<String,Vector<String>> users = new Hashtable<String,Vector<String>>();
		
		Enumeration<String> keys = this.globalUsers.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			Vector<String> groups = this.globalUserGroups.get(key);
			users.put(key, groups);
		}
		return users;
	}
	
	public boolean doesGroupExist (String groupName) {
		for (int index = 0; index < this.globalGroupList.size(); index++) {
			if (groupName.equals(this.globalGroupList.get(index))) {
				return true;
			}
		}
			
		return false;
	}
	
	public boolean doesUserExist (String userName) {
		try {
			this.globalUsers.get(userName);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}