package distclient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import distconfig.ConnectionCodes;
import distconfig.DistConfig;
import distfilelisting.UserManagement;
import distnodelisting.NodeSearchTable;
import distconfig.Constants;

/**
 * @author paul
 */


public class ClntUserManagement implements Runnable {
	
	private DistConfig distConfig = null;
	private UserManagement um = null;
	private NodeSearchTable nst = null;
	private String[] usersToModify = null;
	private String groupToModify = null;
	private int whatToDo = Constants.REMOVEUSERFROMGROUP;

	public ClntUserManagement (int whatToDo, String[] usersToModify, String groupToModify) {
		this.usersToModify = usersToModify.clone();
		this.groupToModify = groupToModify;
		this.whatToDo = whatToDo;
	}
	
	@Override
	public void run() {
		
		this.distConfig = DistConfig.get_Instance();
		this.um = UserManagement.get_Instance();
		this.nst = NodeSearchTable.get_Instance();
		
		try {
			if (nst.get_ownID().equals(nst.get_IDAt(0))) {
				return;
			}
			
			Socket client = new Socket (nst.get_IPAt(0), distConfig.get_servPortNumber());
			client.setSoTimeout(5000);
			
			BufferedOutputStream bos = new BufferedOutputStream (
                    client.getOutputStream());
			System.out.println("Got OutputStream");
			PrintWriter outStream = new PrintWriter(bos, false);
			System.out.println("Got PrintWriter");
			
			BufferedReader inStream = new BufferedReader (
			        new InputStreamReader (
			                client.getInputStream()));
			System.out.println("Got InputStream");
			
			outStream.println(ConnectionCodes.USERMANAGEMENT);
			outStream.flush();
			
			inStream.readLine();
			
			outStream.println(nst.get_ownID());
			outStream.println(um.get_ownUserName());
			outStream.println(this.groupToModify);
			outStream.flush();
			
			switch (this.whatToDo) {
			case Constants.CREATEGROUP:
				outStream.println(ConnectionCodes.NEWGROUP);
				outStream.flush();
				inStream.readLine();
				
				System.out.println(inStream.readLine());
				System.out.println(inStream.readLine());
				
				break;
			case Constants.ADDUSERTOGROUP:
				outStream.println(ConnectionCodes.ADDUSERTOGROUP);
				outStream.flush();
				inStream.readLine();
				outStream.println(this.usersToModify.length);
				outStream.flush();
				for (int index = 0; index < this.usersToModify.length; index++) {
					outStream.println(this.usersToModify[index]);
				}
				
				System.out.println(inStream.readLine());
				System.out.println(inStream.readLine());
				
				break;
			case Constants.REMOVEUSERFROMGROUP:
				outStream.println(ConnectionCodes.REMOVEUSERFROMGROUP);
				outStream.flush();
				inStream.readLine();
				outStream.println(this.usersToModify.length);
				outStream.flush();
				for (int index = 0; index < this.usersToModify.length; index++) {
					outStream.println(this.usersToModify[index]);
				}
				
				System.out.println(inStream.readLine());
				System.out.println(inStream.readLine());
				break;
			default:
				break;
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
