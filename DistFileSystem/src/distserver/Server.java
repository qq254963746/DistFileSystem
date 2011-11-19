/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package distserver;

import distconfig.ConnectionCodes;
import distconfig.DistConfig;
import distnodelisting.NodeSearchTable;
import distserver.ServCheckPosition;
import distserver.ServEnterNetwork;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author paul
 */
public class Server implements Runnable {

    private ServerSocket sock;
    
    private boolean running = true;
    private Vector<Thread> backgrounded = new Vector<Thread>();
    private DistConfig distConfig;
    
    public Server () {
        this.distConfig = DistConfig.get_Instance();
        
        try {
            this.sock = new ServerSocket(this.distConfig.get_servPortNumber());
            this.sock.setSoTimeout(distConfig.getServertimeout());
        }
        catch (IOException e) {
            // The socket could not be created
            System.out.println("Error: Couldn't create socket.");
            e.printStackTrace(System.err);
        }
    }
    
    @Override
    public void run() {
        Socket client = null;
        String line = null;
                
        while (this.running) {
            try {
                
                System.out.println("Waiting for connection");
                client = sock.accept();
                System.out.println("Connection from " + client.getInetAddress());
                
                BufferedReader in = new BufferedReader (
                        new InputStreamReader (
                                client.getInputStream()));
                
                System.out.println("Waiting for input");
                line = in.readLine();
                System.out.println("Read " + line);
                
                int code = Integer.parseInt(line);
                
                switch (code) {
                    // If this is a new client looking to join the network
                    case ConnectionCodes.ENTERNETWORK:
                        if (distConfig.get_CurrNodes() == distConfig.get_MaxNodes()) {
                            BufferedOutputStream bos = new BufferedOutputStream (
                                    client.getOutputStream());
                            // Setup the writer to the output stream
                            PrintWriter outStream = new PrintWriter(bos, false);
                            outStream.println(ConnectionCodes.NETWORKFULL);
                            outStream.close();
                            bos.close();
                            in.close();
                            client.close();
                        }
                        else {
                            // Setup the appropriate class
                            ServEnterNetwork dsen = 
                                    new ServEnterNetwork(client);
                            // Setup and start the thread, so it doesn't block
                            Thread enterDSEN = new Thread(dsen);
                            enterDSEN.start();
                            // Add the thread to those that have been backgrounded
                            this.backgrounded.add(enterDSEN);
                            enterDSEN = null;
                        }
                        break;
                    case ConnectionCodes.CHECKPOSITION:
                        // Setup the appropriate class
                        ServCheckPosition dscp = 
                                new ServCheckPosition(client);
                        // Setup and start the thread, so it doesn't block
                        Thread enterDSCP = new Thread (dscp);
                        enterDSCP.start();
                        this.backgrounded.add(enterDSCP);
                        enterDSCP = null;
                        break;
                    case ConnectionCodes.NEWPREDECESSOR:
                    	// Setup the appropriate class
                        ServNewPredecessor dsnp = 
                                new ServNewPredecessor(client);
                        // Setup and start the thread, so it doesn't block
                        Thread enterDSNP = new Thread (dsnp);
                        enterDSNP.start();
                        this.backgrounded.add(enterDSNP);
                        enterDSNP = null;
                        break;
                    case ConnectionCodes.NEWSUCCESSOR:
                    	// Setup the appropriate class
                        ServNewSuccessor dsns = 
                                new ServNewSuccessor(client);
                        // Setup and start the thread, so it doesn't block
                        Thread enterDSNS = new Thread (dsns);
                        enterDSNS.start();
                        this.backgrounded.add(enterDSNS);
                        enterDSNS = null;
                        break;
                    case ConnectionCodes.NEWNODE:
                    	// Setup the appropriate class
                        ServNewNode dsnn = new ServNewNode(client);
                        // Setup and start the thread, so it doesn't block
                        Thread enterDSNN = new Thread (dsnn);
                        enterDSNN.start();
                        this.backgrounded.add(enterDSNN);
                        enterDSNN = null;
                        break;
                    case ConnectionCodes.SETUPSEARCHTABLE:
                    	// Setup the appropriate class
                        ServSetupSearchTable dsst = 
                                new ServSetupSearchTable(client);
                        // Setup and start the thread, so it doesn't block
                        Thread enterDSST = new Thread (dsst);
                        enterDSST.start();
                        this.backgrounded.add(enterDSST);
                        enterDSST = null;
                    case ConnectionCodes.GETFILE:
                    	// Setup the appropriate class
                        ServGetFile dsgf = 
                                new ServGetFile(client);
                        // Setup and start the thread, so it doesn't block
                        Thread enterDSGF = new Thread (dsgf);
                        enterDSGF.start();
                        this.backgrounded.add(enterDSGF);
                        enterDSGF = null;
                    case ConnectionCodes.UPLOADFILE:
                    	// Setup the appropriate class
                        ServUploadFile dsuf = 
                                new ServUploadFile(client);
                        // Setup and start the thread, so it doesn't block
                        Thread enterDSUF = new Thread (dsuf);
                        enterDSUF.start();
                        this.backgrounded.add(enterDSUF);
                        enterDSUF = null;
                        break;
                    default:
                        NumberFormatException nfe =
                                new NumberFormatException();
                        throw nfe;
                }
                
                // Collect all finished threads
                for (int i = 0; i < this.backgrounded.size(); i++) {
                    if (this.backgrounded.elementAt(i) == null) {
                        continue;
                    }
                    Thread tmpThread = (Thread)this.backgrounded.elementAt(i);
                    if ( !tmpThread.isAlive() ) {
                        tmpThread.join();
                        this.backgrounded.remove(i);
                        i -= 1;
                    }
                }
            }
            
            catch (NumberFormatException e) {
                try {
                    // The line was not an integer, return error code
                    // Get the output stream for the client
                    BufferedOutputStream bos = new BufferedOutputStream (
                            client.getOutputStream());
                    PrintWriter outputStream = new PrintWriter(bos, false);
                    
                    // Write the error message to the client and flush
                    outputStream.println(ConnectionCodes.UNRECOGNIZEDCODE);
                    outputStream.println("Unrecognized Code: " + line);
                    outputStream.flush();
                    
                    // Close the connection
                    outputStream.close();
                    client.close();
                } catch (IOException ex) {
                    // Something went wrong replying to the client
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            catch (SocketTimeoutException ste) {
            	// The server socket connection timed out
            	// check to see if the successor is still there
            	// if it is than ask for any new files
            	// TODO: Add code to run the heartbeat and handle if it fails
            	System.out.println("Client is null");
            } 
            catch (Exception e) {
				e.printStackTrace();
			}
        }
    }
    
    
    public static void main (String[] args) {
        Server ds = new Server();
        NodeSearchTable dct = NodeSearchTable.get_Instance();
        
        dct.set_own("5", "5.5.5.5");
        dct.add("10", "10.10.10.10");
        dct.add("15", "15.15.15.15");
        dct.add("20", "20.20.20.20");
        dct.add("25", "25.25.25.25");
        dct.add("3", "3.3.3.3");
        dct.set_predicessor("0", "0.0.0.0");
        
        System.out.println("own = " + dct.get_ownID());
        System.out.println("pred = " + dct.get_predecessorID());
        for (int index = 0; index < dct.size(); index++) {
            System.out.println("index = " + index);
            System.out.println("val = " + dct.get_IDAt(index));
        }
        
        Thread th = new Thread(ds);
        th.start();
    }
}


