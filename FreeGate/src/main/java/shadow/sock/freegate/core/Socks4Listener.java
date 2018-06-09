package shadow.sock.freegate.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shadow.sock.freegate.util.SUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandStatus;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
public class Socks4Listener implements GenericFutureListener<Future<Channel>>{
	private static Logger LOG = LoggerFactory.getLogger(Socks4Listener.class);
	private final ChannelHandlerContext ctx;
	private final Socks4CommandRequest request;
	private final SocksServerConnectHandler ssch;
	public Socks4Listener(final ChannelHandlerContext ctx, 
			              final Socks4CommandRequest request,
			              final SocksServerConnectHandler ssch){
		this.ctx = ctx;
		this.request = request;
		this.ssch = ssch;
	}
	
	@Override
	public void operationComplete(Future<Channel> future) throws Exception {
		final Channel outboundChannel = future.getNow();
		if(!future.isSuccess()){
			LOG.warn("Requset:{}:{} failed", request.dstAddr(), 
					 request.dstPort());
			Object msg = new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED);
			ctx.channel().writeAndFlush(msg);
            SUtils.closeOnFlush(ctx.channel());
            return ;
		}
		
		Object msg = new DefaultSocks4CommandResponse(Socks4CommandStatus.SUCCESS);
		ChannelFuture responseFuture = ctx.channel().writeAndFlush(msg);
		
		responseFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                try{
               	 ctx.pipeline().remove(ssch);
                }catch(Throwable t){
               	 LOG.warn("Remove ssch failed:{}:{}", request.dstAddr(), request.dstPort());
                }
                outboundChannel.pipeline().addLast(new RelayHandler(ctx.channel()));
                ctx.pipeline().addLast(new RelayHandler(outboundChannel));
            }
        });
		
	}

}
