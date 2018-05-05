package net.devaction.socialledger.ethereum.core.extradata;

import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Víctor Gil
 * 
 * since Sun 2018-Apr-08 
 */
public class ExtradataStringProvider {

    private static final Log log = LogFactory.getLog(ExtradataStringProvider.class);
    
    public static void main(String[] args){
        String hexString = args[0];
        log.info("Input Hex String: " + hexString);
        
        byte[] bytes = hexStringToByteArray(hexString);
        String extradataStr = new String(bytes, StandardCharsets.US_ASCII).trim();
        
        log.info("Extrada ASCII String: " + extradataStr);        
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}

