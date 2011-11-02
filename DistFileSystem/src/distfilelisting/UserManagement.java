/**
*/

package distfilelisting;

import java.util.Hashtable;
import java.util.Vector;

/**
 * @author paul
 *
 */
public class UserManagement {
	
	private static UserManagement instance = null;
	
	private String ownUsername = null;
    private Hashtable<String, String> globalUsers = null;
    private Vector<String> globalGroupList = null;
    private Hashtable<String, Vector<String>> globalUserGroups = null;
    
    private UserManagement () {
		ownUsername = System.getProperty("user.name");
		this.globalUsers = new Hashtable<String, String>();
		this.globalUsers.put(ownUsername, "5");
	}
	
	public static UserManagement get_Instance () {
		if (instance == null) {
			instance = new UserManagement();
		}
		
		return instance;
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
			retval[0] = "0";
			retval[1] = "The user " + userName +
					", already exists";
		}
		else {
			this.globalUsers.put(userName, passHash);
			this.create_Group(userName, userName, passHash);
		}
		
		return retval;
	}
	
	public String[] create_Group (String groupName, String authUserName, String passHash) {

    	String[] retval = {null, null};
    	if (!this.globalUsers.containsKey(authUserName)) {
    		retval[0] = "0";
    		retval[1] = "User, " + authUserName + ", does not exist";
    	}
    	else if (!((String)this.globalUsers.get(authUserName)).equals(passHash)) {
    		retval[0] = "0";
    		retval[1] = "Incorrect authentication";
    	}
    	else if (this.globalGroupList.contains(groupName)) {
    		retval[0] = "0";
    		retval[1] = "Group, " + groupName + ", already exists";
    	}
    	else {
    		this.globalGroupList.add(groupName);
    		
    		Vector<String> currGroups = this.globalUserGroups.get(authUserName);
    		currGroups.add(groupName);
    		this.globalUserGroups.put(authUserName, currGroups);
    		
    		retval[0] = "1";
    		retval[1] = "Successfully created " + groupName;
    	}
    	
    	return retval;
	}
	
	public String[] remove_UserFromGroup (String userToRemove, String groupName,
			String authUserName, String passHash) {

    	String[] retval = {null, null};
    	if (!this.globalUsers.containsKey(authUserName)) {
    		retval[0] = "0";
    		retval[1] = "User, " + authUserName + ", does not exist";
    	}
    	else if (!this.globalUsers.containsKey(userToRemove)) {
    		retval[0] = "0";
    		retval[1] = "User, " + userToRemove + ", does not exist";
    	}
    	else if (!((String)this.globalUsers.get(authUserName)).equals(passHash)) {
    		retval[0] = "0";
    		retval[1] = "Incorrect authentication";
    	}
    	else if (!this.globalGroupList.contains(groupName)) {
    		retval[0] = "0";
    		retval[1] = "Group, " + groupName + ", does not exist";
    	}
    	else if (!this.globalUserGroups.get(authUserName).contains(groupName)) {
    		retval[0] = "0";
    		retval[1] = "Requesting user not in group " + groupName;
    	}
    	else {
    		Vector<String> currGroups = this.globalUserGroups.get(userToRemove);
    		currGroups.add(groupName);
    		this.globalUserGroups.put(userToRemove, currGroups);
    		
    		retval[0] = "1";
    		retval[1] = "Successfully added " + userToRemove +
    				", to group " + groupName + ".";
    	}
    	
    	return retval;		
	}
	
	public String[] add_UserToGroup (String userToAdd, String groupName, 
			String authUserName, String passHash) {
		
    	String[] retval = {null, null};
    	if (!this.globalUsers.containsKey(authUserName)) {
    		retval[0] = "0";
    		retval[1] = "User, " + authUserName + ", does not exist";
    	}
    	else if (!this.globalUsers.containsKey(userToAdd)) {
    		retval[0] = "0";
    		retval[1] = "User, " + userToAdd + ", does not exist";
    	}
    	else if (!((String)this.globalUsers.get(authUserName)).equals(passHash)) {
    		retval[0] = "0";
    		retval[1] = "Incorrect authentication";
    	}
    	else if (!this.globalGroupList.contains(groupName)) {
    		retval[0] = "0";
    		retval[1] = "Group, " + groupName + ", does not exist";
    	}
    	else if (!this.globalUserGroups.get(authUserName).contains(groupName)) {
    		retval[0] = "0";
    		retval[1] = "Requesting user not in group " + groupName;
    	}
    	else {
    		Vector<String> currGroups = this.globalUserGroups.get(userToAdd);
    		currGroups.add(groupName);
    		this.globalUserGroups.put(userToAdd, currGroups);
    		
    		retval[0] = "1";
    		retval[1] = "Successfully added " + userToAdd +
    				", to group " + groupName + ".";
    	}
    	
    	return retval;
    }
}