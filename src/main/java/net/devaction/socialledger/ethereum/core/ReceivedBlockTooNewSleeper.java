package net.devaction.socialledger.ethereum.core;

import org.ethereum.core.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.devaction.socialledger.ethereum.core.SocialLedgerManager.TIME_SLOT_IN_SECS;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Víctor Gil
 * Since Thu 1-Feb-2018  
 */
public class ReceivedBlockTooNewSleeper{
    private static final Logger logger = LoggerFactory.getLogger(ReceivedBlockTooNewSleeper.class);
    private static final int MIN_MILLIS = 200;
    
    public static void waitIfRequired(Block block){
        long endOfThisTimeSlot = block.getTimestamp() * 1000;
        waitIfRequired(endOfThisTimeSlot);
    }
    
    static void waitIfRequired(long endOfThisTimeSlot){
        long currentTimeMillis = new Date().getTime();
        long milliSecondsToWait = endOfThisTimeSlot - TIME_SLOT_IN_SECS * 1000 + MIN_MILLIS - currentTimeMillis;
        
        if (milliSecondsToWait > 0) {
            logger.info("We need to sleep " + milliSecondsToWait + " milliseconds");
            sleep(milliSecondsToWait);
            logger.info("We woke up after " + milliSecondsToWait + " milliseconds");
        } else
            logger.info("We do not need to sleep");
    }
    
    static void sleep(long milliSecondsToWait){
        try{
            TimeUnit.MILLISECONDS.sleep(milliSecondsToWait);
        } catch(InterruptedException ex){
            logger.error(ex.toString(), ex);
        }
    }
}
