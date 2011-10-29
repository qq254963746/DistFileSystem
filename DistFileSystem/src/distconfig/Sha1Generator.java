/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package distconfig;

import distconfig.DistConfig;
import distserver.ServEnterNetwork;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author paul
 */
public class Sha1Generator {
    
    public static int generate_Sha1 (String value) {
        
        int val = -1;
        
        DistConfig dc = DistConfig.get_Instance();
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.reset();
            md.update(value.getBytes());
            byte[] hash = md.digest();
            
            BigInteger bigInt = new BigInteger(hash);
            val = bigInt.mod(BigInteger.valueOf(dc.get_MaxNodes())).intValue();
        }
        catch (NoSuchAlgorithmException nsae) {
            Logger.getLogger(
                    ServEnterNetwork.class.getName()).log(
                    Level.SEVERE, null, nsae);
        }
        
        return val;
    }
    
    public static void main (String[] args) {
        int val = Sha1Generator.generate_Sha1("192.168.1.100");
        
        System.out.println(Integer.toString(val) + " 192.168.1.100");
        
        String[] tmp = {Integer.toString(val), "192.168.1.100"};
        System.out.println(tmp);
    }
    
}
