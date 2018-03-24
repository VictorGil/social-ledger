package net.devaction.socialledger.ethereum.core.extradata;

import static org.ethereum.config.SystemProperties.EXTRA_DATA_CHARSET;

//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;

/**
 * @author Víctor Gil
 * 
 * since Sun 2018-Mar-18 
 */
public class ExtradataDepthProvider{
    private static final Log log = LogFactory.getLog(ExtradataDepthProvider.class);
    //public static final Charset EXTRA_DATA_CHARSET = StandardCharsets.US_ASCII;
    
    public static int provide(Block block, Blockchain blockchain){
        String extradataStr = new String(block.getExtraData(), EXTRA_DATA_CHARSET).trim();
       
        int depth = 0;
        Block parentBlock = null;
        
        do{
            parentBlock = blockchain.getBlockByHash(block.getParentHash());
            if (isExtraDataTheSame(extradataStr, parentBlock.getExtraData())){
                log.info("extra data depth of block " + block.getShortHash() + " is " + depth);
                return depth;
            }
            depth++;
        } while(!parentBlock.isGenesis());
        
        return depth;
    }
    
    static boolean isExtraDataTheSame(String blockExtradataStr, byte[] otherBlockExtradata){
        String otherBlockExtradataStr = new String(otherBlockExtradata, EXTRA_DATA_CHARSET).trim();                
        return blockExtradataStr.equalsIgnoreCase(otherBlockExtradataStr);
    }    
    
    static boolean isExtraDataTheSame(byte[] blockExtradata, byte[] otherBlockExtradata){
        String blockExtradataStr = new String(blockExtradata, EXTRA_DATA_CHARSET).trim();
        String otherBlockExtradataStr = new String(otherBlockExtradata, EXTRA_DATA_CHARSET).trim();
                
        return blockExtradataStr.equalsIgnoreCase(otherBlockExtradataStr);
    } 
}
