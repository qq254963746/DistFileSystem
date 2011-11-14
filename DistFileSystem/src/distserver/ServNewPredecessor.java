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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import distconfig.ConnectionCodes;
import distconfig.DistConfig;
import distfilelisting.FileObject;
import distfilelisting.LocalPathList;
import distnodelisting.NodeSearchTable;




public class ServNewPredecessor implements Runnable {
	
	private Socket client = null;
	private DistConfig distConfig = null;
	
	public ServNewPredecessor (Socket cli) {
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
            outStream.println(ConnectionCodes.NEWPREDECESSOR);
            outStream.flush();
            
            // Get the ID of the new Predecessor
            int newPredID = Integer.parseInt(inStream.readLine());
            
            Vector<FileObject> filesToTransfer = LocalPathList.get_Instance().get_filesBetween(
            		newPredID, 
            		Integer.parseInt(NodeSearchTable.get_Instance().get_ownID()));
            
            oos.writeObject(filesToTransfer);
            oos.flush();
            
            // Wait for acknowledgment
            inStream.readLine();
            
            // Send each file individually
            for (int index = 0; index < filesToTransfer.size(); index++) {
            	String fileName = filesToTransfer.get(index).getName();
            	
            	File toTransfer = new File (fileName);
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
            
            // Remove the files from the directory and list
            LocalPathList lpl = LocalPathList.get_Instance();
            for (int index = 0; index < filesToTransfer.size(); index++) {
            	String fileName = filesToTransfer.get(index).getName();
            	File toDelete = new File (fileName);
            	toDelete.delete();
            	lpl.remove(filesToTransfer.get(index));
            }
            
            // Add the new predecessor to search list
            NodeSearchTable nst = NodeSearchTable.get_Instance();
            nst.set_predicessor(Integer.toString(newPredID), client.getInetAddress().toString());
            
            // Send confirmation
            outStream.println(ConnectionCodes.NEWPREDECESSOR);
            outStream.flush();
            
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