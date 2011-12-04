package distclient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;

import distconfig.ConnectionCodes;
import distconfig.DistConfig;
import distconfig.Sha1Generator;
import distfilelisting.FileObject;
import distnodelisting.NodeSearchTable;

public class ClntGetFile implements Runnable {
	private String host;
	private Client client;
	private String fileName;
	private boolean backup;
	private String username;
	private String wheretoput;
	private boolean success = false;

	public ClntGetFile(String host, Client client, String fileName, String username){
		this.host = host;
		this.client = client;
		this.fileName = fileName;
		this.backup = false;
		this.username = username;
	}
	
	private ClntGetFile(String host, Client client, String fileName, String username, boolean backup) {
		this.host = host;
		this.client = client;
		this.fileName = fileName;
		this.backup = backup;
		this.username = username;
	}
	
	private ClntGetFile (String host, Client client, String filename, String wheretoput, String username, boolean backup) {
		this.host = host;
		this.client = client;
		this.fileName = filename;
		this.backup = backup;
		this.username = username;
		this.wheretoput = wheretoput;
	}
	
	public ClntGetFile (Client client, String filename, String wheretoput, String username) {
		this.client = client;
		this.backup = false;
		this.username = username;
		this.fileName = filename;
		this.wheretoput = wheretoput;
		
		int hash = Sha1Generator.generate_Sha1(this.fileName);
		
		NodeSearchTable nst = NodeSearchTable.get_Instance();
		this.host = null;
		for (int index = 0; index < nst.size()-1; index++) {
			if (NodeSearchTable.is_between(
					hash,
					Integer.parseInt(nst.get_IDAt(index)), 
					Integer.parseInt(nst.get_IDAt(index + 1)))) {
				this.host = nst.get_IPAt(index);
				break;
			}
			else {
				this.host = nst.get_IPAt(index+1);
			}
		}
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
	        
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
	        
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
			        
			        if (Integer.parseInt(response) == ConnectionCodes.AUTHORIZED) {
			        	FileOutputStream fos = new FileOutputStream (distConfig.get_rootPath() + fileName);
			        	FileObject nfo = (FileObject)ois.readObject();
	            		
	            		// Download
			        	int bytesRead = 0;
	            		byte [] buffer = new byte[distConfig.getBufferSize()];
	            		do {
	            			bytesRead = (Integer)ois.readObject();
	            			buffer = (byte[])ois.readObject();
	            			fos.write(buffer, 0, bytesRead);
	            		} while (bytesRead == distConfig.getBufferSize());
	            		
	            		fos.close();
			        	
			        	System.out.println("Sending confirm");
			        	outStream.println(ConnectionCodes.GETFILE);
			        	
			        	success = true;
			        	
			        } else if (Integer.parseInt(response) == ConnectionCodes.NOTAUTHORIZED) {
			        }
		        	

		        } else if (Integer.parseInt(response) == ConnectionCodes.FILEDOESNTEXIST) {			        
			        if (!this.backup){
			        	response = in.readLine();
				        System.out.println("Received backup IP: " + response);
				        
				        sock.close();
				        ClntGetFile cgf = new ClntGetFile(response, this.client, fileName, this.username, true);
				        cgf.run();
				        success = cgf.isSuccess();
			        	
			        } else {
			        	response = in.readLine();
				        System.out.println("Received ack: " + response);
			        	
			        }
		        }
	        	
	        } else if (Integer.parseInt(response) == ConnectionCodes.WRONGPOSITION) {
		        String nextHost = in.readLine();
		        System.out.println("Received next Server IP: " + nextHost);
		        
		        sock.close();		        
		        ClntGetFile cgf = new ClntGetFile(nextHost, this.client, fileName, this.username);
		        cgf.run();
		        success = cgf.isSuccess();
	        	
	        }
	        
	        if (!sock.isClosed()) 
	        	sock.close();
	        return;
	        
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isSuccess() { return success; }

}
