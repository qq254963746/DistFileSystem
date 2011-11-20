/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package distconfig;

/**
 * 
 * @author paul
 */
public class ConnectionCodes {

	// Connection Codes
	public static final int ENTERNETWORK = 1;
	public static final int NEWSUCCESSOR = 2;
	public static final int NEWPREDECESSOR = 3;
	public static final int UPLOADFILE = 4;
	public static final int RECEIVEDFILE = 5;
	public static final int UPDATETABLE = 6;
	public static final int CHECKPOSITION = 7;
	public static final int CORRECTPOSITION = 8;
	public static final int WRONGPOSITION = 9;
	public static final int NEWID = 10;
	public static final int NEWNODE = 11;
	public static final int SETUPSEARCHTABLE = 12;
	public static final int GETFILE = 13;
	public static final int FILEDOESNTEXIST = 14;
	public static final int FILEEXISTS = 15;
	public static final int NOTAUTHORIZED = 16;
	public static final int AUTHORIZED = 17;
	public static final int HEARTBEAT = 18;
	public static final int PREDDROPPED = 19;
	public static final int NODEDROPPED = 20;
	public static final int FORCEHEARTBEAT = 21;

	// Error Codes
	public static final int UNRECOGNIZEDCODE = 900;
	public static final int FAILEDTORECIEVE = 901;
	public static final int NETWORKFULL = 902;
}
