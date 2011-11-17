/**
*/

package distserver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import distconfig.ConnectionCodes;
import distconfig.DistConfig;
import distconfig.Sha1Generator;
import distfilelisting.FileObject;
import distfilelisting.LocalPathList;
import distfilelisting.UserManagement;
import distnodelisting.NodeSearchTable;


public class ServUploadFile implements Runnable {

	private Socket client = null;
	private DistConfig distConfig = null;
	
	public ServUploadFile (Socket cli) {
		this.client = cli;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try {
			System.out.println("In thread for check position");
            this.distConfig = DistConfig.get_Instance();
            
            // Get the input stream for the client
            BufferedReader inStream = new BufferedReader (
                    new InputStreamReader(client.getInputStream()));
            // Create object input stream
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
            // Get the output stream for the client
            BufferedOutputStream bos = new BufferedOutputStream (
                    client.getOutputStream());
            // Setup the writer to the client
            PrintWriter outStream = new PrintWriter(bos, false);
            
            // Send acknowledgment everything is set
            outStream.println(ConnectionCodes.SENDFILE);
            outStream.flush();
      
            // Get the file name
            String filename = inStream.readLine();
            
            // Get the hash for the file
            int fileHash = Sha1Generator.generate_Sha1(filename);
            
            // Get the search table
            NodeSearchTable nst = NodeSearchTable.get_Instance();
            
            
            // Check if this is the correct server
            if (fileHash <= Integer.parseInt(nst.get_ownID()) &&
            		fileHash > Integer.parseInt(nst.get_predecessorID())) {
            	
            	outStream.println(ConnectionCodes.CORRECTPOSITION);
            	outStream.flush();
            	
            	FileObject filetoupload = (FileObject)ois.readObject();
            	
            	String username = filetoupload.getOwner();
            	
            	
            	// Check if file already exists
            	LocalPathList lpl = LocalPathList.get_Instance();
            	FileObject localFile = lpl.get_file(filename);
            	
            	boolean isPermitted = false;
            	
            	if (localFile == null) {
            		isPermitted = true;
            	}
            	else {
            		Vector<String> groups = UserManagement.get_Instance().get_GroupsForUser(username);
                	
                	if (username == localFile.getOwner()) {
                		isPermitted = true;
                	}
                	else if (groups.contains(localFile.getGroup())) {
                		if (localFile.getGroupPermision() >= 2 &&
                				localFile.getGroupPermision() != 4 &&
                				localFile.getGroupPermision() != 5) {
                			isPermitted = true;
                		}
                	}
                	else if (localFile.getGroupPermision() >= 2 && 
                			localFile.getGroupPermision() != 4 && 
                			localFile.getGroupPermision() != 5) {
                		isPermitted = true;
                	}
            	}
            	
            	if (!isPermitted) {
            		outStream.println(ConnectionCodes.NOTAUTHORIZED);
            	}
            	else {
            		outStream.println(ConnectionCodes.AUTHORIZED);
            		
            		File newUpload = new File(DistConfig.get_Instance().get_rootPath() + filename);
            		if (newUpload.exists()) {
            			newUpload.delete();
            		}
            		
            		int bytesRead = 0;
            		byte [] buffer = new byte[distConfig.getBufferSize()];
            		FileOutputStream fos = new FileOutputStream (distConfig.get_rootPath() + filetoupload.getName());
            		
            		do {
            			bytesRead = (Integer)ois.readObject();
            			buffer = (byte[])ois.readObject();
            			fos.write(buffer, 0, bytesRead);
            		} while (bytesRead == distConfig.getBufferSize());
            		
            		fos.close();
            		
            		outStream.write(ConnectionCodes.RECEIVEDFILE);
            		
            		lpl.add(filetoupload);
            	}
            	
            }
            
            // else locate next server to check
            else {
            	int nextCheckID = Integer.parseInt(nst.get_IDAt(0));
            	String nextCheckIP = nst.get_IPAt(0);
            	for (int index = 0; index < nst.size()-1; index++) {
            		// Get the next two IDs
            		int nextID = Integer.parseInt(nst.get_IDAt(index));
            		int secondID = Integer.parseInt(nst.get_IDAt(index + 1));
            		
            		// Check if the has is between the two IDs
            		if (fileHash >= nextID && fileHash < secondID) {
            			nextCheckID = nextID;
            			nextCheckIP = nst.get_IPAt(index);
            			continue;
            		}
            		else if (fileHash >= nextID && fileHash > secondID) {
            			nextCheckID = nextID;
            			nextCheckIP = nst.get_IPAt(index);
            			continue;
            		}
            		else if (fileHash <= nextID && fileHash < secondID) {
            			nextCheckID = nextID;
            			nextCheckIP = nst.get_IPAt(index);
            			continue;
            		}
            		
            		// If it wasn't between, set the next check ID to the second ID
        			nextCheckID = secondID;
        			nextCheckIP = nst.get_IPAt(index + 1);
            	}
            	
            	// Send the next location to look
            	outStream.println(ConnectionCodes.WRONGPOSITION);
            	outStream.println(Integer.toString(nextCheckID));
            	outStream.println(nextCheckIP);
            	outStream.flush();
            	
            	inStream.readLine();
            }
            
            ois.close();
            outStream.close();
            inStream.close();
            client.close();
		}
		
		catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
        }
		
	}
	
}