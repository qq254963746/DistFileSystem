/**
 * @author paul
 */

package distserver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import distconfig.ConnectionCodes;
import distconfig.Constants;
import distconfig.DistConfig;
import distfilelisting.UserManagement;
import distnodelisting.NodeSearchTable;

public class ServUserManagement implements Runnable {

	private Socket client = null;
	private DistConfig distConfig = null;
	private UserManagement userManage = null;
	
	public ServUserManagement (Socket cli) {
		this.client = cli;
	}
	
	@Override
	public void run() {
		this.distConfig = DistConfig.get_Instance();
		this.userManage = UserManagement.get_Instance();
		
		try {
			System.out.println("In thread for managing users");
            
            // Get the input stream for the client
            BufferedReader inStream = new BufferedReader (
                    new InputStreamReader(client.getInputStream()));
            // Get the output stream for the client
            BufferedOutputStream bos = new BufferedOutputStream (
                    client.getOutputStream());
            // Setup the writer to the client
            PrintWriter outStream = new PrintWriter(bos, false);
            
            // Send acknowledgment everything is set
            outStream.println(ConnectionCodes.USERMANAGEMENT);
            outStream.flush();
            
            // Get the ID of the initiator
            String origID = inStream.readLine();
            // Get the user name of the requester
            String origUserName = inStream.readLine();
            // Get what they want to be done
            Integer manageCode = Integer.parseInt(inStream.readLine());
            // Get the group to modify
            String groupName = inStream.readLine();
            // The results of the usermanagement
            String[] results = new String[2];
            String userToChange = null;
            
            switch (manageCode) {
            case ConnectionCodes.NEWGROUP:
            	// Send confirm signal
            	outStream.println(ConnectionCodes.NEWGROUP);
            	// Create the group
            	results = userManage.create_Group(groupName, origUserName, "1");
            	// Send the code and string
            	outStream.println(results[0]);
            	outStream.println(results[1]);
            	outStream.flush();
            	break;
            case ConnectionCodes.ADDUSERTOGROUP:
            	// Send confirm signal
            	outStream.println(ConnectionCodes.ADDUSERTOGROUP);
            	// Get the user to be added
            	userToChange = inStream.readLine();
            	// Create the group
            	results = userManage.add_UserToGroup(userToChange, groupName, origUserName, "1");
            	// Send the results back
            	outStream.println(results[0]);
            	outStream.println(results[1]);
            	outStream.flush();
            	break;
            case ConnectionCodes.REMOVEUSERFROMGROUP:
            	// Send confirm signal
            	outStream.println(ConnectionCodes.REMOVEUSERFROMGROUP);
            	// Get the user to be removed
            	userToChange = inStream.readLine();
            	// Remove the user from the group
            	results = userManage.remove_UserFromGroup(userToChange, groupName, origUserName, "1");
            	// Send back the results
            	outStream.println(results[0]);
            	outStream.println(results[1]);
            	outStream.flush();
            	break;
            default:
            	// The code wasn't recognized, send back that signal
            	outStream.println(ConnectionCodes.UNRECOGNIZEDCODE);
            	results[0] = Integer.toString(Constants.FAILURE);
            	results[1] = "The code " + manageCode + 
            			", was unrecognized as a user management code.";
            	// Send back the results
            	outStream.println(results[0]);
            	outStream.println(results[1]);
            	outStream.flush();
            	break;
            }
            
            // Close all the connections and streams
            outStream.close();
            bos.close();
            inStream.close();
            client.close();
            
            // Check if the signal succeeded
            // And this is not the originator
            if (Integer.parseInt(results[0]) == Constants.SUCCESS &&
            		!NodeSearchTable.get_Instance().get_ownID().equals(origID)) {
            			
            	// Setup and forward the information to the next node
            	String nextIP = NodeSearchTable.get_Instance().get_IPAt(0);
            	
            	try {
            		Socket successor = new Socket(nextIP, distConfig.get_servPortNumber());
					successor.setSoTimeout(5000);
					
					// Get the streams
					bos = new BufferedOutputStream (successor.getOutputStream());
					outStream = new PrintWriter(bos, false);
					
					// Get input stream
					inStream = new BufferedReader (
		                    new InputStreamReader(successor.getInputStream()));
					
					// Send first signal
					outStream.println(ConnectionCodes.USERMANAGEMENT);
					outStream.flush();
					
					// Receive ack
					inStream.readLine();
					
					// Send the originating ID, username
					outStream.println(origID);
					outStream.println(origUserName);
					
					// Send the next code and group name
					outStream.println(manageCode);
					outStream.println(groupName);
					
					// Receive ack
					Integer ack = Integer.parseInt(inStream.readLine());
					
					switch (ack) {
					case ConnectionCodes.NEWGROUP:
						break;
					case ConnectionCodes.ADDUSERTOGROUP:
					case ConnectionCodes.REMOVEUSERFROMGROUP:
						// Send the user to be added or removed
						outStream.println(userToChange);
						outStream.flush();
						break;
					}
					
					// Get the result
					inStream.readLine();
					inStream.readLine();
					
		            // Close connection
					outStream.close();
					bos.close();
					inStream.close();
					successor.close();
            	}
            	// Catch any potential errors
        		catch (IOException ex) {
                    Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
		}
		// Catch any potential errors
		catch (IOException ex) {
            Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
        }
		
	}
	
}

