package distclient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;

import distconfig.ConnectionCodes;
import distconfig.DistConfig;

public class ClntCheckPosition implements Runnable {
	private String host;
	private Client client;
	private int id;
	
	public ClntCheckPosition(String host, Client client){
		this(host, client.getId(), client);
	}
	
	public ClntCheckPosition(String host, int id, Client client){
		this.host = host;
		this.client = client;
		this.id = id;
				
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
	        outStream.println(ConnectionCodes.CHECKPOSITION);
	        outStream.flush();
	        
	        ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
	        client.setOis(ois);
	        System.out.println("Got Object InputStream");
	        
	        System.out.println("Getting Ack");
	        System.out.println(in.readLine());
	        
	        System.out.println("Sending my ID as " + id);
	        outStream.println(Integer.toString(id));
	        outStream.flush();
	        
	        String tmpline = in.readLine();
	        
	        switch(Integer.parseInt(tmpline)) {
	        case ConnectionCodes.NEWID:
	            id = Integer.parseInt(in.readLine());
	            client.setId(id);
	            System.out.println("New ID = " + id);
	            tmpline = in.readLine();
	            break;
	        
	        case ConnectionCodes.CORRECTPOSITION:
	            String[] predecessor = (String[])ois.readObject();
	            System.out.println("Correct Position");
	            System.out.println("Pred ID = " + predecessor[0]);
	            System.out.println("Pred IP = " + predecessor[1]);
	            String[] successor = (String[])ois.readObject();
	            System.out.println("Next ID = " + successor[0]);
	            System.out.println("Next IP = " + successor[1]);
	            client.setPredecessor(predecessor);
	            client.setSuccessor(successor);
	            break;
	            
	        default:
	            int nextTestId = Integer.parseInt(in.readLine());
	            client.setNextTestId(nextTestId);
	            
	            String nextTestIp = in.readLine();
	            client.setNextTestIp(nextTestIp);
	            
	            System.out.println("Wrong Position");
	            System.out.println("next ID = " + nextTestId);
	            System.out.println("next IP = " + nextTestIp);
	        }
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
