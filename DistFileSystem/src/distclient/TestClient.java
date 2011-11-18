/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package distclient;


/**
 *
 * @author paul
 */
public class TestClient extends Client {
	private final String serverHost = "127.0.0.1";
	
    public TestClient () {}
    
    public void DistTestServEnterNetwork () {
        super.addTask(new ClntEnterNetwork(serverHost, this));
    }
    
    
    public void DistTestServCheckPosition (int myId) {
    	super.addTask(new ClntCheckPosition(serverHost, myId, this));
        
    }
    
    public void DistTestServCheckPosition () {
    	super.addTask(new ClntCheckPosition(serverHost, this));
        
    }
    
    public static void main (String[] args) {
    	System.out.println("Hello");
        TestClient dtc = new TestClient();
        dtc.DistTestServEnterNetwork();
        dtc.DistTestServCheckPosition();
        dtc.DistTestServCheckPosition(28);
    }
    
}
