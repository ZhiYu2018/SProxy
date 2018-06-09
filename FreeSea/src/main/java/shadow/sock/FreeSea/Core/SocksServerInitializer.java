package shadow.sock.FreeSea.Core;

import java.util.concurrent.TimeUnit;

import shadow.sock.FreeSea.Core.ssl.SSLDecoder;
import shadow.sock.FreeSea.Core.ssl.SSLEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

public final class SocksServerInitializer extends ChannelInitializer<SocketChannel>{
	private final static int MAX_SIZE = 1024*1024;
	private final static int LENHTH_OFFSET = 0;
	private final static int L_LENTH = 4;
	private final static LengthFieldPrepender LE_NCODER = new LengthFieldPrepender(L_LENTH);
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		 ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG),
				               new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS),
				               new SSHeartbeat())
				    .addLast(LE_NCODER, SSLEncoder.SSLE, Socks5ServerEncoder.DEFAULT)
		            .addLast(new LengthFieldBasedFrameDecoder(MAX_SIZE, LENHTH_OFFSET, L_LENTH),new SSLDecoder())
				    .addLast("socks5", new Socks5InitialRequestDecoder())
				    .addLast(SocksServerHandler.getInstance());
	}
}