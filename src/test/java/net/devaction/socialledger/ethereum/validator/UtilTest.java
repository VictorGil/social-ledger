package net.devaction.socialledger.ethereum.validator;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Víctor Gil
 * Since Mon 15-Jan-2018  
 */
public class UtilTest{
private static final Logger logger = LoggerFactory.getLogger(UtilTest.class);
    
    @Test
    public void testUtil1(){
        String extraDataStr1 = "twitter0devactionnet";        
        String extraDataStr2 = "Twitter1devactionnet";
        boolean result = Util.isExtraDataTheSame(extraDataStr1, extraDataStr2);
        logger.debug("Extra data String 1: " + extraDataStr1);
        logger.debug("Extra data String 2: " + extraDataStr2);
        logger.debug("Are they equal?: " + result);
        Assert.assertTrue(result);       
    }
    
    @Test
    public void testUtil2(){
        String extraDataStr1 = "twitter0devactionnet";        
        String extraDataStr2 = "Twitter1devactionnet5";
        boolean result = Util.isExtraDataTheSame(extraDataStr1, extraDataStr2);
        logger.debug("Extra data String 1: " + extraDataStr1);
        logger.debug("Extra data String 2: " + extraDataStr2);
        logger.debug("Are they equal?: " + result);
        Assert.assertFalse(result);       
    }
}
