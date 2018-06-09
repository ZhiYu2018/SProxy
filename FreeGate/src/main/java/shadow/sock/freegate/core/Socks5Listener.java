package shadow.sock.freegate.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shadow.sock.freegate.util.SUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class Socks5Listener implements GenericFutureListener<Future<Channel>>{
	private static Logger LOG = LoggerFactory.getLogger(Socks5Listener.class);
	private Socks5Objs sobjs;
	
	public Socks5Listener(Socks5Objs sobjs){
		this.sobjs = sobjs;
	}

	@Override
	public void operationComplete(Future<Channel> future) throws Exception {
		if(!future.isSuccess()){
			LOG.warn("{} request:[{}:{}] failed", sobjs.getCtx().channel().remoteAddress().toString(), 
					sobjs.getRequest().dstAddr(), sobjs.getRequest().dstPort());
			
			Object msg = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, 
					                                      sobjs.getRequest().dstAddrType());
			sobjs.getCtx().channel().write(msg);
			SUtils.closeOnFlush(sobjs.getCtx().channel());
			return ;
		}

		final Channel outboundChannel = future.getNow();
		Object msg = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS,
				sobjs.getRequest().dstAddrType(),
				sobjs.getRequest().dstAddr(),
				sobjs.getRequest().dstPort());
		
		ChannelFuture responseFuture = sobjs.getCtx().channel().writeAndFlush(msg);
		responseFuture.addListener(new ChannelFutureListener() {
             @Override
             public void operationComplete(ChannelFuture channelFuture) {
                 outboundChannel.pipeline().addLast(new RelayHandler(sobjs.getCtx().channel()));
                 sobjs.getCtx().pipeline().addLast(new RelayHandler(outboundChannel));
             }
         });
	}

}
