package net.devaction.socialledger.ethereum.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.ethereum.core.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Víctor Gil
 * 
 */
public class WaitingCallable implements Callable<Block>{
    private static final Logger logger = LoggerFactory.getLogger(WaitingCallable.class);
    
    private final List<Byte> parentHash;
    private Block block;
    private final long parentTimestamp;
    //this is the very same Future object which we get 
    //when submitting this Callable for execution
    private Future<Block> future;
    //private final BlockchainImpl blockchain;
    
    public WaitingCallable(byte[] parentHashArray, Block block, long parentTimestamp){
        this.block = block;
        
        parentHash = new ArrayList<Byte>();        
        for (byte bytePrimitive : parentHashArray){
            parentHash.add(bytePrimitive);
        }
        
        this.parentTimestamp = parentTimestamp;
    }
    
    public void setBlock(Block block){
        this.block = block;
    }
    
    public Block getBlock(){
        return block;
    }

    public void setFuture(Future<Block> future){
        this.future = future;
    }

    public Future<Block> getFuture(){
        return future;
    }
    
    @Override
    public Block call() throws Exception{
        while (SocialLedgerManager.needToWaitForEndOfTimeSlot(parentTimestamp * 1000)){
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException ex) {
                logger.error(ex.toString(), ex);
            }
        }
        return block;
    }
    
    @Override
    public int hashCode(){
        return parentHash.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WaitingCallable other = (WaitingCallable) obj;
        if (parentHash == null){
            if (other.parentHash != null)
                return false;
        } else if (!parentHash.equals(other.parentHash))
            return false;
        return true;
    }
}
