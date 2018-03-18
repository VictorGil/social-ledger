package net.devaction.socialledger.ethereum.core.extradata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Víctor Gil
 * 
 * since Sun 2018-Mar-18 
 */
public class UsernameProviderTester {
    private static final Log log = LogFactory.getLog(UsernameProviderTester.class);
    
    public static void main(String[] args){
        String extradata = "twitter458891agora";
        
        log.info("extradata: " + extradata);
        String username = UsernameProvider.provide(extradata);
        log.info("username: " + username);
        
        String twitter = extradata.substring(0,7);
        log.info("substirng(0,7): " + twitter);
    }
}


