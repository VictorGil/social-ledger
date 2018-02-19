package net.devaction.socialledger.ethereum.core.blockutil;

import java.util.Arrays;

import org.ethereum.core.Block;

/**
 * @author Víctor Gil
 * Since Fri 26-Jan-2018 
 *  
 */
//this class is not used so far
public class GenesisInfo{

    public static boolean isGenesis(Block block){
        return Arrays.equals(block.getParentHash(), provideAllZerosHash());  
    }
    
    static byte[] provideAllZerosHash(){
        byte[] zeros = new byte[32];
        
        for (int i = 0; i<zeros.length; i++)
            zeros[i] = 0;
        
        return zeros;
    }
}
