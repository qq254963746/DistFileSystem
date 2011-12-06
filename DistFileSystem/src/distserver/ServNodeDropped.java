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
import distfilelisting.UserManagement;
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
	
	/**
	 * Default to run as server
	 * @param cli : the socket to which the client is connected
	 */
	public ServNodeDropped (Socket cli) {
		this.client = cli;
	}
	
	/**
	 * Used to notify when a node has dropped
	 */
	@Override
	public void run() {
		this.runas_server();
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
	        String startingID = inStream.readLine();
	        
	        // Send confirmation and close the connection
	        outStream.println(ConnectionCodes.NODEDROPPED);
	        
	        // Close all the connections
	        outStream.close();
	        bos.close();
	        inStream.close();
	        client.close();
	        
	        boolean was_successor = false;
	        
	        // Clear the node search table for a clean start
	        for (int index = 1; index < nst.size(); index++) {
	        	nst.set(index, nst.get_ownID(), nst.get_ownIPAddress());
	        }
	        nst.set_predicessor(nst.get_ownID(), nst.get_ownIPAddress());
	        
	        if (startingID.equals(nst.get_ownID())) {
	        	was_successor = true;
	        }
	        
	        // If this table was changed, send out a notification to the network,
	        // to setup the table again.
	        if (!was_successor) {
	        	client = new Socket (nst.get_IPAt(0), distConfig.get_servPortNumber());
	        	client.setSoTimeout(5000);
	        	
	        	// If the connection completes, run the heart beat
				bos = new BufferedOutputStream (client.getOutputStream());
				outStream = new PrintWriter(bos, false);
				
				outStream.println(ConnectionCodes.NODEDROPPED);
				outStream.flush();
	        	
	        	this.runas_client(startingID);
	        }
        	send_newNodeNotification(nst.get_ownID(), nst.get_ownIPAddress());
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
	private void send_newNodeNotification (String newID, String newIP) {
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
			outStream.println(UserManagement.get_Instance().get_ownUserName());
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
	
	public void runas_client(String startingID) {
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
	        outStream.println(startingID);
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

