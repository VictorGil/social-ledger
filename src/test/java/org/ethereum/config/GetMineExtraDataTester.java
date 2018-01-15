package org.ethereum.config;

import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.ethereum.config.SystemProperties.EXTRA_DATA_CHARSET;

/**
 * @author Víctor Gil
 * Since Mon 15-Jan-2018  
 */
public class GetMineExtraDataTester{
    private static final Logger logger = LoggerFactory.getLogger(GetMineExtraDataTester.class);
    
    public static void main(String[] args){
        String extraDataStr = "devaction";
        byte[] bytes = extraDataStr.getBytes(EXTRA_DATA_CHARSET);
        String extraDataHex = ByteUtil.toHexString(bytes);
        logger.info("Extra data ASCII String: " + extraDataStr +  " --> " + "extra data in Hex format: " + extraDataHex);
    }
}
