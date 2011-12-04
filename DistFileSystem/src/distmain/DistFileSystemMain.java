/**
 * @author paul
 */


package distmain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.validator.routines.InetAddressValidator;
import distclient.*;
import distconfig.Constants;
import distconfig.Sha1Generator;
import distfilelisting.UserManagement;
import distnodelisting.NodeSearchTable;
import distserver.Server;



public class DistFileSystemMain {
	
	private String prompt = "[%s]$ ";
	private BufferedReader inStream;
	private UserManagement userManage = null;
	private NodeSearchTable nst = null;
	private Client cli = new Client();
	
	private String ipAddress = null;
	private Thread thServ = null;
	
	public DistFileSystemMain () {
		try {
			inStream = new BufferedReader(new InputStreamReader(System.in));
			userManage = UserManagement.get_Instance();
			nst = NodeSearchTable.get_Instance();
			
			System.out.print("Please input your username: ");
			String userName = "";
			do {
				userName = inStream.readLine().trim();
				if (userName.equals("")) {
					System.out.print("Username must contain non-whitespace characters. Please try again: ");
				}
				
			} while (userName.equals(""));
			
			userManage.set_ownUserName(userName);
			
			
			System.out.print("Would you like to [s]tart a network or [j]oin one that already exists?\n(s/j): ");
			String response = "";
			do {
				response = inStream.readLine().trim().toUpperCase();
				if (!(response.equals("S") || response.equals("J"))) {
					System.out.print("Invalid input received. Try again.\n(s/j): ");
				}
			} while (!(response.equals("S") || response.equals("J")));
			
			ipAddress = "";
			InetAddressValidator validator = InetAddressValidator.getInstance();


			this.start_server();
			switch (response) {
				case "J":
					System.out.print("Please input the IP address of the network you would like to join: ");
					do {
						ipAddress = inStream.readLine().trim();
						if (!validator.isValidInet4Address(ipAddress)){
							System.out.print("Invalid IPv4 address received. Try again: ");
						}
					} while (!validator.isValidInet4Address(ipAddress));

					break;
					
				case "S":
				default:
					this.connect_to_network();
				
			}

			this.run_interface();
			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public DistFileSystemMain (boolean gratus) {
		try {
			inStream = new BufferedReader(new InputStreamReader(System.in));
			userManage = UserManagement.get_Instance();
			nst = NodeSearchTable.get_Instance();
			
			System.out.print("Please input your username: ");
			String userName = "";
			do {
				userName = inStream.readLine().trim();
				if (userName.equals("")) {
					System.out.print("Username must contain non-whitespace characters. Please try again: ");
				}
				
			} while (userName.equals(""));
			
			userManage.set_ownUserName(userName);
			
			
			System.out.print("Would you like to [s]tart a network or [j]oin one that already exists?\n(s/j): ");
			String response = "";
			do {
				response = inStream.readLine().trim().toUpperCase();
				if (!(response.equals("S") || response.equals("J"))) {
					System.out.print("Invalid input received. Try again.\n(s/j): ");
				}
			} while (!(response.equals("S") || response.equals("J")));
			
			ipAddress = "";
			InetAddressValidator validator = InetAddressValidator.getInstance();

			System.out.print("Enter IP: ");
			ipAddress = inStream.readLine();
			
			if (!ipAddress.trim().equals("")) {
				this.connect_to_network();
			}

			this.start_server();
			/*switch (response) {
				case "J":
					System.out.print("Please input the IP address of the network you would like to join: ");
					do {
						ipAddress = inStream.readLine().trim();
						if (!validator.isValidInet4Address(ipAddress)){
							System.out.print("Invalid IPv4 address received. Try again: ");
						}
					} while (!validator.isValidInet4Address(ipAddress));

					break;
					
				case "S":
				default:
					this.connect_to_network();
				
			}*/

			this.run_interface();
			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start_server() {
		System.out.println("Starting Server");
		Server serv = new Server();
		thServ = new Thread (serv);
		thServ.start();
	}
	
	public void connect_to_network() {
		try {
			int nextID;
			String nextIP;
			
			
			// Connect to the network and get initial search location
			// These have to all run in the same thread since they rely on each other
			System.out.printf("Connecting to %s\n", ipAddress);
			ClntEnterNetwork cen = new ClntEnterNetwork(ipAddress, cli);
			cen.run();
			cen = null;
		
			nextID = cli.getServId();
			nextIP = cli.getServIp();
			
			// Locate the servers location
			System.out.printf("Entering network at %s\n", nextIP);
			ClntCheckPosition ccp = new ClntCheckPosition(nextIP, nextID, cli);
			ccp.run();
			ccp = null;
			
			String[] pred = cli.getPredecessor();
			String[] succ = cli.getSuccessor();
			
			// Connect to the predecessor
			System.out.printf("Connecting to predecessor %s\n", pred[1]);
			ClntNewPredecessor cnp = new ClntNewPredecessor(cli);
			cnp.run();
			cnp = null;
			
			// Connect to the successor
			System.out.printf("Connecting to successor %s\n", succ[1]);
			ClntNewSuccessor cns = new ClntNewSuccessor(cli);
			cns.run();
			cns = null;
			
			// Send new node notification
			System.out.printf("Sending the new node notification\n");
			ClntNewNode cnn = new ClntNewNode(succ[Constants.IP_ADDRESS]);
			cnn.run();
			cnn = null;
			
			System.out.println ("Connected to the network\n");
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void run_interface() {
		String input;
		boolean exit = false;
		while (!exit) {
			try {
				System.out.printf(this.prompt, this.userManage.get_ownUserName());
				input = inStream.readLine().toLowerCase().trim();
				/*String[] instr = input.split(" ",2);
				String path;
				
				switch (instr[0]) {
				
				case "write":
					
					if (instr.length > 1) {
						path = instr[1].split(" ",1)[0].trim();
						
						if (canWrite(path)) {
							//TODO
						} else {
							System.out.println("Can not write to \"" + path + "\".");
						}
						
					}
					
					break;
				
				case "read":
					
					if (instr.length > 1) {
						path = instr[1].split(" ",1)[0].trim();
						
						if (canRead(path) && exists(path)) {
							//TODO
						} else {
							System.out.println("Can not read from \"" + path + "\" or it does not exist.");
						}
						
					}
					
					break;

				case "rm":
					if (instr.length > 1) {
						path = instr[1].split(" ",1)[0].trim();
						
						if (canWrite(path) && exists(path)) {
							//TODO
						} else {
							System.out.println("Can not remove \"" + path + "\" or it does not exist.");
						}
						
					}
					
					break;
					
				default:
					System.out.println("Unrecognized command: \""+ instr[0] + "\".");
					break;
				}*/
				
				
				if (input.equals("view predecessor")) {
					System.out.printf("Predecessor ID = %s\n", this.nst.get_predecessorID());
					System.out.printf("Predecessor IP = %s\n", this.nst.get_predecessorIPAddress());
				}
				else if (input.equals("view node search table")) {
					for(int index = 0; index < this.nst.size(); index++) {
						System.out.printf("Entry: %d\tID: %s\tIP: %s\n",
								index, this.nst.get_IDAt(index), this.nst.get_IPAt(index));
					}
				}
				else if (input.equals("view own information")) {
					System.out.printf("ID: %s\nIP: %s\n", this.nst.get_ownID(), this.nst.get_ownIPAddress());
				}
				else if (input.contains("sha1")) {
					String[] vals = input.split(" ");
					for (int index = 0; index < vals.length; index++) {
						System.out.printf("%s :\t%s\n", vals[index], Sha1Generator.generate_Sha1(vals[index]));
					}
				}
				else if (input.contains("test")) {
					DistFileSystemTest dfst = new DistFileSystemTest ();
					dfst.runTestCommand(input);
				}
				
			}
			
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main (String[] args) {
		new DistFileSystemMain();
	}
	
	public boolean exists(String path) {
		// TODO
		
		return true;
	}
	
	public boolean canWrite(String path) {
		
		// TODO
		return true;
	}
	
	public boolean canRead(String path) {
		// TODO
		return true;
	}
}

