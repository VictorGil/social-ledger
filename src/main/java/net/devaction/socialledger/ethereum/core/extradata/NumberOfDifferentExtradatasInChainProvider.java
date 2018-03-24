package net.devaction.socialledger.ethereum.core.extradata;

import static org.ethereum.config.SystemProperties.EXTRA_DATA_CHARSET;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;

/**
 * @author Víctor Gil
 * 
 * since Sat 2018-Mar-24 
 */
public class NumberOfDifferentExtradatasInChainProvider{
    private static final Log log = LogFactory.getLog(NumberOfDifferentExtradatasInChainProvider.class);
    
    public static int provide(Block block, Blockchain blockchain){
        String extradataStr = new String(block.getExtraData(), EXTRA_DATA_CHARSET).trim();
        
        Set<String> set = new HashSet<String>();
        set.add(extradataStr.toLowerCase());

        Block parentBlock = null;       
        do{
            parentBlock = blockchain.getBlockByHash(block.getParentHash());
            String parentExtradataStr = new String(parentBlock.getExtraData());
            set.add(parentExtradataStr.toLowerCase());
        } while(!parentBlock.isGenesis());
        
        int numberOfDifferentExtradatasInChain = set.size();
        log.info("Number of different extra data values in the block: " + block.getShortDescr() + " chain is " + numberOfDifferentExtradatasInChain);
        return numberOfDifferentExtradatasInChain;
    }
}


