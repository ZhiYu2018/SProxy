package shadow.sock.freegate.core;

import shadow.sock.freegate.core.ssl.SSLDecoder;
import shadow.sock.freegate.core.ssl.SSLEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.socksx.v5.Socks5ClientEncoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public final class AuthChannelInitializer extends ChannelInitializer<SocketChannel>{
	private final static int MAX_SIZE = 1024*1024;
	private final static int LENHTH_OFFSET = 0;
	private final static int L_LENTH = 4;
	private final static LengthFieldPrepender LE_NCODER = new LengthFieldPrepender(L_LENTH);
	private Socks5Objs objs;
	public AuthChannelInitializer(Socks5Objs objs){
		this.objs = objs;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		 ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG))
		              .addLast(LE_NCODER, SSLEncoder.SSLE, Socks5ClientEncoder.DEFAULT)
				      .addLast(new LengthFieldBasedFrameDecoder(MAX_SIZE, LENHTH_OFFSET, L_LENTH),new SSLDecoder())
		              .addLast("socks5c", new Socks5InitialResponseDecoder())
				      .addLast(new AuthSocksHandler(objs));
	}

}
