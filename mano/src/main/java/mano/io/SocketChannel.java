
package mano.io;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * @deprecated 移除
 * @author jun
 */


public class SocketChannel {

    public static void main(String[] args) throws Exception {
        
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8082));
        serverChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        SelectionKey key;
        Iterator<SelectionKey> iter;
        for(;;){
            if(selector.select()<=0){
                //todo stop...
                continue;
            }
            iter = selector.selectedKeys().iterator();
            while(iter.hasNext()){
                key = iter.next();
                if(!key.isValid()){
                    key.cancel();
                    iter.remove();
                }
                
                if(key.isAcceptable()){
                    java.nio.channels.SocketChannel client = serverChannel.accept();
                    client.socket().getOutputStream().write(null);
                    key.interestOps(0);
                    key.selector().wakeup();
                }
                else if(key.isConnectable()){
                    
                }
                else if(key.isReadable()){
                    
                }
                else if(key.isWritable()){
                    
                }
                iter.remove();
            }
        }
        
    }
    
    
    protected void processAccepted(){
        //超时
        java.util.Timer timer;
    }
    
}
