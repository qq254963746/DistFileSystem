/**
 * @author paul
 */

package distserver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
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

/**
 * This serves as the heartbeat and sink
 * @author paul
 */
public class ServHeartBeat implements Runnable {

	private boolean is_serv = false;
	private boolean is_predecessor = false;
	private Socket client = null;
	private DistConfig distConfig = null;
	private NodeSearchTable nst = null;
	private LocalPathList lpl = null;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private BufferedReader inStream;
	private PrintWriter outStream;
	
	/**
	 * 
	 * @param cli : The socket to which the client is connected
	 * @param is_server : Boolean, is this the server or not
	 */
	public ServHeartBeat (Socket cli, boolean is_server) {
		this.client = cli;
		this.is_serv = is_server;
		this.is_predecessor = false;
	}
	
	public ServHeartBeat (Socket cli, boolean is_server, boolean is_predecessor) {
		this.client = cli;
		this.is_serv = is_server;
		this.is_predecessor = is_predecessor;
	}
	
	@Override
	public void run() {
		if (is_serv)
			this.runas_server();
		
		else
			this.runas_client();
		
		try {
			this.client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This function will receive the heartbeat message,
	 * Send the set of file contained on this server
	 * Then send those necessary to be updated
	 */
	@SuppressWarnings("unchecked")
	public void runas_server () {
		System.out.println("Inside heartbeat server");
		// Setup global parameters
		this.distConfig = DistConfig.get_Instance();
		this.nst = NodeSearchTable.get_Instance();
		this.lpl = LocalPathList.get_Instance();
		
		try {
			// Get the input stream for the client
	        inStream = new BufferedReader (
	                new InputStreamReader(client.getInputStream()));
	        System.out.println("got instream in hb serv");
	        // Get the output stream for the client
	        BufferedOutputStream bos = new BufferedOutputStream (
	                client.getOutputStream());
	        // Setup the writer to the client
	        outStream = new PrintWriter(bos, false);
	        System.out.println("got outstream in hb serv");
        
	        // Setup the object writer to the client
	        oos = new ObjectOutputStream (bos);
	        System.out.println("got oos in hb serv");

	        // Send acknowledgment
	        System.out.println("Sending ack in hb serv");
	        outStream.println(ConnectionCodes.HEARTBEAT);
	        outStream.flush();
	        
	        // Setup the object input stream
	        ois = new ObjectInputStream (client.getInputStream());
	        System.out.println("got ois in hb serv");
			
			
	        // Get ID of the connecting server
	        System.out.println("Receiving ID of connecting serv in hb serv");
	        int prevID = Integer.parseInt(inStream.readLine());
	        
	        Vector<FileObject> filestosend = new Vector<FileObject>();
	        
	        // Send the list of files contained on this server
	        if (!is_predecessor) {
	        	filestosend = lpl.get_filesBetween(prevID, Integer.parseInt(nst.get_ownID()));
	        }
	        else {
	        	filestosend = lpl.get_filesBetween(Integer.parseInt(nst.get_ownID()), prevID);
	        }
	        System.out.println("Sending list of files in hb serv");
	        oos.writeObject(filestosend);
	        oos.flush();
	        
	        // Receive the updated vector of which files the client needs
	        System.out.println("Receiving the list of needed files in hb serv");
	        filestosend = (Vector<FileObject>)ois.readObject();
	        
	        String filename;
	        // Loop through all the files to send
	        System.out.println("looping through files in hb serv");
	        for (int index = 0; index < filestosend.size(); index++) {
	        	// First get and send the name of the file
	        	filename = filestosend.get(index).getName();
	        	System.out.printf("Sending name and file %s in hb serv\n", filename);
	        	outStream.println(filename);
	        	outStream.flush();
	        	
            	// Open the file and input stream to that file
            	File toTransfer = new File (distConfig.get_rootPath() + filename);
            	FileInputStream fis = new FileInputStream(toTransfer);
            	byte[] buffer = new byte[distConfig.getBufferSize()];
            	
            	Integer bytesRead = 0;
            	// Loop through the bytes of the file, reading them into the buffer
            	// and sending them to the new client
            	while ((bytesRead = fis.read(buffer)) > 0) {
            		oos.writeObject(bytesRead);
            		oos.flush();
            		oos.writeObject(Arrays.copyOf(buffer, buffer.length));
            		oos.flush();
            	}
            	
            	// Get confirmation for each file sent
            	inStream.readLine();
	        }
	        
	        System.out.println("Closing in hb serv");
		}
		
		catch (ClassNotFoundException ex) {
			Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (IOException ex) {
            Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	/**
	 * This function will send the heartbeat message,
	 * Receive the set of files contained on the next server
	 * Then receive those files necissary for updating
	 */
	@SuppressWarnings("unchecked")
	public void runas_client () {
		// Setup global parameters
		System.out.println("Inside client in hb client");
		this.distConfig = DistConfig.get_Instance();
		this.nst = NodeSearchTable.get_Instance();
		this.lpl = LocalPathList.get_Instance();
		
		try {
			// Get the input stream for the server
	        inStream = new BufferedReader (
	                new InputStreamReader(client.getInputStream()));
	        System.out.println("Got instream in hb client");
	        // Get the output stream for the server
	        BufferedOutputStream bos = new BufferedOutputStream (
	                client.getOutputStream());
	        // Setup the writer to the server
	        outStream = new PrintWriter(bos, false);
	        System.out.println("got outstream in hb client");

	        // Setup the object input stream
	        ois = new ObjectInputStream (client.getInputStream());
	        System.out.println("got ois in hb client");
			
	        // Receive acknowledgment and send ID
	        System.out.println("Getting ack in hb client");
	        inStream.readLine();

	        // Setup the object writer to the server
	        oos = new ObjectOutputStream (bos);
	        System.out.println("got oos in hb client");	
			
	        System.out.println("sending id in hb client");
	        outStream.println(nst.get_ownID());
	        outStream.flush();
	        
	        // Receive the list of files on the server
	        System.out.println("Getting files to receive in hb client");
	        Vector<FileObject> filesToReceive = (Vector<FileObject>)ois.readObject();
	        
	        // Get the list of files that need updating
	        Vector<FileObject> tmpFilesToReceive = new Vector<FileObject>();
	        FileObject tmpFO = null;
	        for (int index = 0; index < filesToReceive.size(); index++) {
	        	tmpFO = lpl.get_file(filesToReceive.get(index).getName());
	        	// If the file currently doesn't exist on this node
	        	if (tmpFO == null) {
	        		tmpFilesToReceive.add(filesToReceive.get(index));
	        	}
	        	// Or if it is out of date
	        	else if (tmpFO.getLastupdate().before(filesToReceive.get(index).getLastupdate())) {
	        		tmpFilesToReceive.add(filesToReceive.get(index));
	        	}
	        }
	        
	        // Send the needed files
	        System.out.println("Sending needed files in hb client");
	        oos.writeObject(tmpFilesToReceive);
	        oos.flush();
	        
	        // loop through the files to receive
	        System.out.println("looping through files in hb client");
	        for (int index = 0; index < tmpFilesToReceive.size(); index++) {
	        	// Get the file name
	        	String filename = inStream.readLine();
	        	System.out.printf("got filename %s and receiving in hb client\n", filename);
	        	
	        	// Open the file to write to it
	        	File toReceive = new File (distConfig.get_rootPath() + filename);
	        	FileOutputStream fos = new FileOutputStream(toReceive);
	        	byte[] buffer = new byte[distConfig.getBufferSize()];
	        	
	        	Integer bytesRead = 0;
	        	
	        	// loop to gather all bytes and output them to the file
	        	do {
	        		bytesRead = (Integer)ois.readObject();
	        		buffer = (byte[])ois.readObject();
	        		fos.write(buffer, 0, bytesRead);
	        	} while (bytesRead == distConfig.getBufferSize());
	        	
	        	// Update the lpl
	        	lpl.set_file(tmpFilesToReceive.get(index));
	        	
	        	// Send acknowledgment that the file was received
	        	outStream.println(ConnectionCodes.RECEIVEDFILE);
	        	outStream.flush();
	        }
	        
	        System.out.println("closing in hb client");
		}
		
		catch (ClassNotFoundException ex) {
			Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (IOException ex) {
            Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
}


