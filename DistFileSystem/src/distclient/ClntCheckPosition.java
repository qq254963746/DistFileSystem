package distclient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import distconfig.ConnectionCodes;
import distconfig.Constants;
import distconfig.DistConfig;
import distnodelisting.NodeSearchTable;

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
		this.id = Integer.parseInt(NodeSearchTable.get_Instance().get_ownID());
				
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
	        outStream.println(ConnectionCodes.CHECKPOSITION);
	        outStream.flush();
	        
	        System.out.println("Getting Ack");
	        System.out.println(in.readLine());
	        
	        System.out.println("Sending my ID as " + id);
	        outStream.println(Integer.toString(id));
	        outStream.flush();
	        
	        String tmpline = in.readLine();
	        System.out.println("Position Code " + tmpline);
	        
	        if (Integer.parseInt(tmpline) == ConnectionCodes.NEWID) {
	            id = Integer.parseInt(in.readLine());
	            NodeSearchTable.get_Instance().set_own(Integer.toString(id),
	            		NodeSearchTable.get_Instance().get_ownIPAddress());
	            client.setId(id);
	            System.out.println("New ID = " + id);
	            tmpline = in.readLine();
	            System.out.printf("Received Code %s\n", tmpline);
	        }
	        
	        if ( Integer.parseInt(tmpline) == ConnectionCodes.CORRECTPOSITION) {
	        	System.out.println("Getting the predicessor ID");
	            //String[] predecessor = (String[])ois.readObject();
	        	String[] predecessor = new String[2];
	        	predecessor[Constants.ID] = in.readLine();
	        	predecessor[Constants.IP_ADDRESS] = in.readLine();
	            System.out.println("Correct Position");
	            System.out.println("Pred ID = " + predecessor[Constants.ID]);
	            System.out.println("Pred IP = " + predecessor[Constants.IP_ADDRESS]);
	            //String[] successor = (String[])ois.readObject();
	            String[] successor = new String[2];
	        	successor[Constants.ID] = in.readLine();
	        	successor[Constants.IP_ADDRESS] = in.readLine();
	            System.out.println("Next ID = " + successor[Constants.ID]);
	            System.out.println("Next IP = " + successor[Constants.IP_ADDRESS]);
	            client.setPredecessor(predecessor);
	            client.setSuccessor(successor);
	            sock.close();
	        } else {
	        	System.out.println("Wrong Position");
	            int nextTestId = Integer.parseInt(in.readLine());
	            String nextTestIp = in.readLine();
	            
	            System.out.println("Wrong Position");
	            System.out.println("next ID = " + nextTestId);
	            
	            System.out.println("next IP = " + nextTestIp);
	            sock.close();
	            ClntCheckPosition ccp = new ClntCheckPosition (nextTestIp, client);
	            ccp.run();
	            ccp = null;
	        }
		} catch (IOException e) {
			e.printStackTrace();
		} 
		//catch (ClassNotFoundException e) {
		//	e.printStackTrace();
		//}
	}

}
