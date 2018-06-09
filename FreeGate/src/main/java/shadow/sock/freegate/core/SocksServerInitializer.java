package shadow.sock.freegate.core;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

public final class SocksServerInitializer extends ChannelInitializer<SocketChannel>{
	public SocksServerInitializer(){
	}
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		 ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG),
				    new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS),
				    new SSHeartbeat(),
	                new SocksPortUnificationServerHandler(),
	                SocksServerHandler.getInstance());
	}
}
