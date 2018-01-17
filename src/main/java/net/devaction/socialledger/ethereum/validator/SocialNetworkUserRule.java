package net.devaction.socialledger.ethereum.validator;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockHeader;
import org.ethereum.validator.DependentBlockHeaderRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ethereum.config.SystemProperties.EXTRA_DATA_CHARSET;

/**
 * @author Víctor Gil
 * 
 * Since 15-JAN-2018 
 *  */
public class SocialNetworkUserRule extends DependentBlockHeaderRule{
    private static final Logger socialLedgerLogger = LoggerFactory.getLogger(DependentBlockHeaderRule.class);
    
    private final SystemProperties config;

    public SocialNetworkUserRule(SystemProperties config){
        this.config = config;
    }

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {

        errors.clear();
        byte[] blockExtraDataBytes = header.getExtraData();
        byte[] parentExtraDataBytes = parent.getExtraData();
        
        //we comment out this temporarily
        /*
        if (Util.isExtraDataTheSame(blockExtraDataBytes, parentExtraDataBytes)){ 
            String errorMsg = "The block extra data (i.e, social network user)"
                    + " is the same as its parent extra data (social network user)." +
                    " Block extra data: " + new String(blockExtraDataBytes, EXTRA_DATA_CHARSET) + 
                    ". Parent block extra data: " + new String(parentExtraDataBytes, EXTRA_DATA_CHARSET);
            socialLedgerLogger.debug(errorMsg);
            errors.add(String.format("block #%d:" + errorMsg, header.getNumber()));
            return false;            
        }
        */
        
        return true;
    }
}
