package shadow.sock.freegate.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shadow.sock.freegate.util.SUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandStatus;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.util.concurrent.Promise;

@ChannelHandler.Sharable
public class SocksServerConnectHandler extends SimpleChannelInboundHandler<SocksMessage>{
	private static Logger LOG = LoggerFactory.getLogger(SocksServerConnectHandler.class);
	private final Bootstrap remoteboot = new Bootstrap();
	public SocksServerConnectHandler(){
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, final SocksMessage msg)throws Exception {
		if(msg instanceof Socks4CommandRequest){
			socks4Read0(ctx, msg);
		}
		else if (msg instanceof Socks5CommandRequest){
			socks5Read0(ctx, msg);
		}else{
			LOG.warn("Unknow msg");
			ctx.close();
		}
	}
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOG.error("{}：Catch:{}",ctx.channel().remoteAddress().toString(), cause.getMessage());
		cause.printStackTrace();
        SUtils.closeOnFlush(ctx.channel());
    }
	
	
	private void socks4Read0(ChannelHandlerContext ctx, final SocksMessage msg){
		final Socks4CommandRequest request = (Socks4CommandRequest) msg;
		Promise<Channel> promise = ctx.executor().newPromise();
		promise.addListener(new Socks4Listener(ctx, request, this));
		
		final Channel inboundChannel = ctx.channel();
		remoteboot.group(inboundChannel.eventLoop()).channel(NioSocketChannel.class)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .handler(new DirectClientHandler(promise));
		
		/**connect to remote**/
		remoteboot.connect(request.dstAddr(), request.dstPort()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // Connection established use handler provided results
                } else {
                    // Close the connection if the connection attempt has failed.
                	LOG.warn("{}:Connect to {}:{} failed", ctx.channel().remoteAddress().toString(), 
                			request.dstAddr(), request.dstPort());
                	Object msg = new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED);
                    ctx.channel().writeAndFlush(msg);
                    SUtils.closeOnFlush(ctx.channel());
                }
            }
        });
	}
	
	private void socks5Read0(ChannelHandlerContext ctx, final SocksMessage msg){
		final Socks5CommandRequest request = (Socks5CommandRequest) msg;
		ChannelHandler handler = null;
		Socks5Objs objs = new Socks5Objs(ctx, request, true);
		String addr;
		int port;
		final Channel inboundChannel = ctx.channel();
		
		if(!objs.isAuthRemote()){
			addr = request.dstAddr();
			port = request.dstPort();
			Promise<Channel> promise = ctx.executor().newPromise();
			promise.addListener(new Socks5Listener(objs));
			handler = new DirectClientHandler(promise);
		}else{
			addr = SocksConf.getConf().getRemoteAddr();
			port = SocksConf.getConf().getPort();
			handler = new AuthChannelInitializer(objs);
		}
		
		remoteboot.group(inboundChannel.eventLoop()).channel(NioSocketChannel.class)
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
		.option(ChannelOption.SO_KEEPALIVE, true)
		.handler(handler);
				
		ChannelFutureListener futer = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // Connection established use handler provided results
                	SUtils.safeRemove(ctx.pipeline(), SocksServerConnectHandler.this);
                } else {
                    // Close the connection if the connection attempt has failed.
                	LOG.info("Connect to {}：{} failed", addr, port);
                	Object msg = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, 
                			                                      request.dstAddrType());
                    ctx.channel().writeAndFlush(msg);
                    SUtils.closeOnFlush(ctx.channel());
                }
            }
        };
        
        /**connect to remote addr**/
		remoteboot.connect(addr, port).addListener(futer);
		
	}

}
