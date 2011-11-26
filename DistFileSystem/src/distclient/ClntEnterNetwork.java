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
import distfilelisting.UserManagement;
import distnodelisting.NodeSearchTable;

public class ClntEnterNetwork implements Runnable {
	String host;
	int port;
	Client client;

	public ClntEnterNetwork(String host, Client client) {
		this(host, DistConfig.get_Instance().get_servPortNumber(), client);
	}

	public ClntEnterNetwork(String host, int port, Client client) {
		this.host = host;
		this.port = port;
		this.client = client;
	}
	
	@Override
	public void run() {
		try {
			System.out.println("Connecting");
			
			Socket sock = new Socket(host,port);
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
            outStream.println(ConnectionCodes.ENTERNETWORK);
            outStream.flush();
            
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
            System.out.println("Got Object InputStream");
            
            DistConfig sdc = (DistConfig) ois.readObject();
            DistConfig.set_Instance(sdc);
            System.out.println(sdc.get_CurrNodes());
            UserManagement remoteManage = (UserManagement) ois.readObject();
            UserManagement.set_Instance(remoteManage);
            
            outStream.println(ConnectionCodes.ENTERNETWORK);
            outStream.flush();
            
            int id = Integer.parseInt(in.readLine());
            client.setId(id);
            NodeSearchTable.get_Instance().set_OwnID(Integer.toString(id));
            System.out.println(id);
            
            outStream.println(ConnectionCodes.ENTERNETWORK);
            outStream.flush();
            
            int servId = Integer.parseInt(in.readLine());
            client.setServId(servId);
            
            String servIp = in.readLine();
            client.setServIp(servIp);
            
            System.out.println(servId);
            System.out.println(servIp);
            
            outStream.println(ConnectionCodes.ENTERNETWORK);
            outStream.flush();
            
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			
		}

	}

}
