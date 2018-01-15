package org.ethereum.config;

import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.ethereum.config.SystemProperties.EXTRA_DATA_CHARSET;

import org.junit.Test;
import org.junit.Assert;

/**
 * @author Víctor Gil
 * Since Mon 15-Jan-2018  
 */
public class GetMineExtraDataTest{
    private static final Logger logger = LoggerFactory.getLogger(GetMineExtraDataTest.class);
    
    @Test
    public void testExtraData1(){
        String extraDataStr = "devaction";
        byte[] bytes = extraDataStr.getBytes(EXTRA_DATA_CHARSET);
        String extraDataHex = ByteUtil.toHexString(bytes);
        logger.debug("Extra data ASCII String: " + extraDataStr +  " --> " + "extra data in Hex format: " + extraDataHex);
        Assert.assertEquals("646576616374696f6e", extraDataHex);        
    }
}
