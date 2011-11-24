package distclient;

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

import distconfig.ConnectionCodes;
import distconfig.DistConfig;
import distfilelisting.FileObject;

public class ClntUploadFile implements Runnable {
	private String host;
	private Client client;
	private FileObject file;
	private String username;

	public ClntUploadFile(String host, Client client, FileObject file, String username){
		this.host = host;
		this.client = client;
		this.file = file;
		this.username = username;
	}

	@Override
	public void run() {
		try {
	
	    	DistConfig distConfig = DistConfig.get_Instance();
	    	
			System.out.println("Connecting");
			Socket sock = new Socket(host, distConfig.get_servPortNumber());
	
	        sock.setSoTimeout(5000);
	        System.out.println("Connected");
	        
	        BufferedOutputStream bos = new BufferedOutputStream (
	                                sock.getOutputStream());
	        System.out.println("Got OutputStream");
	        PrintWriter outStream = new PrintWriter(bos, false);
	        System.out.println("Got PrintWriter");
	
	        BufferedReader in = new BufferedReader (
	                    new InputStreamReader (
	                            sock.getInputStream()));
	        System.out.println("Got InputStream");
	        
	        
	        System.out.println("Sending Code");
	        outStream.println(ConnectionCodes.UPLOADFILE);
	        outStream.flush();
	        
	        ObjectOutputStream oos = new ObjectOutputStream(bos);
	        
	        System.out.println("Getting Ack");
	        System.out.println(in.readLine());
	        
	        System.out.println("Sending filename as " + this.file.getName());
	        outStream.println(this.file.getName());
	        outStream.flush();
	        
	        String response = in.readLine();
	        System.out.println("Received " + response);
	        
	        if (response.equals(ConnectionCodes.CORRECTPOSITION)) {
	        	System.out.println("Sending username as " + this.username);
		        outStream.println(this.username);
		        outStream.flush();
		        
		        oos.writeObject(this.file);
		        
		        response = in.readLine();
		        System.out.println("Received " + response);
		        
		        if (response.equals(ConnectionCodes.AUTHORIZED)) {

		        	String fullPathName = distConfig.get_rootPath() + this.file.getName();
		        	
		        	File toTransfer = new File (fullPathName);
		        	
                    FileInputStream fis = new FileInputStream(toTransfer);
                    byte[] buffer = new byte[distConfig.getBufferSize()];
                    
                    // Send each set of bytes over
                    Integer bytesRead = 0;
                    while ((bytesRead = fis.read(buffer)) > 0) {
                    	oos.writeObject(bytesRead);
                    	oos.writeObject(Arrays.copyOf(buffer, buffer.length));
                    	oos.flush();
                    }
		        	
		        	System.out.println("Received " + in.readLine());
		        	return;
			        	
			    } else if (response.equals(ConnectionCodes.NOTAUTHORIZED)) {
			    	return;
			    }
	        	
	        } else if (response.equals(ConnectionCodes.WRONGPOSITION)) {
	        	int nextId = Integer.parseInt(in.readLine());
		        System.out.println("Received next ID: " + nextId);
		        
		        String nextHost = in.readLine();
		        System.out.println("Received next Server IP: " + nextHost);
		        
		        client.addTask(new ClntUploadFile(nextHost, this.client, this.file, this.username));
		        return;
	        	
	        }
	        
	        return;
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
