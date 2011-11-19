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
import distconfig.DistConfig;
import distnodelisting.NodeSearchTable;


public class ServNodeDropped implements Runnable {

	private Socket client = null;
	private DistConfig distConfig = null;
	private NodeSearchTable nst = null;
	
	public ServNodeDropped (Socket cli) {
		this.client = cli;
	}
	
	@Override
	public void run() {
		distConfig = DistConfig.get_Instance();
		nst = NodeSearchTable.get_Instance();
		
		try {
	        // Get the input stream for the client
	        BufferedReader inStream = new BufferedReader (
	                new InputStreamReader(client.getInputStream()));
	        // Get the output stream for the client
	        BufferedOutputStream bos = new BufferedOutputStream (
	                client.getOutputStream());
	        // Setup the writer to the client
	        PrintWriter outStream = new PrintWriter(bos, false);
	        
	        // Send Confirmation
	        outStream.println(ConnectionCodes.NODEDROPPED);
	        outStream.flush();
	        
	        // Read in the ID and IP of the server that dropped
	        String dropID = inStream.readLine();
	        
	        // Send confirmation and close the connection
	        outStream.println(ConnectionCodes.NODEDROPPED);
	        
	        // Loop through the node search table and see if anything needs to be changed
	        boolean is_changed = false;
	        for (int index = nst.size()-1; index >= 0; index--) {
	        	if (dropID.equals(nst.get_IDAt(index))) {
	        		is_changed = true;
	        		if (index == nst.size()-1) {
	        			nst.set(index, nst.get_predecessorID(), nst.get_predecessorIPAddress());
	        		}
	        		else {
	        			nst.set(index, nst.get(index-1));
	        		}
	        	}
	        }
	        
	        // If this table was changed, send out a notification to the network,
	        // to setup the table again.
	        if (is_changed) {
	        	pushIDAndIP (nst.get_ownID(), nst.get_ownIPAddress(), nst.get_ownID());
	        }
	        
	        
		}
		catch (IOException ex) {
            Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	private void pushIDAndIP (String newID, String newIP, String myID) {
		try {
	    	// Setup the socket to the next node, and the write and read buffers
	    	Socket sock = new Socket(nst.get_IPAt(0), distConfig.get_servPortNumber());
	        sock.setSoTimeout(5000);
	        // write buffer
	        BufferedOutputStream bos = new BufferedOutputStream (sock.getOutputStream());
			PrintWriter outStream = new PrintWriter(bos, false);
			// read buffer
			BufferedReader inStream = new BufferedReader (
	                new InputStreamReader(sock.getInputStream()));
					
			// Sending connection code
			outStream.println(ConnectionCodes.NEWNODE);
			outStream.flush();
			
			// Receive Confirmation
			inStream.readLine();
			
			// Send newID and newIP
			outStream.println(newID);
			outStream.println(newIP);
			outStream.flush();
			
			// receive confirmation
			inStream.readLine();
			
			// Close the connection
			inStream.close();
			bos.close();
			outStream.close();
			sock.close();
		}
		
		catch (IOException ex) {
            Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
}

