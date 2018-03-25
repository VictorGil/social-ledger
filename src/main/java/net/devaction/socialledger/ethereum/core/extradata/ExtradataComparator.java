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
public class ExtradataComparator{
    private static final Log log = LogFactory.getLog(ExtradataComparator.class);
    public static final Charset EXTRA_DATA_CHARSET = StandardCharsets.US_ASCII;
    
    public static boolean areEqual(byte[] extradata1, byte[] extradata2){
        String extradataStr1 = new String(extradata1, EXTRA_DATA_CHARSET).trim();
        String extradataStr2 = new String(extradata2, EXTRA_DATA_CHARSET).trim();
        
        return extradataStr1.toLowerCase().equals(extradataStr2.toLowerCase());
    }   
}
