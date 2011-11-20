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

public class ClntNewNode implements Runnable {
	private String host;
	private Client client;
	private int id;
	
	public ClntNewNode(Client client){
		this(client.getSuccessor()[Constants.IP_ADDRESS], client);
	}
	
	public ClntNewNode(String host, Client client){
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
	        outStream.println(ConnectionCodes.NEWNODE);
	        outStream.flush();
	        
	        System.out.println("Getting Ack");
	        System.out.println(in.readLine());
	        
	        System.out.println("Sending my ID as " + id);
	        outStream.println(Integer.toString(id));
	        
	        String ip = sock.getInetAddress().getHostAddress();
	        System.out.println("Sending my IP as " + ip);
	        outStream.println(ip);
	        outStream.flush();
	        
	        System.out.println("Getting Ack");
	        System.out.println(in.readLine());
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
