package distclient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class Client {
	
	private final TaskManager tm = new TaskManager();
	
	private BufferedReader in;
	private BufferedOutputStream bos;
	private PrintWriter outStream;
	private Socket sock;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private int id;
	private int servId;
    private String servIp;
    private String[] predecessor;
    private String[] successor;
	
	public BufferedReader getIn() {
		return in;
	}
	public void setIn(BufferedReader in) {
		this.in = in;
	}
	public BufferedOutputStream getBos() {
		return bos;
	}
	public void setBos(BufferedOutputStream bos) {
		this.bos = bos;
	}
	public PrintWriter getOutStream() {
		return outStream;
	}
	public void setOutStream(PrintWriter outStream) {
		this.outStream = outStream;
	}
	public Socket getSock() {
		return sock;
	}
	public void setSock(Socket sock) {
		this.sock = sock;
	}
	public ObjectInputStream getOis() {
		return ois;
	}
	public void setOis(ObjectInputStream ois) {
		this.ois = ois;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public ObjectOutputStream getOos() {
		return oos;
	}
	public void setOos(ObjectOutputStream oos) {
		this.oos = oos;
	}
	

}
