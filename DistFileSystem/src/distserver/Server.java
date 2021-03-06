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
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.acl.LastOwnerException;
import java.util.Timer;
import java.util.TimerTask;
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
    private Timer timer = null;
    
    public Server () {
        this.distConfig = DistConfig.get_Instance();
        
        try {
            this.sock = new ServerSocket(this.distConfig.get_servPortNumber());
            this.timer = new Timer();
            this.setTimerTask();
            //this.sock.setSoTimeout(distConfig.getServertimeout());
        }
        catch (IOException e) {
            // The socket could not be created
            System.out.println("Error: Couldn't create socket.");
            e.printStackTrace(System.err);
        }
    }
    
    public void stop_Server() {
    	try {
			this.sock.close();
			this.running = false;
			
			while (this.backgrounded.size() > 0) {
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
			
		} 
    	catch (InterruptedException ex) {
    		ex.printStackTrace();
    	}
    	catch (IOException ex) {
			ex.printStackTrace();
		}
    }
    
    @Override
    public void run() {
        Socket client = null;
        String line = null;
                
        while (this.running) {
            try {
            	//this.sock.setSoTimeout(distConfig.getServertimeout());
                System.out.println("Waiting for connection");
                client = sock.accept();
                System.out.println("Connection from " + client.getInetAddress().getHostAddress());
                
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
                        break;
                    case ConnectionCodes.GETFILE:
                    	// Setup the appropriate class
                        ServGetFile dsgf = 
                                new ServGetFile(client);
                        // Setup and start the thread, so it doesn't block
                        Thread enterDSGF = new Thread (dsgf);
                        enterDSGF.start();
                        this.backgrounded.add(enterDSGF);
                        enterDSGF = null;
                        break;
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
                    case ConnectionCodes.FORCEHEARTBEAT:
                    	throw new SocketTimeoutException();
				case ConnectionCodes.HEARTBEAT:
                    	// Setup the appropriate class
                        ServHeartBeat dshb = 
                                new ServHeartBeat(client, true);
                        // Setup and start the thread, so it doesn't block
                        Thread enterDSHB = new Thread (dshb);
                        enterDSHB.start();
                        this.backgrounded.add(enterDSHB);
                        enterDSHB = null;
                        break;
                    case ConnectionCodes.REVERSEHEARTBEAT:
                    	ServHeartBeat drhb = 
                    		new ServHeartBeat (client, true, true);
                    	Thread enterDRHB = new Thread (drhb);
                    	enterDRHB.start();
                    	this.backgrounded.add(enterDRHB);
                    	enterDRHB = null;
                    	break;
                    case ConnectionCodes.PREDDROPPED:
                    	// Setup the appropriate class
                        ServPredecessorDropped dspd = 
                                new ServPredecessorDropped(client, false);
                        // Setup and start the thread, so it doesn't block
                        Thread enterDSPD = new Thread (dspd);
                        enterDSPD.start();
                        this.backgrounded.add(enterDSPD);
                        enterDSPD = null;
                        break;
                    case ConnectionCodes.NODEDROPPED:
                    	// Setup the appropriate class
                        ServNodeDropped dsnd = 
                                new ServNodeDropped(client);
                        // Setup and start the thread, so it doesn't block
                        Thread enterDSND = new Thread (dsnd);
                        enterDSND.start();
                        this.backgrounded.add(enterDSND);
                        enterDSND = null;
                        break;
                    case ConnectionCodes.USERMANAGEMENT:
                    	// Setup the appropriate class
                        ServUserManagement dsud = 
                                new ServUserManagement(client);
                        // Setup and start the thread, so it doesn't block
                        Thread enterDSUD = new Thread (dsud);
                        enterDSUD.start();
                        this.backgrounded.add(enterDSUD);
                        enterDSUD = null;
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
                	System.out.println("Unknown number exception");
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
            
            // Check if the successor is still alive after a given amount of time
            catch (SocketTimeoutException ste) {
            	// The server socket connection timed out
            	// check to see if the successor is still there
            	// if it is than ask for any new files
            	NodeSearchTable nst = NodeSearchTable.get_Instance();
            	Socket sock;
            	DistConfig distConfig = DistConfig.get_Instance();
				try {
					// Attempt to establish a connection
					sock = new Socket(nst.get_IPAt(0), distConfig.get_servPortNumber());
					sock.setSoTimeout(5000);
					
					// Get the output stream for the server
			        BufferedOutputStream bos = new BufferedOutputStream (
			                sock.getOutputStream());
			        // Setup the writer to the server
			        PrintWriter outStream = new PrintWriter(bos, false);
					
			        // Send code for which server to start
			        outStream.println(ConnectionCodes.HEARTBEAT);
					outStream.flush();
					
					// Setup the new thread and start
					// the heartbeat thread in the background
					ServHeartBeat dshb = new ServHeartBeat(sock, false);
					dshb.run();
                    
				}
				catch (ConnectException e) {
					Socket newpred;
					try {
						System.out.println("Inside initial catch");
						// Send out message that predecessor failed
						String failedip = nst.get_IPAt(0);
						String nextip = nst.get_IPAt(1);
						String nextid = nst.get_IDAt(1);
						
						for (int index = 0; index < nst.size(); index++) {
							if (!failedip.equals(nst.get_IPAt(index)) &&
									!nst.get_IPAt(index).equals(nst.get_ownIPAddress())) {
								nextip = nst.get_IPAt(index);
								nextid = nst.get_IDAt(index);
								break;
							}
						}
						
						if (nextip.equals(failedip)) {
							nextip = nst.get_predecessorIPAddress();
							nextid = nst.get_predecessorID();
						}
						
						if (nextip.equals(failedip)) {
							LastOwnerException loe = new LastOwnerException();
							throw loe;
						}
						
						newpred = new Socket(nextip, distConfig.get_servPortNumber());
						newpred.setSoTimeout(5000);
						
						// If the connection completes, run the heart beat
						BufferedOutputStream bos = new BufferedOutputStream (newpred.getOutputStream());
						PrintWriter outStream = new PrintWriter(bos, false);
						
						outStream.println(ConnectionCodes.PREDDROPPED);
						outStream.flush();
						
						// Setup the new thread and start
						// transferring in the background
						ServPredecessorDropped dspd =
								new ServPredecessorDropped(newpred, true);
						dspd.run();
						//Thread enterDSPD = new Thread(dspd);
						//enterDSPD.start();
						//this.backgrounded.add(enterDSPD);
						//enterDSPD = null;
						
						outStream.close();
						bos.close();
						
						nst.set(0, nextid, nextip);
						
						//
						// Send out signal that the node has failed
						//
						Socket nodefail = new Socket(nextip, distConfig.get_servPortNumber());
						nodefail.setSoTimeout(5000);
						
						// If the connection completes, run the heart beat
						bos = new BufferedOutputStream (nodefail.getOutputStream());
						outStream = new PrintWriter(bos, false);
						
						outStream.println(ConnectionCodes.NODEDROPPED);
						outStream.flush();
						
						// Setup the new thread and start
						// transferring the information for the node that dropped
						ServNodeDropped dsnd =
								new ServNodeDropped(nodefail);
						dsnd.runas_client(nst.get_ownID());
						//Thread enterDSND = new Thread(dsnd);
						//enterDSND.start();
						//this.backgrounded.add(enterDSND);
						//enterDSND = null;
						
					}
					catch (LastOwnerException loe) {
						System.out.println("Last node in network");
						for (int index = 0; index < nst.size(); index++) {
							nst.set(index, nst.get_ownID(), nst.get_ownIPAddress());
						}
						nst.set_predicessor(nst.get_ownID(), nst.get_ownIPAddress());
					}
					catch (Exception we) {
						we.printStackTrace();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
            } 
            catch (Exception e) {
				e.printStackTrace();
			}
        }
    }
    
    private void setTimerTask () {
    	timer.scheduleAtFixedRate(new TimerTask() {
    		public void run() {
    			// The server socket connection timed out
            	// check to see if the successor is still there
            	// if it is than ask for any new files
            	NodeSearchTable nst = NodeSearchTable.get_Instance();
            	Socket sock;
            	DistConfig distConfig = DistConfig.get_Instance();
				try {
					// Attempt to establish a connection
					sock = new Socket(nst.get_IPAt(0), distConfig.get_servPortNumber());
					sock.setSoTimeout(5000);
					
					// Get the output stream for the server
			        BufferedOutputStream bos = new BufferedOutputStream (
			                sock.getOutputStream());
			        // Setup the writer to the server
			        PrintWriter outStream = new PrintWriter(bos, false);
					
			        // Send code for which server to start
			        outStream.println(ConnectionCodes.HEARTBEAT);
					outStream.flush();
					
					// Setup the new thread and start
					// the heartbeat thread in the background
					ServHeartBeat dshb = new ServHeartBeat(sock, false);
					dshb.run();
                    
				}
				catch (ConnectException e) {
					Socket newpred;
					try {
						System.out.println("Inside initial catch");
						// Send out message that predecessor failed
						String failedip = nst.get_IPAt(0);
						String nextip = nst.get_IPAt(1);
						String nextid = nst.get_IDAt(1);
						
						for (int index = 0; index < nst.size(); index++) {
							if (!failedip.equals(nst.get_IPAt(index)) &&
									!nst.get_IPAt(index).equals(nst.get_ownIPAddress())) {
								nextip = nst.get_IPAt(index);
								nextid = nst.get_IDAt(index);
								break;
							}
						}
						
						if (nextip.equals(failedip)) {
							nextip = nst.get_predecessorIPAddress();
							nextid = nst.get_predecessorID();
						}
						
						if (nextip.equals(failedip)) {
							LastOwnerException loe = new LastOwnerException();
							throw loe;
						}
						
						nst.set(0, nextid, nextip);
						
						newpred = new Socket(nextip, distConfig.get_servPortNumber());
						newpred.setSoTimeout(5000);
						
						// If the connection completes, run the heart beat
						BufferedOutputStream bos = new BufferedOutputStream (newpred.getOutputStream());
						PrintWriter outStream = new PrintWriter(bos, false);
						
						outStream.println(ConnectionCodes.PREDDROPPED);
						outStream.flush();
						
						// Setup the new thread and start
						// transferring in the background
						ServPredecessorDropped dspd =
								new ServPredecessorDropped(newpred, true);
						dspd.run();
						//Thread enterDSPD = new Thread(dspd);
						//enterDSPD.start();
						//this.backgrounded.add(enterDSPD);
						//enterDSPD = null;
						
						outStream.close();
						bos.close();
						
						nst.set(0, nextid, nextip);
						
						//
						// Send out signal that the node has failed
						//
						Socket nodefail = new Socket(nextip, distConfig.get_servPortNumber());
						nodefail.setSoTimeout(5000);
						
						// If the connection completes, run the heart beat
						bos = new BufferedOutputStream (nodefail.getOutputStream());
						outStream = new PrintWriter(bos, false);
						
						outStream.println(ConnectionCodes.NODEDROPPED);
						outStream.flush();
						
						// Setup the new thread and start
						// transferring the information for the node that dropped
						ServNodeDropped dsnd =
								new ServNodeDropped(nodefail);
						dsnd.runas_client(nst.get_ownID());
						//Thread enterDSND = new Thread(dsnd);
						//enterDSND.start();
						//this.backgrounded.add(enterDSND);
						//enterDSND = null;
						
					}
					catch (LastOwnerException loe) {
						System.out.println("Last node in network");
						for (int index = 0; index < nst.size(); index++) {
							nst.set(index, nst.get_ownID(), nst.get_ownIPAddress());
						}
						nst.set_predicessor(nst.get_ownID(), nst.get_ownIPAddress());
					}
					catch (Exception we) {
						we.printStackTrace();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
    		}
    	}, distConfig.getServertimeout(), distConfig.getServertimeout());
    }
    
    public void runHeartBeat() {
    	
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


