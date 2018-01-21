package net.devaction.socialledger.ethereum.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.ethereum.core.Block;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.ImportResult;
import org.ethereum.core.Repository;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

/**
 * @author Víctor Gil
 * Since Tue 16-Jan-2018  
 */
public class SocialLedgerManager{
    private static final Logger socialLedgerLogger = LoggerFactory.getLogger(SocialLedgerManager.class);
    private static SocialLedgerManager INSTANCE;
    public static final int TIME_SLOT_IN_SECS = 10;
    
    //Maybe there is always just one entry in this map at most but I am not sure
    //better to use a map so far, just in case
    private final Map<List<Byte>, WaitingCallable> blocksCallableMap = new HashMap<List<Byte>, WaitingCallable>();
    
    private BlockchainImpl blockchain;
    
    private SocialLedgerManager(BlockchainImpl blockchain){
        this.blockchain = blockchain;
    }
    
    public static SocialLedgerManager getInstance(BlockchainImpl blockchain){
        if (INSTANCE == null)
            INSTANCE = new SocialLedgerManager(blockchain);
        return INSTANCE;
    }    
    
    public ImportResult bestBlockWaitForEndOfTimeSlot(byte[] parentHashArray, 
        Block block, long parentTimestamp){
        
        //validate the block before sleeping/waiting
        Repository repo = blockchain.getRepository();
        if (!blockchain.isValid(repo, block)) {
            socialLedgerLogger.warn("Invalid block with number: {}", block.getNumber());
            return ImportResult.INVALID_BLOCK;
        }
        
        return blockWaitForEndOfTimeSlot(parentHashArray, block, parentTimestamp, true);
    }

    public ImportResult notBestBlockWaitForEndOfTimeSlot(byte[] parentHashArray, 
            Block block, long parentTimestamp){
        
        //validate the block before sleeping/waiting
        Block parentBlock = blockchain.getBlockByHash(block.getParentHash());
        Repository repo = blockchain.getRepository().getSnapshotTo(parentBlock.getStateRoot());
        if (!blockchain.isValid(repo, block)) {
            socialLedgerLogger.warn("Invalid block with number: {}", block.getNumber());
            return ImportResult.INVALID_BLOCK;
        }
        
        socialLedgerLogger.info("Block has been validated: " + block.getShortDescr() + ". Now we need to wait until " + 
            "the end of the time-slot");
        return blockWaitForEndOfTimeSlot(parentHashArray, block, parentTimestamp, false);
    }
    
    ImportResult blockWaitForEndOfTimeSlot(byte[] parentHashArray, 
        Block block, long parentTimestamp, boolean isBest){
        
        final List<Byte> parentHashBytesList = new ArrayList<Byte>();        
        for (byte bytePrimitive : parentHashArray){
            parentHashBytesList.add(bytePrimitive);
        }
        
        WaitingCallable waitingCallable;
        Future<Block> future;
        Block winnerBlock = null;
        
        if (blocksCallableMap.containsKey(parentHashBytesList)){
            socialLedgerLogger.info("There is a compiting block to be the chosen child of " + 
                ByteUtil.toHexString(parentHashArray) + ". Current block: " + 
                blocksCallableMap.get(parentHashBytesList).getBlock().getShortDescr() + 
                ". New (compiting) block: " + block.getShortDescr());     
            
            //so far we just assume that the new block is better than the current block
            //for the sake of simplicity
            blocksCallableMap.get(parentHashBytesList).setBlock(block);
            future =  blocksCallableMap.get(parentHashBytesList).getFuture();
        } else{
            waitingCallable = new WaitingCallable(parentHashArray, block, parentTimestamp);
            blocksCallableMap.put(parentHashBytesList, waitingCallable);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            future = executor.submit(waitingCallable);
            waitingCallable.setFuture(future);
            executor.shutdown();
        }
        
        try{
            winnerBlock = future.get();
        } catch(InterruptedException | ExecutionException ex){
            socialLedgerLogger.error(ex.toString(), ex);
        } finally{
            blocksCallableMap.remove(parentHashBytesList);
        }
        
        if (Arrays.equals(block.getHash(), winnerBlock.getHash())) {
            if (isBest){
                blockchain.processBest(block);
                return ImportResult.IMPORTED_BEST;
            } else{
                blockchain.processNotBest(block);
                return ImportResult.IMPORTED_NOT_BEST;
            }                
        }        
        //this is not really accurate, it would be better to return something 
        //such as "LOSER" as per the Social Ledger algorithm 
        return ImportResult.INVALID_BLOCK;
    }
    
    public static boolean needToWaitForEndOfTimeSlot(Block parentBlock){        
        long parentTimestampInMillis = parentBlock.getTimestamp() * 1000L;
        return needToWaitForEndOfTimeSlot(parentTimestampInMillis);       
    }
    
    public static boolean needToWaitForEndOfTimeSlot(long parentTimestampInMillis){
        long currentTimeInMillis = new Date().getTime();
        long timeSlotInMillis = TIME_SLOT_IN_SECS * 1000;
        //logger.trace("Parent block timestamp in milliseconds: " + parentTimestampInMillis + 
        //        ". Current time in milliseconds: " + currentTimeInMillis);
        
        if (currentTimeInMillis - parentTimestampInMillis < timeSlotInMillis)
            return true;
        return false;              
    }
}
