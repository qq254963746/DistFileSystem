/**
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

public class ServNewNode implements Runnable {

	private Socket client = null;
	private NodeSearchTable nst = null;
	
	public ServNewNode (Socket cli) {
		this.client = cli;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
        
		try {
	        // Get the input stream for the client
	        BufferedReader inStream = new BufferedReader (
	                new InputStreamReader(client.getInputStream()));
	        // Get the output stream for the client
	        BufferedOutputStream bos = new BufferedOutputStream (
	                client.getOutputStream());
	        // Setup the writer to the client
	        PrintWriter outStream = new PrintWriter(bos, false);
	        
	        // Send confirmation
	        outStream.println(ConnectionCodes.NEWNODE);
	        outStream.flush();
	        
	        // Receive new ID and IP
	        nst = NodeSearchTable.get_Instance();
	        int newID = Integer.parseInt(inStream.readLine());
	        String newIP = inStream.readLine();
	        
	        // Send received confirmation
	        outStream.println(ConnectionCodes.NEWNODE);
	        outStream.flush();
	        
	        bos.close();
	        outStream.close();
	        inStream.close();
	        client.close();
	        
	        int myID = Integer.parseInt(nst.get_ownID());
	        if (!(newID == myID)) {
	        	this.pushNewIDAndIP(newID, newIP, myID);
	        	this.sendOwnInfo(newIP);
	        }
		}
		
		catch (IOException ex) {
            Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	private void pushNewIDAndIP (int newID, String newIP, int myID) {
		try {
			if (newID < myID && newID > Integer.parseInt(nst.get_predecessorID())) {
	    		nst.set_predicessor(Integer.toString(newID), newIP);
	    	}
	    	
	    	int maxNodes = DistConfig.get_Instance().get_MaxNodes();
	    	for (int index = 0; index < nst.size(); index++) {
	    		int potID = (int) ((myID + Math.pow(2, index)) % maxNodes);
	    		int currSearchID = Integer.parseInt(nst.get_IDAt(index));
	    		
	    		if (newID > currSearchID && newID < potID) {
	    			nst.set(index, Integer.toString(newID), newIP);
	    		}
	    	}
	    	
	    	Socket sock = new Socket(nst.get_IPAt(0), DistConfig.get_Instance().get_servPortNumber());
	        
	        sock.setSoTimeout(5000);
	        BufferedOutputStream bos = new BufferedOutputStream (sock.getOutputStream());
			PrintWriter outStream = new PrintWriter(bos, false);
			
			BufferedReader inStream = new BufferedReader (
	                new InputStreamReader(sock.getInputStream()));
					
			// Sending connection code
			outStream.println(ConnectionCodes.NEWNODE);
			outStream.flush();
			
			// Receive Confirmation
			inStream.readLine();
			
			// Send newID and newIP
			outStream.println(Integer.toString(newID));
			outStream.println(newIP);
			outStream.flush();
			
			// receive confirmation
			inStream.readLine();
			
			inStream.close();
			bos.close();
			outStream.close();
			sock.close();
		}
		
		catch (IOException ex) {
            Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	private void sendOwnInfo(String newIP) {
		try {
			Socket sock = new Socket(newIP, DistConfig.get_Instance().get_servPortNumber());
	        
	        sock.setSoTimeout(5000);
	        BufferedOutputStream bos = new BufferedOutputStream (sock.getOutputStream());
			PrintWriter outStream = new PrintWriter(bos, false);
			
			BufferedReader inStream = new BufferedReader (
	                new InputStreamReader(sock.getInputStream()));
					
			// Sending connection code
			outStream.println(ConnectionCodes.SETUPSEARCHTABLE);
			outStream.flush();
			
			// Receive Confirmation
			inStream.readLine();
			
			// Send newID and newIP
			outStream.println(nst.get_ownID());
			outStream.println(nst.get_ownIPAddress());
			outStream.flush();
			
			// receive confirmation
			inStream.readLine();
			
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