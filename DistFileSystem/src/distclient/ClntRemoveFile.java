package distclient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import distconfig.ConnectionCodes;
import distconfig.DistConfig;

public class ClntRemoveFile implements Runnable {
	private String host;
	private Client client;
	private String fileName;
	private boolean backup;
	private String username;
	private boolean success = false; 

	public ClntRemoveFile(String host, Client client, String fileName, String username){
		this.host = host;
		this.client = client;
		this.fileName = fileName;
		this.backup = false;
		this.username = username;
	}
	
	private ClntRemoveFile(String host, Client client, String fileName, String username, boolean backup) {
		this.host = host;
		this.client = client;
		this.fileName = fileName;
		this.backup = backup;
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
	        outStream.println(ConnectionCodes.GETFILE);
	        outStream.flush();
	        
	        System.out.println("Getting Ack");
	        System.out.println(in.readLine());
	        
	        System.out.println("Sending filename as " + fileName);
	        outStream.println(fileName);
	        outStream.flush();
	        
	        String back = this.backup ? "1" : "0";
	        System.out.println("Sending backup as " + back);
	        outStream.println(back);
	        outStream.flush();
	        
	        String response = in.readLine();
	        System.out.println("Received " + response);
	        
	        if (Integer.parseInt(response) == ConnectionCodes.CORRECTPOSITION) {
	        	response = in.readLine();
		        System.out.println("Received " + response);
		        
		        if (Integer.parseInt(response) == ConnectionCodes.FILEEXISTS) {
		        	System.out.println("Sending username as " + username);
			        outStream.println(username);
			        outStream.flush();
			        
			        response = in.readLine();
			        System.out.println("Received " + response);
			        
			        success = true;
			        
			        if (Integer.parseInt(response) == ConnectionCodes.AUTHORIZED) {
			        	
			        } else if (Integer.parseInt(response) == ConnectionCodes.NOTAUTHORIZED) {
			        	
			        }
		        	
		        } else if (Integer.parseInt(response) == ConnectionCodes.FILEDOESNTEXIST) {
		        	response = in.readLine();
			        System.out.println("Received backup IP: " + response);
			        
			        if (this.backup){
			        	response = in.readLine();
				        System.out.println("Received backup IP: " + response);
				        
				        sock.close();
				        ClntRemoveFile crf = new ClntRemoveFile(response, this.client, this.fileName, this.username, true);
				        crf.run();
				        success = crf.isSuccess();
			        	
			        } else {
			        	response = in.readLine();
				        System.out.println("Received ack: " + response);
			        	
			        }

		        }
	        	
	        } else if (Integer.parseInt(response) == ConnectionCodes.WRONGPOSITION) {
		        String nextHost = in.readLine();
		        System.out.println("Received next Server IP: " + nextHost);
		        
		        sock.close();
		        client.addTask(new ClntRemoveFile(nextHost, this.client, this.fileName, this.username));
	        	
	        }
	        
	        if (!sock.isClosed()) 
	        	sock.close();
	        return;
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isSuccess() { return success; }

}
