package net.devaction.socialledger.ethereum.core.extradata;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;

/**
 * @author Víctor Gil
 * 
 * since Sun 2018-Mar-18 
 */
public class BasicExtradataValidator{
    private static final Log log = LogFactory.getLog(BasicExtradataValidator.class);
    public static final Charset EXTRA_DATA_CHARSET = StandardCharsets.US_ASCII;
    public static final String TWITTER = "twitter";
    
    public static boolean validate(Block block, Blockchain blockchain){
        String extradataStr = new String(block.getExtraData(), EXTRA_DATA_CHARSET).trim();
        
        if (extradataStr.length() < 11){
            log.info("extra data is too short: " + extradataStr);
            return false;
        }
        
        if (!extradataStr.substring(0, 7).equalsIgnoreCase(TWITTER)){
            log.info("Extradata does not start with " + TWITTER + 
                    ". Currently is the only social network supported, but more will come.");
            return false;
        }
        
        return true;
    }
}
