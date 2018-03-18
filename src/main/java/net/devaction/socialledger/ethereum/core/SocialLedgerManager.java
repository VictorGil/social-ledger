package net.devaction.socialledger.ethereum.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.ImportResult;
import org.ethereum.core.Repository;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.devaction.socialledger.algorithm.BestBlock;
import net.devaction.socialledger.algorithm.BestBlockSelector;
import net.devaction.socialledger.algorithm.DummieBestBlockSelector;
import net.devaction.socialledger.ethereum.core.extradata.BasicExtradataValidator;
import net.devaction.socialledger.ethereum.core.extradata.UsernameProvider;
import net.devaction.socialledger.validatorusingtwitter.validate.HashcodeValidator;
import net.devaction.socialledger.validatorusingtwitter.validate.TwitterUserValidator;

import java.util.concurrent.Future;

import static net.devaction.socialledger.algorithm.BestBlock.BLOCK2;

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
    private final Map<List<Byte>, WaitingCallable> blocksCallableMap = new ConcurrentHashMap<List<Byte>, WaitingCallable>();
    
    private final BlockchainImpl blockchain;
    
    private volatile long firstBlockMinedByUsTimestamp = -1L;
    
    private final TwitterUserValidator twitterUserValidator;
    private final HashcodeValidator hashcodeVerifier; 
    
    private SocialLedgerManager(BlockchainImpl blockchain){
        this.blockchain = blockchain;
        this.twitterUserValidator = TwitterUserValidator.getInstance();
        this.hashcodeVerifier = HashcodeValidator.getInstance();
    }
    
    public static SocialLedgerManager getInstance(BlockchainImpl blockchain){
        if (INSTANCE == null)
            INSTANCE = new SocialLedgerManager(blockchain);
        return INSTANCE;
    }    
    
    public ImportResult bestBlockWaitForEndOfTimeSlot(Block block, long parentTimestamp){
        
        //validate the block before sleeping/waiting
        Repository repo = blockchain.getRepository();
        if (!blockchain.isValid(repo, block)) {
            socialLedgerLogger.warn("Invalid block with number: {}", block.getNumber());
            return ImportResult.INVALID_BLOCK;
        }
        
        socialLedgerLogger.info("Block has been validated: " + block.getShortDescr() + ". Now we need to wait until " + 
                "the end of the time-slot if required");
        
        return blockWaitForEndOfTimeSlot(block, parentTimestamp, true);
    }

    public ImportResult notBestBlockWaitForEndOfTimeSlot(byte[] parentHashArray, 
            Block block, long parentTimestamp){
        
        //validate the block before sleeping/waiting
        Block parentBlock = blockchain.getBlockByHash(block.getParentHash());
        Repository repo = blockchain.getRepository().getSnapshotTo(parentBlock.getStateRoot());
        if (!blockchain.isValid(repo, block)) {
            socialLedgerLogger.warn("Invalid block with number: " + block.getNumber() + ". Block: " + block.getShortDescr());
            return ImportResult.INVALID_BLOCK;
        }
        
        if (!BasicExtradataValidator.validate(block, blockchain)){
            socialLedgerLogger.warn("Invalid extradata in block with number: " + block.getNumber() + ". Block: " + block.getShortDescr());
            return ImportResult.INVALID_BLOCK;
        }
        
        String twitterUsername = UsernameProvider.provide(block.getExtraData());
        if (!twitterUserValidator.validate(twitterUsername)){
            socialLedgerLogger.warn("Invalid Twitter username in block with number: " + block.getNumber() + 
                    ". Block: " + block.getShortDescr() + ". Twitter username in extradata: " + twitterUsername);
        }
        
        
        
        socialLedgerLogger.info("Block has been validated: " + block.getShortDescr() + ". Now we need to wait until " + 
            "the end of the time-slot");
        return blockWaitForEndOfTimeSlot(block, parentTimestamp, false);
    }
    
    ImportResult blockWaitForEndOfTimeSlot(final Block block, long parentTimestamp, boolean isBest){
        
        final List<Byte> parentHashBytesList = new ArrayList<Byte>();
        Block winnerBlock = null;
        Future<Block> future;
        
        Object lock = new Object();
        synchronized(lock){                    
            for (byte bytePrimitive : block.getParentHash()){
                parentHashBytesList.add(bytePrimitive);
            }
        
            WaitingCallable waitingCallable;
                    
            if (blocksCallableMap.containsKey(parentHashBytesList)){
                Block currentBlock = blocksCallableMap.get(parentHashBytesList).getBlock();
            
                socialLedgerLogger.info("There is a competing block to be the chosen child of " + 
                        ByteUtil.toHexString(block.getParentHash()) + ". Current block: " + 
                        currentBlock.getShortDescr() + 
                        ". New (competing) block: " + block.getShortDescr());               
                 
                //IMPORTANT: so far we just use the dummie implementation
                BestBlockSelector bestBlockSelector = DummieBestBlockSelector.getInstance();  
                //BestBlockSelector bestBlockSelector = BestBlockSelectorBasedOnExtraData.getInstance();
                    
                BestBlock bestBlock = bestBlockSelector.select(currentBlock, block);
                if (bestBlock == BLOCK2){            
                    blocksCallableMap.get(parentHashBytesList).setBlock(block);
                }

                future =  blocksCallableMap.get(parentHashBytesList).getFuture();
                socialLedgerLogger.info("Going to wait in case more competing blocks arrive. " + 
                        "Exiting the synchronized block now");
            } else{
                socialLedgerLogger.info("There is NO competing block to be the chosen child of " + 
                        ByteUtil.toHexString(block.getParentHash()) + " so far. New (first) block: " + 
                        block.getShortDescr()); 
                        
                waitingCallable = new WaitingCallable(block.getParentHash(), block, parentTimestamp);
                blocksCallableMap.put(parentHashBytesList, waitingCallable);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                future = executor.submit(waitingCallable);
                waitingCallable.setFuture(future);
                executor.shutdown();
                socialLedgerLogger.info("Going to wait in case the first competing block arrives. " + 
                        "Exiting the synchronized block now");
                
                if (firstBlockMinedByUsTimestamp == -1L && didWeMineIt(block)) {
                    socialLedgerLogger.info("This is the first block that we mined: " + block.getShortDescr());
                    firstBlockMinedByUsTimestamp = block.getTimestamp();
                }
            }
        }//end of synchronized block
        
        try{
            //TO DO: probably only one thread should wait here
            winnerBlock = future.get();
        } catch(InterruptedException | ExecutionException ex){
            socialLedgerLogger.error(ex.toString(), ex);
        } finally{
            blocksCallableMap.remove(parentHashBytesList);
        }
        socialLedgerLogger.info("Woke up after waiting. Winner block: " + 
                winnerBlock == null ? null : winnerBlock.getShortDescr());
        
        if (Arrays.equals(block.getHash(), winnerBlock.getHash())) {
            if (isBest){
                blockchain.processBest(block);
                socialLedgerLogger.info("Going to return: " + ImportResult.IMPORTED_BEST);
                return ImportResult.IMPORTED_BEST;
            } else{
                blockchain.processNotBest(block);
                socialLedgerLogger.info("Going to return: " + ImportResult.IMPORTED_NOT_BEST);
                return ImportResult.IMPORTED_NOT_BEST;
            }                
        }        
        //this is not really accurate, it would be better to return something 
        //such as "LOSER" as per the Social Ledger algorithm
        socialLedgerLogger.info("Going to return: " + ImportResult.INVALID_BLOCK);
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
    
    static boolean didWeMineIt(Block block){
        byte[] thisMinerCoinBase = SystemProperties.getDefault().getMinerCoinbase();
        if (Arrays.equals(thisMinerCoinBase, block.getCoinbase())) {
            socialLedgerLogger.info("We mined this block: " + block.getShortDescr());
            return true;
        }
        socialLedgerLogger.info("We did not mine this block: " + block.getShortDescr());
        return false;
    }
}
