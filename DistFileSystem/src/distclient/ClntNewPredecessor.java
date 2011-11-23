package distclient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

import distconfig.ConnectionCodes;
import distconfig.Constants;
import distconfig.DistConfig;
import distfilelisting.FileObject;
import distfilelisting.LocalPathList;

public class ClntNewPredecessor implements Runnable {
	private String host;
	private Client client;
	private int id;
	
	public ClntNewPredecessor(Client client){
		this(client.getSuccessor()[Constants.IP_ADDRESS], client);
	}
	
	public ClntNewPredecessor(String host, Client client){
		this.host = host;
		this.client = client;
	}

	@Override
	public void run() {
		try {
	
	    	DistConfig distConfig = DistConfig.get_Instance();
	    	
			System.out.println("Connecting");
			Socket sock = new Socket(host, distConfig.get_servPortNumber());
	        client.setSock(sock);
	
	        sock.setSoTimeout(5000);
	        System.out.println("Connected");
	        
	        BufferedOutputStream bos = new BufferedOutputStream (
	                                sock.getOutputStream());
	        client.setBos(bos);
	        System.out.println("Got OutputStream");
	        PrintWriter outStream = new PrintWriter(bos, false);
	        client.setOutStream(outStream);
	        System.out.println("Got PrintWriter");
	
	        BufferedReader in = new BufferedReader (
	                    new InputStreamReader (
	                            sock.getInputStream()));
	        client.setIn(in);
	        System.out.println("Got InputStream");
	        
	
	        
	        System.out.println("Sending Code");
	        outStream.println(ConnectionCodes.NEWSUCCESSOR);
	        outStream.flush();
	        
	        ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
	        client.setOis(ois);
	        System.out.println("Got Object InputStream");
	        
	        System.out.println("Getting Ack");
	        System.out.println(in.readLine());
	        
	        System.out.println("Sending my ID as " + client.getId());
	        outStream.println(Integer.toString(id));
	        outStream.flush();
	        
	        Vector<FileObject> vfo = (Vector<FileObject>) ois.readObject();
	        
	        System.out.println("Sending confirm");
	        outStream.println(ConnectionCodes.NEWSUCCESSOR);
	        outStream.flush();        
	        
	        for (FileObject f : vfo) {
	        	   
	        	FileOutputStream fos;
	          	byte [] buffer = new byte[distConfig.getBufferSize()];  
	        	      
	            fos = new FileOutputStream(distConfig.get_rootPath() + f.getName());
	        
	            Integer bytesRead = 0;  
	      
	            do {        
	                bytesRead = (Integer)ois.readObject(); 	      
	                buffer = (byte[])ois.readObject();  
	      
	                fos.write(buffer, 0, bytesRead);  
	            } while (bytesRead == distConfig.getBufferSize());  
	      
	            fos.close();
		        LocalPathList.get_Instance().add(f);
	            
	            System.out.println("Sending file receipt");
		        outStream.println(ConnectionCodes.NEWSUCCESSOR);
		        outStream.flush();
	        }
	        

	        System.out.println("Getting Ack");
	        System.out.println(in.readLine());
	        
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
