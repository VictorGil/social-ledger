package net.devaction.socialledger.ethereum.core.blockutil;

import org.ethereum.jsonrpc.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.bind.DatatypeConverter;
/**
 * @author Víctor Gil
 * Since Fri 26-Jan-2018  
 */
public class GenesisInfoTester{
    private static final Logger logger = LoggerFactory.getLogger(GenesisInfoTester.class);
    
    public static void main(String[] args) throws Exception{
        //64 zeros
        String allZerosHash ="0000000000000000000000000000000000000000000000000000000000000000"; 
        logger.info("All zeros hash: " + allZerosHash);    
        byte[] allZerosHashBytes = TypeConverter.StringHexToByteArray(allZerosHash);
        String allZerosHashDecoded = DatatypeConverter.printHexBinary(allZerosHashBytes);
        logger.info("All zeros hash decoded from byte array: " + allZerosHashDecoded);  
        
        allZerosHashBytes = GenesisInfo.provideAllZerosHash();
        allZerosHashDecoded = DatatypeConverter.printHexBinary(allZerosHashBytes);
        logger.info("All zeros hash from GenesisInfo.provideAllZerosHash(): " + allZerosHashDecoded);  
    }
}
