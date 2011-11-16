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


class ServSetupSearchTable implements Runnable {
	
	private Socket client = null;

	public ServSetupSearchTable (Socket cli) {
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
	        
	        outStream.println(ConnectionCodes.SETUPSEARCHTABLE);
	        outStream.flush();
	        
	        int newID = Integer.parseInt(inStream.readLine());
	        String newIP = inStream.readLine();
	        
	        NodeSearchTable nst = NodeSearchTable.get_Instance();
	        
	        int myID = Integer.parseInt(nst.get_ownID());
	        
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
	    	
	    	outStream.println(ConnectionCodes.SETUPSEARCHTABLE);
	    	
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