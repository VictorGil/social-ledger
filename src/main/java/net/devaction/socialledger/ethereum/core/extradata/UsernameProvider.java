package net.devaction.socialledger.ethereum.core.extradata;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Víctor Gil
 * 
 * since Sun 2018-Mar-18 
 */
public class UsernameProvider{
    private static final Log log = LogFactory.getLog(UsernameProvider.class);
    public static final Charset EXTRA_DATA_CHARSET = StandardCharsets.US_ASCII;
    
    public static String provide(byte[] extradata){
        String extradataStr = new String(extradata, EXTRA_DATA_CHARSET).trim();
        
        return provide(extradataStr);
    } 
    
    static String provide(String extradataStr){
        return extradataStr.substring(7);
    }
}


