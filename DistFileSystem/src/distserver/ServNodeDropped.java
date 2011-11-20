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

/**
 * Used when a node has dropped
 * @author paul
 *
 */
public class ServNodeDropped implements Runnable {

	private Socket client = null;
	private DistConfig distConfig = null;
	private NodeSearchTable nst = null;
	private boolean is_server = true;
	
	
	/**
	 * Default to run as server
	 * @param cli : the socket to which the client is connected
	 */
	public ServNodeDropped (Socket cli) {
		this(cli, true);
	}
	
	/**
	 * 
	 * @param cli : The socket to which the client is connected
	 * @param is_serv : True if this is being run as the server
	 */
	public ServNodeDropped (Socket cli, boolean is_serv) {
		this.client = cli;
		this.is_server = is_serv;
	}
	
	/**
	 * Used to notify when a node has dropped
	 */
	@Override
	public void run() {
		if (this.is_server) {
			this.runas_server();
		}
		else {
			this.runas_client();
		}
	}
		
	private void runas_server() {
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
	        
	        // Decrement the current nodes
	        distConfig.decrement_CurrNodes();
	        // Send Confirmation
	        outStream.println(ConnectionCodes.NODEDROPPED);
	        outStream.flush();
	        
	        // Read in the ID and IP of the server that dropped
	        String dropID = inStream.readLine();
	        
	        // Send confirmation and close the connection
	        outStream.println(ConnectionCodes.NODEDROPPED);
	        
	        // Close all the connections
	        outStream.close();
	        bos.close();
	        inStream.close();
	        client.close();
	        
	        // Loop through the node search table and see if anything needs to be changed
	        boolean is_changed = false;
	        boolean was_successor = false;
	        for (int index = nst.size()-1; index >= 0; index--) {
	        	if (dropID.equals(nst.get_IDAt(index))) {
	        		is_changed = true;
	        		if (index == nst.size()-1) {
	        			nst.set(index, nst.get_predecessorID(), nst.get_predecessorIPAddress());
	        		}
	        		else {
	        			nst.set(index, nst.get(index-1));
	        		}
	        		if (index == 0) {
	        			was_successor = true;
	        		}
	        	}
	        }
	        
	        // If this table was changed, send out a notification to the network,
	        // to setup the table again.
	        if (is_changed && !was_successor) {
	        	pushIDAndIP (nst.get_ownID(), nst.get_ownIPAddress(), nst.get_ownID());
	        }
		}
		catch (IOException ex) {
            Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	/**
	 * When the node search table has been changed, the node pretends to be new.
	 * It then sends out a message to everyone to figure out where everyone is.
	 * @param newID : The ID of this node
	 * @param newIP : The IP of this node
	 * @param myID : The ID of this node
	 */
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
	
	private void runas_client() {
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
	        
	        // Receive Confirmation
	        inStream.readLine();
	        
	        // Send the ID of the server that dropped
	        outStream.println(nst.get_IDAt(0));
	        outStream.flush();
	        
	        // Read confirmation
	        inStream.readLine();
	        	        
	        // Close all the connections
	        outStream.close();
	        bos.close();
	        inStream.close();
	        client.close();
		}
		catch (IOException ex) {
            Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
}

