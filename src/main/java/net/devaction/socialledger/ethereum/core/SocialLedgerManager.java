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
import net.devaction.socialledger.algorithm.BestBlockSelectorBasedOnExtraData;
import net.devaction.socialledger.algorithm.DummieBestBlockSelector;
import net.devaction.socialledger.ethereum.core.extradata.BasicExtradataValidator;
import net.devaction.socialledger.ethereum.core.extradata.UsernameProvider;
import net.devaction.socialledger.validatorusingtwitter.TwitterProvider;
import net.devaction.socialledger.validatorusingtwitter.tweet.TextTweetter;
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
    //40 seconds because of twitter API tweeting limit 
    public static final int TIME_SLOT_IN_SECS = 40;
    
    //Maybe there is always just one entry in this map at most but I am not sure
    //better to use a map so far, just in case
    private final Map<List<Byte>, WaitingCallable> blocksCallableMap = new ConcurrentHashMap<List<Byte>, WaitingCallable>();
    
    private final BlockchainImpl blockchain;
    
    private volatile long firstBlockMinedByUsTimestamp = -1L;
    private volatile boolean oneOfOurMinedBlocksIsWaitingForCompetitors; 
    
    private final TwitterUserValidator twitterUserValidator;
    private final HashcodeValidator hashcodeValidator; 
    private final TextTweetter textTweetter;

    private SocialLedgerManager(BlockchainImpl blockchain){
        String twitterApiKey = blockchain.getConfig().getTwitterConsumerApiKey();
        String twitterApiSecret = blockchain.getConfig().getTwitterConsumerApiSecret();
        String twitterAccessToken = blockchain.getConfig().getTwitterAccessToken();
        String twitterAccessTokenSecret = blockchain.getConfig().getTwitterAccessTokenSecret();
        
        TwitterProvider twitterProvider = new TwitterProvider(
                twitterApiKey, twitterApiSecret, twitterAccessToken, twitterAccessTokenSecret);
        
        this.blockchain = blockchain;
        this.twitterUserValidator = new TwitterUserValidator(twitterProvider.provide());
        this.hashcodeValidator = new HashcodeValidator(twitterProvider.provide());
        this.textTweetter = new TextTweetter(twitterProvider.provide());        
    }
    
    public static SocialLedgerManager getInstance(BlockchainImpl blockchain){
        if (INSTANCE == null)
            INSTANCE = new SocialLedgerManager(blockchain);
        return INSTANCE;
    }    
    
    public ImportResult bestBlockWaitForEndOfTimeSlot(Block block, long parentTimestamp){
        
        //validate the block before sleeping/waiting
        Repository repo = blockchain.getRepository();
        
        if (!validateBlockUsingTwitter(repo, block)){
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
        
        if (!validateBlockUsingTwitter(repo, block)){
            return ImportResult.INVALID_BLOCK;
        }
        
        return blockWaitForEndOfTimeSlot(block, parentTimestamp, false);
    }

    boolean validateBlockUsingTwitter(Repository repo, Block block){
        if (!blockchain.isValid(repo, block)) {
            socialLedgerLogger.warn("Invalid block with number: " + block.getNumber() + ". Block: " + block.getShortDescr());
            return false;
        }
        
        if (!BasicExtradataValidator.validate(block, blockchain)){
            socialLedgerLogger.warn("Invalid extradata in block with number: " + block.getNumber() + ". Block: " + block.getShortDescr());
            return false;
        }
        
        String twitterUsername = UsernameProvider.provide(block.getExtraData());
        if (!twitterUserValidator.validate(twitterUsername)){
            socialLedgerLogger.warn("Invalid Twitter username in block with number: " + block.getNumber() + 
                    ". Block: " + block.getShortDescr() + ". Twitter username in extradata: " + twitterUsername);
            return false;
        }
        
        long blockTimestampInMillis = block.getTimestamp() * 1000;
        long limitTimestampInMillis = blockTimestampInMillis - 1000 * 60 * 5;//5 minutes before 
        String blockHashcodeString = ByteUtil.toHexString(block.getHash());        
        if (!hashcodeValidator.validate(blockHashcodeString, twitterUsername, limitTimestampInMillis)){
            socialLedgerLogger.warn("Could not validate the block hashcode in twitter: " + blockHashcodeString + 
                    ". Block: " + block.getShortDescr() + ". Twitter username in extradata: " + twitterUsername);
            return false;
        }        
        
        socialLedgerLogger.info("Block has been validated: " + block.getShortDescr() + ". Now we need to wait until " + 
            "the end of the time-slot");
        return true;
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
                 
                //IMPORTANT: so far we just use the dummy implementation
                //BestBlock bestBlock = DummieBestBlockSelector.select(currentBlock, block);  
                
                BestBlock bestBlock = BestBlockSelectorBasedOnExtraData.select(currentBlock, block, blockchain);
                
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
                
                if (didWeMineIt(block)) {
                    oneOfOurMinedBlocksIsWaitingForCompetitors = true;
                    if (firstBlockMinedByUsTimestamp == -1L){
                        socialLedgerLogger.info("This is the first block that we mined: " + block.getShortDescr());
                        firstBlockMinedByUsTimestamp = block.getTimestamp();    
                    }
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
        
        oneOfOurMinedBlocksIsWaitingForCompetitors = false;
        
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
    
    public void tweet(byte[] blockHash){
        String blockHashString = ByteUtil.toHexString(blockHash);
        socialLedgerLogger.info("Going to tweet: " + blockHashString);
        textTweetter.tweet(blockHashString);
    }
    
    public boolean isOneOfOurMinedBlocksWaitingForCompetitors(){
        return oneOfOurMinedBlocksIsWaitingForCompetitors;
    }
}
