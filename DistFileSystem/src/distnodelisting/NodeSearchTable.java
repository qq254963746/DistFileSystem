/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package distnodelisting;

import java.util.Vector;

import distconfig.Constants;

/**
 *
 * @author paul
 */
public class NodeSearchTable extends Vector<String[]> {
    
    /**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	private static NodeSearchTable dctInstance = null;
    private String[] predecessor = null;
    private String[] own = null;
    
    
    private NodeSearchTable () {}
    
    public static NodeSearchTable get_Instance () {
        if (dctInstance == null) {
            dctInstance = new NodeSearchTable();
        }
        return dctInstance;
    }
    
    public void set_own(String id, String ipAddress) {
        String[] tmp = {id, ipAddress};
        this.own = tmp;
    }
    
    public String get_ownID () {
        return this.own[Constants.ID];
    }
    
    public String get_ownIPAddress () {
        return this.own[Constants.IP_ADDRESS];
    }
    
    public void set_predicessor(String id, String ipAddress) {
        String[] temp = {id, ipAddress};
        this.predecessor = temp;
    }
    
    public String get_predecessorID () {
        return this.predecessor[Constants.ID];
    }
    
    public String get_predecessorIPAddress () {
        return this.predecessor[Constants.IP_ADDRESS];
    }
    
    public void add(String id, String ipAddress) {
        String[] idip = {id, ipAddress};
        super.add(idip);
    }
    
    public void set(int index, String id, String ipAddress) {
        String[] idip = { id, ipAddress };
        super.set(index, idip);
    }
    
    public String get_IDAt(int index) {
        String[] idip = (String[]) super.get(index);
        return idip[Constants.ID];
    }
    
    public String get_IPAt(int index) {
        String[] idip = (String[]) super.get(index);
        return idip[Constants.IP_ADDRESS];
    }
    
    public Object remove_NodeAt (int index) {
        return super.remove(index);
    }
    
    public Object remove_Node (String[] node) {
        return super.remove(node);
    }
    
    public boolean contains_ID (int id) {
        return this.contains_ID(Integer.toString(Constants.ID));
    }
    
    public boolean contains_ID (String id) {
        for (int index = 0; index < this.size(); index++) {
            if (this.get_IDAt(index).compareTo(id) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean contains_IP (String ip) {
        for (int index = 0; index < this.size(); index++) {
            if (this.get_IPAt(index).compareTo(ip) == 0) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean is_between (int newID, int prevID, int nextID) {
    	if ((prevID < newID && newID < nextID) || 
                (nextID < prevID && prevID < newID) ||
                (newID < nextID && nextID < prevID)) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
}
