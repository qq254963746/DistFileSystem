/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package distconfig;

import java.util.Vector;

/**
 *
 * @author paul
 */
public class DistConnectionTable extends Vector<Object> {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static DistConnectionTable dctInstance = null;
    private String[] predecessor = null;
    private String[] own = null;
    
    private DistConnectionTable () {}
    
    public static DistConnectionTable get_Instance () {
        if (dctInstance == null) {
            dctInstance = new DistConnectionTable();
        }
        return dctInstance;
    }
    
    public void set_own(String ID, String ipAddress) {
        String[] tmp = {ID, ipAddress};
        this.own = tmp;
    }
    
    public String get_ownID () {
        return this.own[0];
    }
    
    public String get_ownIPAddress () {
        return this.own[1];
    }
    
    public void set_predicessor(String ID, String ipAddress) {
        String[] temp = {ID, ipAddress};
        this.predecessor = temp;
    }
    
    public String get_predecessorID () {
        return this.predecessor[0];
    }
    
    public String get_predecessorIPAddress () {
        return this.predecessor[1];
    }
    
    public void add(String ID, String ipAddress) {
        String[] idip = {ID, ipAddress};
        super.add(idip);
    }
    
    public void set(int index, String ID, String ipAddress) {
        String[] idip = { ID, ipAddress };
        super.set(index, idip);
    }
    
    public String get_IDAt(int index) {
        String[] idip = (String[]) super.get(index);
        return idip[0];
    }
    
    public String get_IPAt(int index) {
        String[] idip = (String[]) super.get(index);
        return idip[1];
    }
    
    public Object remove_NodeAt (int index) {
        return super.remove(index);
    }
    
    public Object remove_Node (String[] node) {
        return super.remove(node);
    }
    
    public boolean contains_ID (int ID) {
        return this.contains_ID(Integer.toString(ID));
    }
    
    public boolean contains_ID (String ID) {
        for (int index = 0; index < this.size(); index++) {
            if (this.get_IDAt(index).compareTo(ID) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean contains_IP (String IP) {
        for (int index = 0; index < this.size(); index++) {
            if (this.get_IPAt(index).compareTo(IP) == 0) {
                return true;
            }
        }
        return false;
    }
    
    
}
