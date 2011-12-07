/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package distconfig;

import java.io.File;
import java.io.Serializable;

/**
 *
 * @author paul
 */
public class DistConfig implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static DistConfig distConf = null;
    
    private boolean useGlobalPathTable = false;
    private boolean useGlobalNodeTable = false;
    
    private int MAXNODES = 40;
    private int CURRNODES = 0;
    private int ServPortNumber = 8956;
    
    private String rootpath = null;
    private int bufferSize = 1024;
    //private int servertimeout = 300000; //Default 5 min
    // For testing purposes
    private int servertimeout = 10000;
    
    private DistConfig() {
    	this.rootpath = System.getProperty("user.dir");
    	
    	File home = new File(this.rootpath + System.getProperty("file.separator") + "home");
    	if (!home.exists()) {
			boolean success = home.mkdir();
			if (!success) {
				System.exit(-1);
			}
    	}
    	
    	this.rootpath = this.rootpath +  System.getProperty("file.separator") + "home";
    	
    	this.CURRNODES = 1;
    }
    
    public static DistConfig get_Instance () {
        if (distConf == null) {
            distConf = new DistConfig();
        }
        
        return distConf;
    }
    
    public static DistConfig set_Instance(DistConfig distconfiguration) {
    	distConf = distconfiguration;
    	return distConf;
    }
    
    public int get_MaxNodes () {
        return this.MAXNODES;
    }
    
    public boolean get_UseGlobalNodeTable () {
        return this.useGlobalNodeTable;
    }
    
    public int get_CurrNodes () {
        return this.CURRNODES;
    }
    
    public int increment_CurrNodes() {
        this.CURRNODES++;
        return this.CURRNODES;
    }
    
    public int decrement_CurrNodes() {
        this.CURRNODES--;
        return this.CURRNODES;
    }
    
    public int get_servPortNumber() {
        return this.ServPortNumber;
    }
    
    public int set_servPortNumber(int port) {
        this.ServPortNumber = port;
        return port;
    }
    
    public boolean get_useGlobalPathTable() {
    	return this.useGlobalPathTable;
    }
    
    public String get_rootPath () {
    	return this.rootpath + System.getProperty("file.separator");
    }

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * @return the servertimeout
	 */
	public int getServertimeout() {
		return servertimeout;
	}

	/**
	 * @param servertimeout the servertimeout to set
	 */
	public void setServertimeout(int servertimeout) {
		this.servertimeout = servertimeout;
	}
    
}
