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
import distnodelisting.NodeSearchTable;


public class ServPredecessorDropped implements Runnable {
	
	private Socket client = null;
	private NodeSearchTable nst = null;
	
	public ServPredecessorDropped (Socket cli) {
		this.client = cli;
	}
	

	@Override
	public void run() {
		
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
	        outStream.println(ConnectionCodes.PREDDROPPED);
	        outStream.flush();
	        
	        // Read in new ID
	        String newPredID = inStream.readLine();
	        
	        // Set the new predecessor
	        nst.set_predicessor(newPredID, client.getInetAddress().toString());
	        
	        // Exchange Files
	        ServHeartBeat shb = new ServHeartBeat(this.client, true);
	        shb.runas_server(false);
	        shb.runas_client();
	        
	        // close the connections
	        inStream.close();
	        outStream.close();
	        bos.close();
	        this.client.close();
	        
		}
		catch (IOException ex) {
            Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
}