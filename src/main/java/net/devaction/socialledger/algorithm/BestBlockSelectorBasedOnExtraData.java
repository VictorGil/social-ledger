package net.devaction.socialledger.algorithm;

import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.devaction.socialledger.ethereum.core.extradata.ExtradataDepthProvider;

import static net.devaction.socialledger.algorithm.BestBlock.BLOCK1;
import static net.devaction.socialledger.algorithm.BestBlock.BLOCK2;

/**
 * @author Víctor Gil
 * 
 * since Sun 4-MAR-2018 
 */
public class BestBlockSelectorBasedOnExtraData{
    private static final Logger socialLedgerLogger = LoggerFactory.getLogger(BestBlockSelectorBasedOnExtraData.class);

    public static BestBlock select(final Block block1, final Block block2, Blockchain blockchain){
        int block1ExtradataDepth = ExtradataDepthProvider.provide(block1, blockchain);
        int block2ExtradataDepth = ExtradataDepthProvider.provide(block2, blockchain);
        
        if (block1ExtradataDepth > block2ExtradataDepth) {
            socialLedgerLogger.info("The extradata of block: " + block1.getShortDescr() + " (" + block1ExtradataDepth + 
                    ") is deeper than the extradata of block: " + block2.getShortDescr() + " (" + block2ExtradataDepth + ")");
            return BLOCK1;
        }
        if (block2ExtradataDepth > block1ExtradataDepth) {
            socialLedgerLogger.info("The extradata of block: " + block2.getShortDescr() + " (" + block2ExtradataDepth + 
                    ") is deeper than the extradata of block: " + block1.getShortDescr() + " (" + block1ExtradataDepth + ")");
            return BLOCK2;
        }
        
        socialLedgerLogger.info("The extradata of block: " + block1.getShortDescr() + " (" + block1ExtradataDepth + 
                ") is equally deep as the extradata of block: " + block2.getShortDescr() + " (" + block2ExtradataDepth + ")");
        socialLedgerLogger.info("Going to decide based on a dummy selector based on the block hashcodes");
        return DummieBestBlockSelector.select(block1, block2);
    }
}
