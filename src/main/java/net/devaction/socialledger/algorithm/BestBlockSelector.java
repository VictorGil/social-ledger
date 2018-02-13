package net.devaction.socialledger.algorithm;

import org.ethereum.core.Block;

/**
 * @author Víctor Gil
 * 
 * Since 23-JAN-2018 
 *  */
public interface BestBlockSelector{
    
    //so far this is the only implementation 
    //we can change it in the future by changing just this line
    //and then the clients code is not impacted
    static final BestBlockSelector INSTANCE = new DummieBestBlockSelector();
    
    public static BestBlockSelector getInstance(){
        return INSTANCE;        
    } 
    
    public BestBlock select(Block block1, Block block2);
}
