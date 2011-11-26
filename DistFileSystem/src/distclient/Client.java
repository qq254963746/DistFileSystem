package distclient;

import distnodelisting.NodeSearchTable;


public class Client {
	
	private final TaskManager tm = new TaskManager();
	
	private int id;
	private int servId;
    private String servIp;
    private String[] predecessor;
    private String[] successor;
    
    public Client() {
    	
    }
	
	public int getId() {
		return Integer.parseInt(NodeSearchTable.get_Instance().get_ownID());
	}
	public void setId(int id) {
		NodeSearchTable.get_Instance().set_OwnID(Integer.toString(id));
	}
	public int getServId() {
		return servId;
	}
	public void setServId(int servId) {
		this.servId = servId;
	}
	public String getServIp() {
		return servIp;
	}
	public void setServIp(String servIp) {
		this.servIp = servIp;
	}
	public String[] getPredecessor() {
		return predecessor;
	}
	public void setPredecessor(String[] predecessor) {
		this.predecessor = predecessor;
	}
	public String[] getSuccessor() {
		return successor;
	}
	public void setSuccessor(String[] successor) {
		this.successor = successor;
	}
	public void addTask(Runnable task) {
		tm.addTask(task);
	}

}
