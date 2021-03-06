/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package distnodelisting;

import java.util.Vector;


/**
 *
 * @author paul
 */
public class GlobalNodeTable extends Vector<Object> {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static GlobalNodeTable DGT = null;
    
    private GlobalNodeTable () {
        super();
    }
    
    public static GlobalNodeTable get_instance () {
        if (GlobalNodeTable.DGT == null) {
            return new GlobalNodeTable();
        }
        else {
            return GlobalNodeTable.DGT;
        }
    }
    
    public boolean add(String ID, String ipAddress) {
        String[] idip = {ID, ipAddress};
        return super.add(idip);
    }
    
    public Object set(int index, String ID, String ipAddress) {
        String[] idip = { ID, ipAddress };
        return super.set(index, idip);
    }
    
    public String get_IDAt(int index) {
        String[] idip = (String[]) super.get(index);
        return idip[0];
    }
    
    public String get_IPAt(int index) {
        String[] idip = (String[]) super.get(index);
        return idip[1];
    }
    
    public Object remove_NodeAt(int index) {
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
