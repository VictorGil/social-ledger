package net.devaction.socialledger.ethereum.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.ethereum.config.SystemProperties.EXTRA_DATA_CHARSET; 

/**
 * @author Víctor Gil
 * 
 * Since 15-JAN-2018 
 *  */
//this class is not used yet 
public class Util{
    private static final Logger socialLedgerLogger = LoggerFactory.getLogger(Util.class);
    
    static boolean isExtraDataTheSame(byte[] blockExtraData, byte[] otherBlockExtraData){
        String blockExtraDataStr = new String(blockExtraData, EXTRA_DATA_CHARSET).trim();
        String mineExtraDataStr = new String(otherBlockExtraData, EXTRA_DATA_CHARSET).trim();
                
        return isExtraDataTheSame(blockExtraDataStr, mineExtraDataStr);
    }
    
    static boolean isExtraDataTheSame(String blockExtraData, String otherBlockExtraData){
        if (!blockExtraData.substring(8).equalsIgnoreCase(otherBlockExtraData.substring(8)))
            return false;
        if (blockExtraData.substring(0, 7).equalsIgnoreCase("twitter") && 
                blockExtraData.substring(0, 7).equalsIgnoreCase(otherBlockExtraData.substring(0, 7)))
            return true;        
        return blockExtraData.substring(0, 8).equalsIgnoreCase(otherBlockExtraData.substring(0, 8));
    }
}
