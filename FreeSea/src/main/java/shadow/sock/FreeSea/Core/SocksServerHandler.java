package shadow.sock.FreeSea.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;

@ChannelHandler.Sharable
public final class SocksServerHandler extends SimpleChannelInboundHandler<SocksMessage>{
	private static Logger LOG = LoggerFactory.getLogger(SocksServerHandler.class);
	private static volatile SocksServerHandler INSTANCE = null;
	
	public static SocksServerHandler getInstance(){
		if(INSTANCE == null){
			synchronized(SocksServerHandler.class){
				if(INSTANCE == null){
					INSTANCE = new SocksServerHandler();
				}
			}
		}
		
		return INSTANCE;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SocksMessage msg)
			throws Exception {
		if(msg.version() != SocksVersion.SOCKS5){
			LOG.info("do not support sock version {} connected from {}",msg.version(),
					ctx.channel().remoteAddress().toString());
			ctx.close();
			return ;
		}
		
		if(msg instanceof Socks5InitialRequest){
			ctx.pipeline().replace("socks5", "socks5", new Socks5PasswordAuthRequestDecoder());
			ctx.writeAndFlush(new DefaultSocks5InitialResponse(Socks5AuthMethod.PASSWORD));
		}
		else if(msg instanceof Socks5PasswordAuthRequest){
			ctx.pipeline().replace("socks5", "socks5", new Socks5CommandRequestDecoder());
			/**check user name and pwd**/
			ctx.writeAndFlush(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS));
		}
		else if(msg instanceof Socks5CommandRequest){
			Socks5CommandRequest s5c = (Socks5CommandRequest) msg;
			if(s5c.type() == Socks5CommandType.CONNECT){
				ctx.pipeline().addLast(new SocksServerConnectHandler());
				ctx.pipeline().remove(this);
				ctx.fireChannelRead(msg);
				
			}else{
				LOG.warn("socks5: {} error status", ctx.channel().remoteAddress().toString());
				ctx.close();
			}
		}else{
			LOG.warn("socks5: {} error msg", ctx.channel().remoteAddress().toString());
			ctx.close();
		}
	}

}
