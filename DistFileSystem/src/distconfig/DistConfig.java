/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package distconfig;

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
    
    private boolean USEFULLTABLE = false;
    private boolean USEFULLPATHTABLE = false;
    private int MAXNODES = 40;
    private int CURRNODES = 0;
    private int ServPortNumber = 8956;
    
    private DistConfig() {}
    
    public static DistConfig get_Instance () {
        if (distConf == null) {
            distConf = new DistConfig();
        }
        return distConf;
    }
    
    public int get_MaxNodes () {
        return this.MAXNODES;
    }
    
    public boolean get_UseFullTable () {
        return this.USEFULLTABLE;
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
    
    public boolean get_useFullPathTable() {
    	return this.USEFULLPATHTABLE;
    }
    
    public void set_useFullPathTable(boolean useFullTable) {
    	this.USEFULLPATHTABLE = useFullTable;
    }
    
}
