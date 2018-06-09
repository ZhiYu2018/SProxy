package shadow.sock.FreeSea.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shadow.sock.FreeSea.Core.util.SUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class Socks5Listener implements GenericFutureListener<Future<Channel>>{
	private static Logger LOG = LoggerFactory.getLogger(Socks5Listener.class);
	private final ChannelHandlerContext ctx;
	private final Socks5CommandRequest request;
	
	public Socks5Listener(final ChannelHandlerContext ctx, final Socks5CommandRequest request){
		this.ctx = ctx;
		this.request = request;
	}

	@Override
	public void operationComplete(Future<Channel> future) throws Exception {
		if(!future.isSuccess()){
			LOG.warn("{} request:[{}:{}] failed", request.dstAddrType(), request.dstAddr(), 
					request.dstPort());
			
			Object msg = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE,request.dstAddrType());
			ctx.channel().writeAndFlush(msg);
			SUtils.closeOnFlush(ctx.channel());
			return ;
		}
		
		final Channel outboundChannel = future.getNow();
		Object msg = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS,
                                                      request.dstAddrType(),
                                                      request.dstAddr(),
                                                      request.dstPort());
		
		ChannelFuture responseFuture = ctx.channel().writeAndFlush(msg);
		responseFuture.addListener(new ChannelFutureListener() {
             @Override
             public void operationComplete(ChannelFuture channelFuture) {
                 /**OUT:IN**/
                 outboundChannel.pipeline().addLast(new RelayHandler(ctx.channel()));  
                 /**IN:OUT**/
                 ctx.pipeline().addLast(new RelayHandler(outboundChannel));
             }
         });
	}

}
