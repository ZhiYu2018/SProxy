package shadow.sock.FreeSea.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shadow.sock.FreeSea.Core.util.SUtils;
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
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.util.concurrent.Promise;

@ChannelHandler.Sharable
public class SocksServerConnectHandler extends SimpleChannelInboundHandler<SocksMessage>{
	private static Logger LOG = LoggerFactory.getLogger(SocksServerConnectHandler.class);
	private final Bootstrap remoteboot = new Bootstrap();
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SocksMessage msg)
			throws Exception {
		final Socks5CommandRequest request = (Socks5CommandRequest) msg;
		Promise<Channel> promise = ctx.executor().newPromise();
		promise.addListener(new Socks5Listener(ctx, request));
		
		/**connect**/
		final Channel inboundChannel = ctx.channel();
		remoteboot.group(inboundChannel.eventLoop()).channel(NioSocketChannel.class)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000).option(ChannelOption.SO_KEEPALIVE, true)
        .handler(new DirectClientHandler(promise));
		
		ChannelFutureListener futer = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // Connection established use handler provided results
                	ctx.pipeline().remove(SocksServerConnectHandler.this);
                } else {
                    // Close the connection if the connection attempt has failed.
                	LOG.warn("Connect to [{}:{}:{}] failed", request.dstAddrType(), request.dstAddr(), 
                			 request.dstPort());
                	Object msg = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, request.dstAddrType());
                    ctx.channel().writeAndFlush(msg);
                    SUtils.closeOnFlush(ctx.channel());
                }
            }
        };
        
        /**connect to**/
        remoteboot.connect(request.dstAddr(), request.dstPort()).addListener(futer);
	}
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOG.error("Catch:{}", cause.getMessage());
		cause.printStackTrace();
        SUtils.closeOnFlush(ctx.channel());
    }

}
