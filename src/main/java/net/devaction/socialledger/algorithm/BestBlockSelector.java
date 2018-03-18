package net.devaction.socialledger.algorithm;

import org.ethereum.core.Block;

/**
 * @author Víctor Gil
 * 
 * since Sun 4-MAR-2018 
 */
public interface BestBlockSelector{
    
    public BestBlock select(Block block1, Block block2);
}
