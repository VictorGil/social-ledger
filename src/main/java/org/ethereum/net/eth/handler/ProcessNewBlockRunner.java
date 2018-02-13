package org.ethereum.net.eth.handler;

import org.ethereum.net.eth.message.NewBlockMessage;

/**
 * @author Víctor Gil
 * Since Tue 13-Feb-2018  
 */
public class ProcessNewBlockRunner implements Runnable{

    private final Eth62 eth62;
    private final NewBlockMessage newBlockMessage;
    
    protected ProcessNewBlockRunner(Eth62 eth62, NewBlockMessage newBlockMessage){
        this.eth62 = eth62;
        this.newBlockMessage = newBlockMessage;
    }
    
    @Override
    public void run(){
        eth62.processNewBlockAsynchronously(newBlockMessage);
    }    
}
