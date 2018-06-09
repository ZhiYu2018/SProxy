package shadow.sock.freegate.util;

import java.io.Closeable;

import org.slf4j.Logger;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

public class SUtils {
	
	public static int getSecond(long ms){
		return (int)(ms/1000);
	}
	/**
    * Closes the specified channel after all queued write requests are flushed.
    */
    public static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    public static void assertLog(String key, int f, int s, final Logger log){
    	if((f < s)){
    		log.warn("{} assert {} < {} failed:", key, f, s);
    	}
    }
    
    public static void safeRemove(ChannelPipeline cp, ChannelHandler handler){
    	try{
    		cp.remove(handler);
    	}catch(Throwable t){
    		
    	}
    }
    
    public static void safeClose(Closeable cb){
    	if(cb == null){
    		return ;
    	}
    	
		try{
			cb.close();
		}catch(Throwable t){
			
		}
    }
}
