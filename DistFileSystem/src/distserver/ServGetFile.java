/**
*/

package distserver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import distconfig.ConnectionCodes;
import distconfig.DistConfig;
import distconfig.Sha1Generator;
import distnodelisting.NodeSearchTable;

class ServGetFile implements Runnable {

	private Socket client = null;
	private DistConfig distConfig;
	
	public ServGetFile (Socket cli) {
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
            // Get the output stream for the client
            BufferedOutputStream bos = new BufferedOutputStream (
                    client.getOutputStream());
            // Setup the writer to the client
            PrintWriter outStream = new PrintWriter(bos, false);
            // Setup the object writer to the client
            ObjectOutputStream oos = new ObjectOutputStream (bos);
            
            // Send acknowledgment everything is set
            outStream.println(ConnectionCodes.GETFILE);
            outStream.flush();
            
            // Get the file name
            String filename = inStream.readLine();
            
            // Get the hash for the file
            int fileHash = Sha1Generator.generate_Sha1(filename);
            
            // Get the search table
            NodeSearchTable nst = NodeSearchTable.get_Instance();

            // Check if this is the correct server
            if (fileHash < Integer.parseInt(nst.get_ownID()) &&
            		fileHash > Integer.parseInt(nst.get_predecessorID())) {
            	
            	outStream.println(ConnectionCodes.CORRECTPOSITION);
            	
            	// Send each file individually
                String fullPathName = distConfig.get_rootPath() + filename;
                	
                File toTransfer = new File (fullPathName);
                FileInputStream fis = new FileInputStream(toTransfer);
                byte[] buffer = new byte[distConfig.getBufferSize()];
                
                Integer bytesRead = 0;
                while ((bytesRead = fis.read(buffer)) > 0) {
                	oos.writeObject(bytesRead);
                	oos.writeObject(Arrays.copyOf(buffer, buffer.length));
                	oos.flush();
                }
                
                // Get confirmation that it made it
                inStream.readLine();
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
            
                        
            oos.close();
            outStream.close();
            inStream.close();
            client.close();
		}
		
		catch (IOException ex) {
            Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
        }
		
	}
	
}