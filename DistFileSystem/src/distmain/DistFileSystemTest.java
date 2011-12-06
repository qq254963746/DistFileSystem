/**
 * @author paul
 */

package distmain;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.acl.LastOwnerException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import distclient.Client;
import distclient.ClntGetFile;
import distclient.ClntUploadFile;
import distconfig.ConnectionCodes;
import distconfig.DistConfig;
import distfilelisting.FileObject;
import distfilelisting.LocalPathList;
import distfilelisting.UserManagement;
import distnodelisting.NodeSearchTable;
import distserver.ServHeartBeat;
import distserver.ServNodeDropped;
import distserver.ServPredecessorDropped;

public class DistFileSystemTest {
	private UserManagement userManage = null;
	private NodeSearchTable nst = null;
	private BufferedReader inStream;
	
	public DistFileSystemTest () {
		this.userManage = UserManagement.get_Instance();
		this.nst = NodeSearchTable.get_Instance();
		this.inStream = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public Thread runTestCommand (String command) {
		try {
			if (command.contains("addfile")) {
				System.out.printf("Path to File: ");
				String pathToFile = inStream.readLine().trim();
				File fi = new File(pathToFile);
				if (!fi.exists()) {
					FileNotFoundException fnfe = 
							new FileNotFoundException(pathToFile);
					throw fnfe;
				}
				int lastSlash = pathToFile.lastIndexOf(System.getProperty("file.separator"));
				String filename = pathToFile.substring(lastSlash+1);
				
				System.out.printf("File Permissions (744): ");
				String permissions = inStream.readLine();
				
				FileObject nfo = new FileObject(filename, permissions, 
						this.userManage.get_ownUserName(), this.userManage.get_ownUserName());
				
				Client cli = new Client();
				ClntUploadFile cuf = new ClntUploadFile (cli, nfo, fi, this.userManage.get_ownUserName());
				cuf.run();
				//cli.addTask(cuf);	
			}
			
			else if (command.contains("view files")) {
				LocalPathList lpl = LocalPathList.get_Instance();
				System.out.printf("Number of Files : %d\n", lpl.size());
				for (int index = 0; index < lpl.size(); index++) {
					System.out.printf("File Number: %s\tFile Name: %s\n", index, lpl.get(index).getName());
				}
			}
			
			else if (command.contains("view users")) {
				UserManagement um = UserManagement.get_Instance();
				Hashtable<String, Vector<String>> users = um.get_usernames();
				Enumeration<String> keys = users.keys();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					System.out.printf("%s: ", key);
					for (String elm : users.get(key)) {
						System.out.printf("%s, ", elm);
					}
					System.out.print("\n");
				}
			}
			
			else if (command.contains("getfile")) {
				System.out.printf("File Name: ");
				String fileName = inStream.readLine();
				System.out.printf("Path to Place: ");
				String pathToPlace = inStream.readLine();
				
				Client cli = new Client();
				
				ClntGetFile cgf = new ClntGetFile (cli, fileName, pathToPlace, this.userManage.get_ownUserName());
				cgf.run();
			}
			
			else if (command.contains("heartbeat")) {
				NodeSearchTable nst = NodeSearchTable.get_Instance();
            	Socket sock;
            	DistConfig distConfig = DistConfig.get_Instance();
				try {
					// Attempt to establish a connection
					sock = new Socket(nst.get_IPAt(0), distConfig.get_servPortNumber());
					sock.setSoTimeout(5000);
					
					// Get the output stream for the server
			        BufferedOutputStream bos = new BufferedOutputStream (
			                sock.getOutputStream());
			        // Setup the writer to the server
			        PrintWriter outStream = new PrintWriter(bos, false);
					
			        // Send code for which server to start
			        outStream.println(ConnectionCodes.HEARTBEAT);
					outStream.flush();
					
					// Setup the new thread and start
					// the heartbeat thread in the background
					ServHeartBeat dshb = new ServHeartBeat(sock, false);
					dshb.run();
                    
				}
				catch (ConnectException e) {
					Socket newpred;
					try {
						System.out.println("Inside initial catch");
						// Send out message that predecessor failed
						String failedip = nst.get_IPAt(0);
						String nextip = nst.get_IPAt(1);
						String nextid = nst.get_IDAt(1);
						
						for (int index = 0; index < nst.size(); index++) {
							if (!failedip.equals(nst.get_IPAt(index)) &&
									!nst.get_IPAt(index).equals(nst.get_ownIPAddress())) {
								nextip = nst.get_IPAt(index);
								nextid = nst.get_IDAt(index);
								break;
							}
						}
						
						if (nextip.equals(failedip)) {
							nextip = nst.get_predecessorIPAddress();
							nextid = nst.get_predecessorID();
						}
						
						if (nextip.equals(failedip)) {
							LastOwnerException loe = new LastOwnerException();
							throw loe;
						}
						
						newpred = new Socket(nextip, distConfig.get_servPortNumber());
						newpred.setSoTimeout(5000);
						
						// If the connection completes, run the heart beat
						BufferedOutputStream bos = new BufferedOutputStream (newpred.getOutputStream());
						PrintWriter outStream = new PrintWriter(bos, false);
						
						outStream.println(ConnectionCodes.PREDDROPPED);
						outStream.flush();
						
						// Setup the new thread and start
						// transferring in the background
						ServPredecessorDropped dspd =
								new ServPredecessorDropped(newpred, true);
						dspd.run();
						//Thread enterDSPD = new Thread(dspd);
						//enterDSPD.start();
						//this.backgrounded.add(enterDSPD);
						//enterDSPD = null;
						
						outStream.close();
						bos.close();
						
						nst.set(0, nextid, nextip);
						
						//
						// Send out signal that the node has failed
						//
						Socket nodefail = new Socket(nextip, distConfig.get_servPortNumber());
						nodefail.setSoTimeout(5000);
						
						// If the connection completes, run the heart beat
						bos = new BufferedOutputStream (nodefail.getOutputStream());
						outStream = new PrintWriter(bos, false);
						
						outStream.println(ConnectionCodes.NODEDROPPED);
						outStream.flush();
						
						// Setup the new thread and start
						// transferring the information for the node that dropped
						ServNodeDropped dsnd =
								new ServNodeDropped(nodefail);
						dsnd.runas_client(nst.get_ownID());
					}
					catch (LastOwnerException loe) {
						System.out.println("Last node in network");
						for (int index = 0; index < nst.size(); index++) {
							nst.set(index, nst.get_ownID(), nst.get_ownIPAddress());
						}
						nst.set_predicessor(nst.get_ownID(), nst.get_ownIPAddress());
					}
					catch (Exception we) {
						we.printStackTrace();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch (FileNotFoundException e) {
			System.out.printf("The file %s, could not be found\n", e.getMessage());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
