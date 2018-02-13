package net.devaction.socialledger.algorithm;

import org.ethereum.core.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static net.devaction.socialledger.algorithm.BestBlock.BLOCK1;

/**
 * @author Víctor Gil
 * 
 * Since 23-JAN-2018 
 *  */
public class DummieBestBlockSelector implements BestBlockSelector{

    private static final Logger socialLedgerLogger = LoggerFactory.getLogger(DummieBestBlockSelector.class);
    
    @Override
    public BestBlock select(final Block block1, final Block block2){
        byte[] hash1 = block1.getHash();
        byte[] hash2 = block2.getHash();
        final BestBlock bestBlockEnum = select(hash1, hash2);
        
        final Block theBestBlock;
        if (bestBlockEnum == BLOCK1)
            theBestBlock = block1;
        else 
            theBestBlock = block2;        
        socialLedgerLogger.info("Comparing " + block1.getShortDescr() + " block and "
                + block2.getShortDescr() + " block, " + theBestBlock.getShortDescr() + " is the best block");
        
        return bestBlockEnum;
    }
    
    //false means hash1 is the chosen one
    //true means hash2 is the chosen one
    BestBlock select(final byte[] hash1, final byte[] hash2){
        for (int i = 0; i < hash1.length; i++){
            int int1 = Byte.toUnsignedInt(hash1[i]);
            int int2 = Byte.toUnsignedInt(hash2[i]);
            
            if (int1 > int2)
                return BestBlock.BLOCK1;
            if (int2 > int1)
                return BestBlock.BLOCK2;
        }
        String errMessage = "Both hashes are equal!";
        socialLedgerLogger.error(errMessage);
        throw new IllegalArgumentException(errMessage);        
    }    
}
